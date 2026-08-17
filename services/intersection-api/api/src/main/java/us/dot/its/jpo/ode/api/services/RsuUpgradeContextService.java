package us.dot.its.jpo.ode.api.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

@Service
@RequiredArgsConstructor
public class RsuUpgradeContextService {

    private final RsuRepository rsuRepository;

    public boolean hasCompleteRsuData(String rsuIp) {
        return findRsuByIp(rsuIp) != null;
    }

    public Rsu findRsuByIp(String rsuIp) {
        InetAddress inetAddress = parseIpv4Address(rsuIp);
        return rsuRepository.findByIpv4Address(inetAddress);
    }

    private InetAddress parseIpv4Address(String rsuIp) {
        try {
            return InetAddress.getByName(rsuIp);
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSU IP address: " + rsuIp, e);
        }
    }
}
