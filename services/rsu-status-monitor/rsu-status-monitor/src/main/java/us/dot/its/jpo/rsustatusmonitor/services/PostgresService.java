package us.dot.its.jpo.rsustatusmonitor.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.stereotype.Service;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;

@Service
public class PostgresService {

    @PersistenceContext
    private EntityManager entityManager;

    // Finds the RSU SNMP credentials for all RSUs, includes the intersection ID if
    // available
    private final String findRsuSnmpCredentials = "SELECT new us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials( "
            +
            "rsu.rsu_id, rsu.ipv4_address, snmp_creds.username, snmp_creds.password, snmp_creds.encrypt_password, snmp_proto.protocol_code, i.intersection_number) "
            +
            "FROM Rsus rsu " +
            "JOIN SnmpCredentials snmp_creds ON rsu.snmp_credential_id = snmp_creds.snmp_credential_id " +
            "JOIN SnmpProtocols snmp_proto ON rsu.snmp_protocol_id = snmp_proto.snmp_protocol_id " +
            "LEFT JOIN RsuIntersection ri ON rsu.rsu_id = ri.rsu_id " +
            "LEFT JOIN Intersections i ON ri.intersection_id = i.intersection_id";

    public List<RsuSnmpCredentials> getRsusWithCredentials() {
        TypedQuery<RsuSnmpCredentials> query = entityManager.createQuery(findRsuSnmpCredentials,
                RsuSnmpCredentials.class);
        return query.getResultList();
    }
}
