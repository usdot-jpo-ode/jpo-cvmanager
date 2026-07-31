package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.fixtures.TestFixtures;
import us.dot.its.jpo.ode.api.models.postgres.tables.*;
import us.dot.its.jpo.ode.api.repositories.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
@Transactional
class ScmsHealthServiceTest {

    @Autowired
    private ScmsHealthService scmsHealthService;

    @Autowired
    private ScmsHealthRepository scmsHealthRepository;

    @Autowired
    private RsuRepository rsuRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RsuOrganizationRepository rsuOrganizationRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private RsuModelRepository rsuModelRepository;

    @Autowired
    private RsuCredentialRepository rsuCredentialRepository;

    @Autowired
    private SnmpCredentialRepository snmpCredentialRepository;

    @Autowired
    private SnmpProtocolRepository snmpProtocolRepository;

    private final TestFixtures fixtures = new TestFixtures();

    @BeforeEach
    void setUp() {
        // Delete in reverse FK-dependency order: child tables before parents.
        // organizations must be last because rsu_credentials and snmp_credentials
        // have NOT NULL FKs to it.
        scmsHealthRepository.deleteAll();
        rsuOrganizationRepository.deleteAll();
        rsuRepository.deleteAll();
        rsuCredentialRepository.deleteAll();
        snmpCredentialRepository.deleteAll();
        snmpProtocolRepository.deleteAll();
        rsuModelRepository.deleteAll();
        manufacturerRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @DisplayName("Returns latest health status for each RSU in organization")
    void testGetScmsStatuses_ReturnsLatestForEachRsuInOrganization() throws Exception {
        // Arrange
        Organization org1 = organizationRepository.save(fixtures.createOrg("Org1"));
        Organization org2 = organizationRepository.save(fixtures.createOrg("Org2"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCred = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org1));
        RsuCredential rsuCred = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org1));

        Rsu rsu1 = rsuRepository.save(fixtures.createRsu("10.0.0.1", model, rsuCred, snmpCred, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu1, org1));
        Rsu rsu2 = rsuRepository.save(fixtures.createRsu("10.0.0.2", model, rsuCred, snmpCred, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu2, org1));
        Rsu rsu3 = rsuRepository.save(fixtures.createRsu("10.0.0.3", model, rsuCred, snmpCred, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu3, org2));

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant oneHourEarlier = now.minus(1, ChronoUnit.HOURS);

        // SCMS Health records
        saveScmsHealth(rsu1, oneHourEarlier, true);
        ScmsHealth rsu1Latest = saveScmsHealth(rsu1, now, false);

        // Single health record for RSU 2
        ScmsHealth rsu2Latest = saveScmsHealth(rsu2, now, true);

        // Health record for RSU 3 (Org 2)
        saveScmsHealth(rsu3, now, true);

        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("Org1");

        // Assert
        assertEquals(2, results.size(), "Should return 2 records for Org1");
        
        ScmsHealthRsuProjection result1 = results.stream()
                .filter(res -> res.getIpv4Address().getHostAddress().equals("10.0.0.1"))
                .findFirst().orElseThrow();
        assertEquals(rsu1Latest.getHealth(), result1.getHealth());

