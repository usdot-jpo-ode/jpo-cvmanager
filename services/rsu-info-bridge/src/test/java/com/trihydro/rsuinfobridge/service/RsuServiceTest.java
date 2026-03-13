package com.trihydro.rsuinfobridge.service;

import com.trihydro.rsuinfobridge.models.tables.Rsu;
import com.trihydro.rsuinfobridge.models.tables.RsuCredential;
import com.trihydro.rsuinfobridge.models.tables.RsuModel;
import com.trihydro.rsuinfobridge.models.tables.RsuOption;
import com.trihydro.rsuinfobridge.models.tables.SnmpCredential;
import com.trihydro.rsuinfobridge.models.tables.SnmpProtocol;
import com.trihydro.rsuinfobridge.testutil.repository.RsuCredentialRepository;
import com.trihydro.rsuinfobridge.testutil.repository.RsuIntersectionRepository;
import com.trihydro.rsuinfobridge.testutil.repository.RsuModelRepository;
import com.trihydro.rsuinfobridge.testutil.repository.RsuOptionRepository;
import com.trihydro.rsuinfobridge.testutil.repository.RsuOrganizationRepository;
import com.trihydro.rsuinfobridge.repository.RsuRepository;
import com.trihydro.rsuinfobridge.testutil.repository.SnmpCredentialRepository;
import com.trihydro.rsuinfobridge.testutil.repository.SnmpMsgfwdConfigRepository;
import com.trihydro.rsuinfobridge.testutil.repository.SnmpProtocolRepository;
import com.trihydro.rsuinfobridge.testutil.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RsuService using PostGIS Testcontainer.
 * Uses production schema (CVManager_CreateTables.sql) and sample data (CVManager_SampleData.sql).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
@Testcontainers
class RsuServiceTest {
    // IDs from CVManager_SampleData.sql
    static final int MODEL_ID = 1;
    static final int CREDENTIAL_ID = 1;
    static final int SNMP_CREDENTIAL_ID = 1;
    static final int SNMP_PROTOCOL_ID = 2;  // NTCIP 1218

    // SRID 4326 for WGS84
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    RsuIntersectionRepository rsuIntersectionRepository;

    @Autowired
    SnmpMsgfwdConfigRepository snmpMsgfwdConfigRepository;

    @Autowired
    RsuOrganizationRepository rsuOrganizationRepository;

    @Autowired
    RsuOptionRepository rsuOptionRepository;

    @Autowired
    RsuRepository rsuRepository;

    @Autowired
    RsuModelRepository rsuModelRepository;

    @Autowired
    RsuCredentialRepository rsuCredentialRepository;

    @Autowired
    SnmpCredentialRepository snmpCredentialRepository;

    @Autowired
    SnmpProtocolRepository snmpProtocolRepository;

    @Autowired
    private RsuService rsuService;

    @BeforeEach
    void setup() {
        clearRsuData();
    }

    @Test
    void testGetAll_returnsAllRsus() {
        // Arrange
        createRsuWithOptions("10.10.10.10", 1.0, "SN001", true);
        createRsuWithOptions("10.10.10.11", 2.0, "SN002", false);

        // Act & Assert
        assertEquals(2, rsuService.getAll(false).size());
    }

    @Test
    void testGetAll_withTimDepositEnabled_returnsOnlyEnabled() throws UnknownHostException {
        // Arrange
        createRsuWithOptions("10.10.10.10", 1.0, "SN001", true);
        createRsuWithOptions("10.10.10.11", 2.0, "SN002", false);

        // Act
        List<Rsu> result = rsuService.getAll(true);

        // Assert
        assertEquals(1, result.size());
        assertEquals(InetAddress.getByName("10.10.10.10"), result.getFirst().getIpv4Address());
    }

    @Test
    void testGetAll_withMultipleTimDepositEnabled_returnsAll() {
        // Arrange
        createRsuWithOptions("10.10.10.10", 1.0, "SN001", true);
        createRsuWithOptions("10.10.10.11", 2.0, "SN002", true);
        createRsuWithOptions("10.10.10.12", 3.0, "SN003", true);

        // Act & Assert
        assertEquals(3, rsuService.getAll(true).size());
    }

