package us.dot.its.jpo.rsustatusmonitor.services;

import java.io.IOException;
import java.time.Instant;

import org.snmp4j.smi.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;
import us.dot.its.jpo.rsustatusmonitor.models.snmp.OID;
import us.dot.its.jpo.rsustatusmonitor.models.snmp.OIDMap;
import us.dot.its.jpo.rsustatusmonitor.models.snmp.RsuState;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
@Slf4j
public class RsuQueryService {

    private SNMPService snmpService;
    private KafkaProducerService kafkaService;
    private MeterRegistry meterRegistry;

    @Autowired
    public RsuQueryService(SNMPService snmpService, KafkaProducerService kafkaService, MeterRegistry meterRegistry) {
        this.snmpService = snmpService;
        this.kafkaService = kafkaService;
        this.meterRegistry = meterRegistry;
    }

    @Async
    public void getRsuInformation(RsuSnmpCredentials cred) {
        String username = cred.getUsername();
        String password = cred.getPassword();
        String encPass = cred.getEncrypt_password();
        String ip = cred.getIpv4_address();
        String intersectionId = cred.getIntersection_id() != null ? cred.getIntersection_id() : "-1";

        log.info("Pulling SNMP Status for RSU: " + ip + " IntersectionID: " + intersectionId);

        Counter.builder("rsu.snmp.status")
                .description("Number of snmp status queries by RSU")
                .tags("rsu_ip", ip)
                .register(meterRegistry)
                .increment();

        if (username == null || password == null || ip == null) {
            log.warn("Cannot pull data from RSU unit. Missing Username, Password, or IP address. RSU ID: " + ip
                    + " Intersection ID: " + intersectionId);

            Counter.builder("rsu.snmp.status.error")
                    .description("Number of snmp status errors queries by RSU")
                    .tags("rsu_ip", ip, "error", "credential-error")
                    .register(meterRegistry)
                    .increment();
            return;
        }

        // enc password is not defined for all RSU units. Try using normal password
        if (encPass == null) {
            encPass = password;
        }

        RsuState state = new RsuState();
        state.timestamp = Instant.now().toEpochMilli();
        state.rsuIP = ip;
        state.intersectionID = intersectionId;

        state.uptime = getIntOID(ip, username, password, encPass, OIDMap.oids.get("rsuTimeSincePowerOn"));
        state.temperature = getIntOID(ip, username, password, encPass, OIDMap.oids.get("rsuIntTemp"));
        state.mode = getIntOID(ip, username, password, encPass, OIDMap.oids.get("rsuModeStatus"));

        RsuIntersectionKey key = new RsuIntersectionKey();
        key.setIntersectionId(Integer.parseInt(intersectionId));
        key.setRsuId(ip);
        key.setRegion(-1);

        kafkaService.sendRsuStatus(key, state);

        log.info("Retrieved RSU Information for RSU: " + ip + " IntersectionID: " + intersectionId
                + " Uptime: " + state.uptime + " Temperature: " + state.temperature + " Mode: " + state.mode);
    }

    public int getIntOID(String ip, String username, String password, String encPass, OID oid) {
        try {
            Variable var = snmpService.getSnmpV3Value(ip, username, password, encPass, oid.getOid());

            if (var != null) {
                return var.toInt();
            } else {
                log.warn("Query of OID " + oid.getName() + " for Intersection" + ip + " returned no value");
                Counter.builder("rsu.snmp.status.error")
                        .description("Number of snmp status errors queries by RSU")
                        .tags("rsu_ip", ip, "error", "no-value", "oid", oid.getName())
                        .register(meterRegistry)
                        .increment();
            }
        } catch (IOException e) {
            log.warn("Unable to Retrieve value for OID: " + oid.getName() + " for Intersection" + ip);
            Counter.builder("rsu.snmp.status.error")
                    .description("Number of snmp status errors queries by RSU")
                    .tags("rsu_ip", ip, "error", "IO Exception", "oid", oid.getName())
                    .register(meterRegistry)
                    .increment();
        }
        return -1;
    }
}