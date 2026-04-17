package us.dot.its.jpo.ode.api.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import us.dot.its.jpo.ode.api.fixtures.TestFixtures;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.models.admin.intersection.AllowedSelections;
import us.dot.its.jpo.ode.api.models.admin.intersection.Bbox;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionListResponse;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionPatch;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionSingleResponse;
import us.dot.its.jpo.ode.api.models.admin.intersection.RefPt;
import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;
import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuCredential;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuModel;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpCredential;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpProtocol;
import us.dot.its.jpo.ode.api.repositories.IntersectionOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.IntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.ManufacturerRepository;
import us.dot.its.jpo.ode.api.repositories.OrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.RsuIntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuModelRepository;
import us.dot.its.jpo.ode.api.repositories.RsuOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpProtocolRepository;

import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
class AdminIntersectionServiceTest {
  @Autowired
  private AdminIntersectionService adminIntersectionService;

  private final TestFixtures fixtures = new TestFixtures();

  @Autowired
  private IntersectionRepository intersectionRepository;

  @Autowired
  private IntersectionOrganizationRepository intersectionOrganizationRepository;

  @Autowired
  private RsuIntersectionRepository rsuIntersectionRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private RsuRepository rsuRepository;

  @Autowired
  private RsuOrganizationRepository rsuOrganizationRepository;

  @Autowired
  private RsuCredentialRepository rsuCredentialRepository;

  @Autowired
  private SnmpCredentialRepository snmpCredentialRepository;

  @Autowired
  private SnmpProtocolRepository snmpProtocolRepository;

  @Autowired
  private RsuModelRepository rsuModelRepository;

  @Autowired
  private ManufacturerRepository manufacturerRepository;

  /**
   * Clears all relevant tables in reverse FK-dependency order before each test so that
   * tests never see each other's data and hardcoded identifiers ("1123", "1000", etc.)
   * never produce duplicate-key violations.
   *
   * Deletion order (leaf tables first):
   *   rsu_intersection → intersection_organization → rsu_organization
   *   → rsus → rsu_credentials → snmp_credentials → snmp_protocols → rsu_models
   *   → intersections → organizations
   *
   * Note: manufacturer rows are left as orphans (no unique constraint in test data).
   */
  @BeforeEach
  void clearDatabase() {
    rsuIntersectionRepository.deleteAll();
    intersectionOrganizationRepository.deleteAll();
    rsuOrganizationRepository.deleteAll();
    rsuRepository.deleteAll();
    rsuCredentialRepository.deleteAll();
    snmpCredentialRepository.deleteAll();
    snmpProtocolRepository.deleteAll();
    rsuModelRepository.deleteAll();
    intersectionRepository.deleteAll();
    organizationRepository.deleteAll();
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private void setUpSuperuserContext() {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .claim("sub", "superuser")
        .claim("preferred_username", "superuser")
        .claim("cvmanager_data", Map.of("super_user", "1", "organizations", List.of()))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
    SecurityContextHolder.getContext().setAuthentication(
        new CvManagerAuthToken(jwt, List.of(), "superuser"));
  }

  private void setUpOperatorContext(String orgName) {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .claim("sub", "operator")
        .claim("preferred_username", "operator")
        .claim("cvmanager_data", Map.of(
            "super_user", "0",
            "organizations", List.of(Map.of("org", orgName, "role", "OPERATOR"))
        ))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
    SecurityContextHolder.getContext().setAuthentication(
        new CvManagerAuthToken(jwt, List.of(), "operator"));
  }

  @Nested
  class GetAllIntersections {

    @Test
    void withOrg_returnsScopedIntersections() {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      String orgName = org.getName();
      Intersection i = intersectionRepository.save(fixtures.createIntersection("1123"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, org));


      IntersectionListResponse result = adminIntersectionService.getAllIntersections(orgName);

      assertNotNull(result.getIntersectionData());
      assertEquals(1, result.getIntersectionData().size());
      assertEquals(1123, result.getIntersectionData().getFirst().getIntersectionId());
    }

    @Test
    void withOrg_excludesIntersectionsFromOtherOrgs() {
      Organization orgA = organizationRepository.save(fixtures.createRandomOrg());
      Organization orgB = organizationRepository.save(fixtures.createRandomOrg());
      Intersection i1 = intersectionRepository.save(fixtures.createIntersection("1001"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i1, orgA));
      Intersection i2 = intersectionRepository.save(fixtures.createIntersection("1002"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i2, orgB));


      IntersectionListResponse result = adminIntersectionService.getAllIntersections(orgA.getName());

      assertEquals(1, result.getIntersectionData().size());
      assertEquals(1001, result.getIntersectionData().getFirst().getIntersectionId());
    }

    @Test
    void noIntersectionsForOrg_returnsEmptyList() {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      intersectionRepository.save(fixtures.createIntersection("1123")); // no org association

      IntersectionListResponse result = adminIntersectionService.getAllIntersections(org.getName());

      assertNotNull(result.getIntersectionData());
      assertTrue(result.getIntersectionData().isEmpty());
    }

    @Test
    void attachesRsuIpsToCorrectIntersection() throws UnknownHostException {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      String orgName = org.getName();
      Intersection i = intersectionRepository.save(fixtures.createIntersection("1123"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, org));
      Rsu rsu = saveRsu("192.168.1.1", org);
      rsuIntersectionRepository.save(fixtures.createRsuIntersection(rsu, i));


      IntersectionListResponse result = adminIntersectionService.getAllIntersections(orgName);

      assertEquals(1, result.getIntersectionData().size());
      assertEquals(List.of("192.168.1.1"), result.getIntersectionData().getFirst().getRsus());
    }
  }

  @Nested
  class GetIntersection {

    @Test
    void notFound_throwsEntityNotFoundException() {
      organizationRepository.save(fixtures.createRandomOrg());

      assertThrows(EntityNotFoundException.class, () ->  adminIntersectionService.getIntersection(Integer.MAX_VALUE),
        "Should throw EntityNotFoundException for non-existent intersection");
    }

    @Test
    void foundAsSuperuser_returnsFullDataWithAllOrgs() throws UnknownHostException {
      setUpSuperuserContext();
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      String orgName = org.getName();
      Intersection i = intersectionRepository.save(fixtures.createIntersection("1123"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, org));
      Rsu rsu = saveRsu("192.168.1.1", org);
      rsuIntersectionRepository.save(fixtures.createRsuIntersection(rsu, i));


      IntersectionSingleResponse result = adminIntersectionService.getIntersection(1123);

      assertEquals(1123, result.getIntersectionDto().getIntersectionId());
      assertNotNull(result.getAllowedSelections());
      assertTrue(result.getAllowedSelections().getOrganizations().contains(orgName));
      assertEquals(List.of("192.168.1.1"), result.getIntersectionDto().getRsus());
    }

    @Test
    void found_singleOrg_returnsOrgInDto() {
      setUpSuperuserContext();
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      String orgName = org.getName();
      Intersection i = intersectionRepository.save(fixtures.createIntersection("1123"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, org));


      IntersectionSingleResponse result = adminIntersectionService.getIntersection(1123);

      assertEquals(1123, result.getIntersectionDto().getIntersectionId());
      assertEquals(List.of(orgName), result.getIntersectionDto().getOrganizations());
    }

    @Test
    void intersectionWithMultipleOrgs_returnsAllAssignedOrgsInDto() {
      setUpSuperuserContext();
      Organization orgA = organizationRepository.save(fixtures.createRandomOrg());
      Organization orgB = organizationRepository.save(fixtures.createRandomOrg());
      String orgAName = orgA.getName();
      String orgBName = orgB.getName();

      Intersection i = intersectionRepository.save(fixtures.createIntersection("1123"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, orgA));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, orgB));


      IntersectionSingleResponse result = adminIntersectionService.getIntersection(1123);

      assertEquals(1123, result.getIntersectionDto().getIntersectionId());
      assertEquals(2, result.getIntersectionDto().getOrganizations().size());
      assertTrue(result.getIntersectionDto().getOrganizations().containsAll(List.of(orgAName, orgBName)));
    }

