package us.dot.its.jpo.rsustatusmonitor.services;

import java.util.Map;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.rsustatusmonitor.snmp.SnmpProperties;

import java.io.IOException;

@Service
@Slf4j
public class SNMPService {

    private SnmpProperties properties;

    @Autowired
    public SNMPService(SnmpProperties properties) {
        this.properties = properties;
    }

    // Some RSUs do not require authentication to retrieve information. They may
    // respond to SNMP V2 requests.
    public String getSnmpV2Value(String ipAddress, String community, String oid) throws Exception {

        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community));
        target.setAddress(new UdpAddress(ipAddress + "/" + properties.getPort()));
        target.setRetries(properties.getRetries());
        target.setTimeout(properties.getTimeout());
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        ResponseEvent<UdpAddress> responseEvent = snmp.send(pdu, target);
        snmp.close();

        if (responseEvent != null && responseEvent.getResponse() != null) {
            VariableBinding vb = responseEvent.getResponse().get(0);
            return vb.getVariable().toString();
        } else {
            throw new RuntimeException("SNMP GET timed out or returned null.");
        }

    }

    public Variable getSnmpV3Value(String ip, String username, String authPass, String privPass, String oid)
            throws IOException {

        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        // Add USM and user
        USM usm = new USM(
                SecurityProtocols.getInstance(),
                new OctetString(MPv3.createLocalEngineID()),
                0);
        SecurityModels.getInstance().addSecurityModel(usm);

        // Add Required Security Protocols
        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA());
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES128());

        snmp.getUSM().addUser(
                new OctetString(username),
                new UsmUser(
                        new OctetString(username),
                        AuthSHA.ID, new OctetString(authPass),
                        PrivAES128.ID, new OctetString(privPass)));

        // Configure the target
        UserTarget<UdpAddress> target = new UserTarget<>();
        target.setAddress(new UdpAddress(ip + "/" + properties.getPort()));
        target.setRetries(properties.getRetries());
        target.setTimeout(properties.getTimeout());
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(new OctetString(username));

        // Create a ScopedPDU for SET
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(new OID(oid)));

        ResponseEvent<UdpAddress> response = snmp.send(pdu, target);
        snmp.close();
        if (response != null && response.getResponse() != null) {
            VariableBinding vb = response.getResponse().get(0);
            return vb.getVariable();
        }
        return null;

    }

    public void setSnmpV3Value(
            String ipAddress,
            String username,
            String authPass,
            String oid,
            int intValue // directly passing integer for clarity
    ) throws Exception {

        // Setup SNMP and transport
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        // Add USM and user
        USM usm = new USM(
                SecurityProtocols.getInstance(),
                new OctetString(MPv3.createLocalEngineID()),
                0);
        SecurityModels.getInstance().addSecurityModel(usm);

        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA());
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES128());

        snmp.getUSM().addUser(
                new OctetString(username),
                new UsmUser(
                        new OctetString(username),
                        AuthSHA.ID, new OctetString(authPass),
                        PrivAES128.ID, new OctetString(authPass)));

        // Configure the target
        UserTarget<UdpAddress> target = new UserTarget<>();
        target.setAddress(new UdpAddress(ipAddress + "/" + properties.getPort()));
        target.setRetries(properties.getRetries());
        target.setTimeout(properties.getTimeout());
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(new OctetString(username));

        // Create a ScopedPDU for SET
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.SET);
        pdu.add(new VariableBinding(new OID(oid), new Integer32(intValue)));

        // Send and handle response
        ResponseEvent<UdpAddress> response = snmp.send(pdu, target);
        snmp.close();
        if (response == null || response.getResponse() == null) {
            log.warn("Received Null Response from RSU unit " + ipAddress);
        }

        if (response.getResponse().getErrorStatus() != PDU.noError) {
            log.warn("Error while setting value on RSU unit " + ipAddress);
        }
    }

    // Used for setting multiple OID values at the same time. RSUs require this for
    // many configurations
    public void setSnmpV3Values(
            String ipAddress,
            String username,
            String authPass,
            Map<String, Variable> oidValuePairs) throws Exception {

        // Setup SNMP and transport
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        // Add USM and user
        USM usm = new USM(
                SecurityProtocols.getInstance(),
                new OctetString(MPv3.createLocalEngineID()),
                0);
        SecurityModels.getInstance().addSecurityModel(usm);

        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA());
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES128());

        snmp.getUSM().addUser(
                new OctetString(username),
                new UsmUser(
                        new OctetString(username),
                        AuthSHA.ID, new OctetString(authPass),
                        PrivAES128.ID, new OctetString(authPass)));

        // Configure the target
        UserTarget<UdpAddress> target = new UserTarget<>();
        target.setAddress(new UdpAddress(ipAddress + "/" + properties.getPort()));
        target.setRetries(properties.getRetries());
        target.setTimeout(properties.getTimeout());
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(new OctetString(username));

        // Create a ScopedPDU for SET with multiple OIDs
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.SET);

        // Add all OID-value pairs to the same PDU
        for (java.util.Map.Entry<String, Variable> entry : oidValuePairs.entrySet()) {
            pdu.add(new VariableBinding(new OID(entry.getKey()), entry.getValue()));
        }

        // Send and handle response
        ResponseEvent<UdpAddress> response = snmp.send(pdu, target);
        snmp.close();
        if (response == null || response.getResponse() == null) {
            log.warn("Received Null Response from RSU unit" + ipAddress);
        }

        if (response.getResponse().getErrorStatus() != PDU.noError) {
            log.warn("Error while setting values on RSU unit" + ipAddress + ". Error: " +
                    response.getResponse().getErrorStatusText());
        }
    }
}