    @Test
    void testGetAll_returnsCorrectFields() throws UnknownHostException {
        // Arrange
        createRsuWithOptions("10.10.10.10", 5.5, "SN-ABC", "SCMS-XYZ", "I-70", true, false);

        // Act
        List<Rsu> result = rsuService.getAll(false);

        // Assert
        assertEquals(1, result.size());
        Rsu rsu = result.getFirst();
        assertEquals(InetAddress.getByName("10.10.10.10"), rsu.getIpv4Address());
        assertEquals(5.5, rsu.getMilepost());
        assertEquals("SN-ABC", rsu.getSerialNumber());
        assertEquals("SCMS-XYZ", rsu.getIssScmsId());
        assertEquals("I-70", rsu.getPrimaryRoute());
        assertEquals("1218", rsu.getSnmpProtocol().getProtocolCode());
        assertEquals("username", rsu.getSnmpCredential().getUsername());
        assertTrue(rsu.getRsuOption().getTimDeposit());
        assertFalse(rsu.getRsuOption().getSnmpMonitoring());
    }

    @Test
    void testGetAll_emptyDatabase_returnsEmptyList() {
        // Act & Assert
        assertTrue(rsuService.getAll(false).isEmpty());
        assertTrue(rsuService.getAll(true).isEmpty());
    }

    @Test
    void testGetAll_noneWithTimDepositEnabled_returnsEmptyList() {
        // Arrange
        createRsuWithOptions("10.10.10.10", 1.0, "SN001", false);
        createRsuWithOptions("10.10.10.11", 2.0, "SN002", false);

        // Act & Assert
        assertTrue(rsuService.getAll(true).isEmpty());
    }

    @Test
    void testGetAll_timDepositFalse_returnsAll() {
        // Arrange
        createRsuWithOptions("10.10.10.10", 1.0, "SN001", true);
        createRsuWithOptions("10.10.10.11", 2.0, "SN002", false);

        // Act & Assert
        assertEquals(2, rsuService.getAll(false).size());
    }

    /**
     * Clears RSU data while preserving prerequisites from sample data.
     */
    void clearRsuData() {
        rsuIntersectionRepository.deleteAll();
        snmpMsgfwdConfigRepository.deleteAll();
        rsuOrganizationRepository.deleteAll();
        rsuOptionRepository.deleteAll();
        rsuRepository.deleteAll();
    }

    /**
     * Creates an RSU with options using sample data prerequisites.
     */
    void createRsuWithOptions(String ipv4Address, double milepost, String serialNumber, boolean timDeposit) {
        createRsuWithOptions(ipv4Address, milepost, serialNumber,
                "SCMS-" + serialNumber, "Route-" + serialNumber, timDeposit, false);
    }

    /**
     * Creates an RSU with options using full control over fields.
     */
    void createRsuWithOptions(String ipv4Address, double milepost, String serialNumber,
                                     String issScmsId, String primaryRoute,
                                     boolean timDeposit, boolean snmpMonitoring) {
        try {
            // Fetch reference entities from sample data
            RsuModel model = rsuModelRepository.getReferenceById(MODEL_ID);
            RsuCredential credential = rsuCredentialRepository.getReferenceById(CREDENTIAL_ID);
            SnmpCredential snmpCredential = snmpCredentialRepository.getReferenceById(SNMP_CREDENTIAL_ID);
            SnmpProtocol snmpProtocol = snmpProtocolRepository.getReferenceById(SNMP_PROTOCOL_ID);

            // Create geography point at origin (0, 0)
            Point geography = GEOMETRY_FACTORY.createPoint(new Coordinate(0, 0));

            // Build and save RSU
            Rsu rsu = Rsu.builder()
                    .geography(geography)
                    .milepost(milepost)
                    .ipv4Address(InetAddress.getByName(ipv4Address))
                    .serialNumber(serialNumber)
                    .issScmsId(issScmsId)
                    .primaryRoute(primaryRoute)
                    .model(model)
                    .credential(credential)
                    .snmpCredential(snmpCredential)
                    .snmpProtocol(snmpProtocol)
                    .build();

            Rsu savedRsu = rsuRepository.save(rsu);

            // Create and save RSU options
            RsuOption rsuOption = new RsuOption();
            rsuOption.setRsu(savedRsu);
            rsuOption.setTimDeposit(timDeposit);
            rsuOption.setSnmpMonitoring(snmpMonitoring);

            RsuOption savedRsuOption = rsuOptionRepository.save(rsuOption);

            // Link the option back to the RSU for bidirectional relationship
            savedRsu.setRsuOption(savedRsuOption);
            rsuRepository.save(savedRsu);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Invalid IP address: " + ipv4Address, e);
        }
    }
}