    @Test
    void nonSuperuser_allowedSelectionsUsesOperatorOrgs() throws UnknownHostException {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      String orgName = org.getName();
      setUpOperatorContext(orgName);
      Intersection i = intersectionRepository.save(fixtures.createIntersection("1123"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, org));
      Rsu rsu = saveRsu("10.0.0.1", org);
      rsuOrganizationRepository.save(fixtures.createRsuOrganization(rsu, org));


      IntersectionSingleResponse result = adminIntersectionService.getIntersection(1123);

      AllowedSelections allowed = result.getAllowedSelections();
      assertEquals(List.of(orgName), allowed.getOrganizations());
      assertEquals(List.of("10.0.0.1"), allowed.getRsus());
    }
  }

  @Nested
  class PatchIntersection {

    @Test
    void basicUpdate_renumbersIntersection() {
      intersectionRepository.save(fixtures.createIntersection("1000"));


      IntersectionPatch patch = new IntersectionPatch(
        1000, 1001, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);

      assertTrue(intersectionRepository.findByIntersectionNumber("1001").isPresent());
      assertFalse(intersectionRepository.findByIntersectionNumber("1000").isPresent());
    }

    @Test
    void withOptionalFields_updatesNonNullFields() {
      intersectionRepository.save(fixtures.createIntersection("1000"));


      Bbox bbox = new Bbox(39.9, -105.2, 40.1, -105.0);
      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), bbox, "Main St", null,
        Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);


      Intersection updated = intersectionRepository.findByIntersectionNumber("1000").orElseThrow();
      assertEquals("Main St", updated.getIntersectionName());
      assertNotNull(updated.getBbox());
    }

    @Test
    void nullOptionalFields_preservesExistingValues() {
      Intersection existing = intersectionRepository.save(fixtures.createIntersection("1000"));
      existing.setIntersectionName("Existing Name");
      intersectionRepository.save(existing);


      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);


      Intersection updated = intersectionRepository.findByIntersectionNumber("1000").orElseThrow();
      assertEquals("Existing Name", updated.getIntersectionName());
    }

    @Test
    void orgsToAdd_createsAssociations() {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      String orgName = org.getName();
      intersectionRepository.save(fixtures.createIntersection("1000"));


      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), null, null, null,
        List.of(orgName), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);


      List<Intersection> result = intersectionRepository.findAllByOrgNameWithOrgs(orgName);
      assertEquals(1, result.size());
      assertEquals("1000", result.getFirst().getIntersectionNumber());
    }

    @Test
    void orgsToRemove_deletesAssociations() {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      String orgName = org.getName();
      Intersection i = intersectionRepository.save(fixtures.createIntersection("1000"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, org));


      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), List.of(orgName),
        Collections.emptyList(), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);


      assertTrue(intersectionRepository.findAllByOrgNameWithOrgs(orgName).isEmpty());
    }

    @Test
    void rsusToAdd_createsAssociations() throws UnknownHostException {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      intersectionRepository.save(fixtures.createIntersection("1000"));
      saveRsu("192.168.1.1", org);


      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), Collections.emptyList(),
        List.of("192.168.1.1"), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);


      assertEquals(1, rsuIntersectionRepository.findRsuIpsByIntersectionNumber(1000).size());
    }

    @Test
    void rsusToRemove_deletesAssociations() throws UnknownHostException {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      Intersection intersection = intersectionRepository.save(fixtures.createIntersection("1000"));
      Rsu rsu = saveRsu("192.168.1.1", org);
      rsuIntersectionRepository.save(fixtures.createRsuIntersection(rsu, intersection));


      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), List.of("192.168.1.1"));

      adminIntersectionService.patchIntersection(patch);


      assertTrue(rsuIntersectionRepository.findRsuIpsByIntersectionNumber(1000).isEmpty());
    }

    @Test
    void intersectionNotFound_throws404() {
      IntersectionPatch patch = new IntersectionPatch(
        9999, 9999, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());

      ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> adminIntersectionService.patchIntersection(patch));
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void rsusToAdd_alreadyAssociated_doesNotCreateDuplicate() throws UnknownHostException {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      Intersection intersection = intersectionRepository.save(fixtures.createIntersection("1000"));
      Rsu rsu = saveRsu("192.168.1.1", org);
      rsuIntersectionRepository.save(fixtures.createRsuIntersection(rsu, intersection));

      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), Collections.emptyList(),
        List.of("192.168.1.1"), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);

      assertEquals(1, rsuIntersectionRepository.findAll().size());
    }

    @Test
    void emptyRelationshipLists_noAssociations_createdOrRemoved() {
      intersectionRepository.save(fixtures.createIntersection("1000"));


      IntersectionPatch patch = new IntersectionPatch(
        1000, 1000, new RefPt(40.0, -105.0), null, null, null,
        Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());

      adminIntersectionService.patchIntersection(patch);


      assertTrue(intersectionOrganizationRepository.findAll().isEmpty());
      assertTrue(rsuIntersectionRepository.findAll().isEmpty());
    }
  }

  @Nested
  class DeleteIntersection {

    @Test
    void existingIntersection_deletesRelationshipsAndReturnsMessage() throws UnknownHostException {
      Organization org = organizationRepository.save(fixtures.createRandomOrg());
      Intersection i = intersectionRepository.save(fixtures.createIntersection("1123"));
      intersectionOrganizationRepository.save(fixtures.createIntersectionOrganization(i, org));
      Rsu rsu = saveRsu("192.168.1.1", org);
      rsuIntersectionRepository.save(fixtures.createRsuIntersection(rsu, i));


      adminIntersectionService.deleteIntersection("1123");

      assertFalse(intersectionRepository.findByIntersectionNumber("1123").isPresent());
      assertTrue(rsuIntersectionRepository.findRsuIpsByIntersectionNumber(1123).isEmpty());
      assertTrue(intersectionOrganizationRepository.findAll().isEmpty());
    }

    @Test
    void notFound_throws404() {
      ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> adminIntersectionService.deleteIntersection("9999"));
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
  }

  private Rsu saveRsu(String ip, Organization org) throws UnknownHostException {
    Manufacturer mfr = manufacturerRepository.save(fixtures.createRandomManufacturer());
    RsuModel model = rsuModelRepository.save(fixtures.createRandomRsuModel(mfr));
    RsuCredential cred = rsuCredentialRepository.save(fixtures.createRandomRsuCredential(org));
    SnmpCredential snmpCred = snmpCredentialRepository.save(fixtures.createRandomSnmpCredential(org));
    SnmpProtocol proto = snmpProtocolRepository.save(fixtures.createRandomSnmpProtocol());
    return rsuRepository.save(fixtures.createRsu(ip, model, cred, snmpCred, proto));
  }

}