        ScmsHealthRsuProjection result2 = results.stream()
                .filter(res -> res.getIpv4Address().getHostAddress().equals("10.0.0.2"))
                .findFirst().orElseThrow();
        assertEquals(rsu2Latest.getHealth(), result2.getHealth());
    }

    @Test
    @DisplayName("Returns empty list when organization has no RSUs")
    void testGetScmsStatuses_ReturnsEmpty_WhenOrganizationHasNoRsus() {
        // Arrange
        organizationRepository.save(fixtures.createOrg("EmptyOrg"));

        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("EmptyOrg");

        // Assert
        assertTrue(results.isEmpty(), "Should return an empty list for an organization with no RSUs");
    }

    @Test
    @DisplayName("When an organization has RSUs but no health records, the health fields are null")
    void testGetScmsStatuses_ReturnsNullHealth_WhenOrganizationHasRsusButNoHealthRecords() throws Exception {
        // Arrange
        Organization org = organizationRepository.save(fixtures.createOrg("NoHealthOrg"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCred = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org));
        RsuCredential rsuCred = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org));

        Rsu rsu = rsuRepository.save(fixtures.createRsu("10.0.0.10", model, rsuCred, snmpCred, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu, org));

        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("NoHealthOrg");

        // Assert
        assertEquals(1, results.size(), "Should return 1 entry even when RSU has no health records");
        assertNull(results.getFirst().getHealth(), "Health should be null when the RSU has no health records");
        assertNull(results.getFirst().getExpiration(), "Expiration should be null when the RSU has no health records");
    }

    @Test
    @DisplayName("Returns empty list when organization does not exist")
    void testGetScmsStatuses_ReturnsEmpty_WhenOrganizationDoesNotExist() {
        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("NonExistentOrg");

        // Assert
        assertTrue(results.isEmpty(), "Should return an empty list for a non-existent organization");
    }

    @Test
    @DisplayName("Given two records with the same timestamp, only one row is returned")
    void testGetScmsStatuses_ReturnsExactlyOneRowPerRsu_WhenMultipleRecordsHaveSameTimestamp() throws Exception {
        // Arrange
        Organization org = organizationRepository.save(fixtures.createOrg("SameTimestampOrg"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCred = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org));
        RsuCredential rsuCred = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org));

        Rsu rsu = rsuRepository.save(fixtures.createRsu("10.0.0.20", model, rsuCred, snmpCred, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu, org));

        Instant sameTime = Instant.now().truncatedTo(ChronoUnit.MICROS);

        // Create multiple health records with identical timestamps.
        // The query must return exactly one row per RSU (matching legacy ROW_NUMBER behavior).
        saveScmsHealth(rsu, sameTime, true);
        saveScmsHealth(rsu, sameTime, false);

        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("SameTimestampOrg");

        // Assert
        assertEquals(1, results.size(),
            "Should return exactly one record per RSU even when multiple records have the same timestamp");

        ScmsHealthRsuProjection result = results.getFirst();
        assertNotNull(result.getHealth(), "Health should not be null");
    }

    @Test
    @DisplayName("Given many tied timestamps, exactly one row is returned")
    void testGetScmsStatuses_ReturnsExactlyOneRowPerRsu_WhenMoreThanTwoRecordsHaveSameTimestamp() throws Exception {
        // Arrange
        Organization org = organizationRepository.save(fixtures.createOrg("ManyTiesOrg"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCredential = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org));
        RsuCredential rsuCredential = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org));

        Rsu rsu = rsuRepository.save(fixtures.createRsu("10.0.0.30", model, rsuCredential, snmpCredential, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu, org));

        Instant sameTime = Instant.now().truncatedTo(ChronoUnit.MICROS);

        // Create 5 health records with identical timestamps
        saveScmsHealth(rsu, sameTime, true);
        saveScmsHealth(rsu, sameTime, false);
        saveScmsHealth(rsu, sameTime, true);
        saveScmsHealth(rsu, sameTime, false);
        saveScmsHealth(rsu, sameTime, true);

        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("ManyTiesOrg");

        // Assert
        assertEquals(1, results.size(),
            "Should return exactly one record per RSU even with many timestamp ties");
    }

    @Test
    @DisplayName("Results ordered by IPv4 address")
    void testGetScmsStatuses_ResultsOrderedByIpv4Address() throws Exception {
        // Arrange
        Organization org = organizationRepository.save(fixtures.createOrg("OrderedOrg"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCredential = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org));
        RsuCredential rsuCredential = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org));

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        // Create RSUs in non-sorted order
        Rsu rsu3 = rsuRepository.save(fixtures.createRsu("10.0.0.103", model, rsuCredential, snmpCredential, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu3, org));
        Rsu rsu1 = rsuRepository.save(fixtures.createRsu("10.0.0.101", model, rsuCredential, snmpCredential, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu1, org));
        Rsu rsu2 = rsuRepository.save(fixtures.createRsu("10.0.0.102", model, rsuCredential, snmpCredential, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu2, org));

        saveScmsHealth(rsu3, now, true);
        saveScmsHealth(rsu1, now, true);
        saveScmsHealth(rsu2, now, true);

        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("OrderedOrg");

        // Assert - Results should be sorted by IPv4 address
        assertEquals(3, results.size());
        assertEquals("10.0.0.101", results.get(0).getIpv4Address().getHostAddress(),
            "First result should be 10.0.0.101");
        assertEquals("10.0.0.102", results.get(1).getIpv4Address().getHostAddress(),
            "Second result should be 10.0.0.102");
        assertEquals("10.0.0.103", results.get(2).getIpv4Address().getHostAddress(),
            "Third result should be 10.0.0.103");
    }

    @Test
    @DisplayName("RSU in multiple organizations appears in each organization's query")
    void testGetScmsStatuses_RsuInMultipleOrganizations_AppearsInEachOrgQuery() throws Exception {
        // Arrange - An RSU can belong to multiple organizations
        Organization org1 = organizationRepository.save(fixtures.createOrg("MultiOrg1"));
        Organization org2 = organizationRepository.save(fixtures.createOrg("MultiOrg2"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCredential = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org1));
        RsuCredential rsuCredential = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org1));

        // Create RSU and associate with both organizations
        Rsu sharedRsu = rsuRepository.save(fixtures.createRsu("10.0.0.80", model, rsuCredential, snmpCredential, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(sharedRsu, org1));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(sharedRsu, org2));

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        ScmsHealth healthRecord = saveScmsHealth(sharedRsu, now, true);

        // Act
        List<ScmsHealthRsuProjection> resultsOrg1 = scmsHealthService.getScmsStatuses("MultiOrg1");
        List<ScmsHealthRsuProjection> resultsOrg2 = scmsHealthService.getScmsStatuses("MultiOrg2");

        // Assert - RSU should appear in both organization queries
        assertEquals(1, resultsOrg1.size(), "RSU should appear in MultiOrg1 results");
        assertEquals(1, resultsOrg2.size(), "RSU should appear in MultiOrg2 results");
        assertEquals(healthRecord.getHealth(), resultsOrg1.getFirst().getHealth());
        assertEquals(healthRecord.getHealth(), resultsOrg2.getFirst().getHealth());
    }

    @Test
    @DisplayName("RSUs without health records are included")
    void testGetScmsStatuses_MixedRsusWithAndWithoutHealthRecords() throws Exception {
        // Arrange
        Organization org = organizationRepository.save(fixtures.createOrg("MixedOrg"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCred = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org));
        RsuCredential rsuCred = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org));

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        // RSU with health records
        Rsu rsuWithHealth = rsuRepository.save(fixtures.createRsu("10.0.0.60", model, rsuCred, snmpCred, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsuWithHealth, org));
        ScmsHealth healthRecord = saveScmsHealth(rsuWithHealth, now, true);

        // RSU without health records
        Rsu rsuWithoutHealth = rsuRepository.save(fixtures.createRsu("10.0.0.61", model, rsuCred, snmpCred, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsuWithoutHealth, org));

        // Act
        List<ScmsHealthRsuProjection> results = scmsHealthService.getScmsStatuses("MixedOrg");

        // Assert - Both RSUs should be returned, sorted by IP
        assertEquals(2, results.size(), "Should return both RSUs");

        // First RSU (10.0.0.60) has health record
        assertEquals("10.0.0.60", results.getFirst().getIpv4Address().getHostAddress());
        assertNotNull(results.get(0).getHealth(), "First RSU should have health record");
        assertEquals(healthRecord.getHealth(), results.get(0).getHealth());

        // Second RSU (10.0.0.61) has no health record
        assertEquals("10.0.0.61", results.get(1).getIpv4Address().getHostAddress());
        assertNull(results.get(1).getHealth(), "Second RSU should have null health");
    }

    @Test
    @DisplayName("Query returns deterministic results on repeated calls")
    void testGetScmsStatuses_DeterministicResults_MultipleCallsReturnSameOrder() throws Exception {
        // Arrange
        Organization org = organizationRepository.save(fixtures.createOrg("DeterministicOrg"));

        Manufacturer manufacturer = manufacturerRepository.save(fixtures.createRandomManufacturer());
        RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(manufacturer));
        SnmpProtocol protocol = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
        SnmpCredential snmpCredential = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org));
        RsuCredential rsuCredential = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org));

        Instant sameTime = Instant.now().truncatedTo(ChronoUnit.MICROS);

        Rsu rsu1 = rsuRepository.save(fixtures.createRsu("10.0.0.70", model, rsuCredential, snmpCredential, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu1, org));
        Rsu rsu2 = rsuRepository.save(fixtures.createRsu("10.0.0.71", model, rsuCredential, snmpCredential, protocol));
        rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu2, org));

        // Create multiple records with same timestamp for each RSU
        saveScmsHealth(rsu1, sameTime, true);
        saveScmsHealth(rsu1, sameTime, false);
        saveScmsHealth(rsu2, sameTime, false);
        saveScmsHealth(rsu2, sameTime, true);

        // Act - Call multiple times
        List<ScmsHealthRsuProjection> results1 = scmsHealthService.getScmsStatuses("DeterministicOrg");
        List<ScmsHealthRsuProjection> results2 = scmsHealthService.getScmsStatuses("DeterministicOrg");
        List<ScmsHealthRsuProjection> results3 = scmsHealthService.getScmsStatuses("DeterministicOrg");

        // Assert - All calls should return identical results
        assertEquals(2, results1.size());
        assertEquals(2, results2.size());
        assertEquals(2, results3.size());

        // Verify same health values are returned each time
        assertEquals(results1.get(0).getHealth(), results2.get(0).getHealth());
        assertEquals(results1.get(0).getHealth(), results3.get(0).getHealth());
        assertEquals(results1.get(1).getHealth(), results2.get(1).getHealth());
        assertEquals(results1.get(1).getHealth(), results3.get(1).getHealth());
    }


    private ScmsHealth saveScmsHealth(Rsu rsu, Instant timestamp, boolean health) {
        ScmsHealth scmsHealth = new ScmsHealth();
        scmsHealth.setRsu(rsu);
        scmsHealth.setTimestamp(timestamp);
        scmsHealth.setHealth(health);
        scmsHealth.setExpiration(timestamp.plus(30, ChronoUnit.DAYS));
        return scmsHealthRepository.save(scmsHealth);
    }
}