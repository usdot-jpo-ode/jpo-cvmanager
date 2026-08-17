package us.dot.its.jpo.ode.api.mappers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import us.dot.its.jpo.ode.api.fixtures.TestFixtures;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;
import us.dot.its.jpo.ode.api.models.postgres.tables.IntersectionOrganization;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import({ IntersectionMapperImpl.class, GeometryMapperImpl.class, INetMapperImpl.class })
class IntersectionMapperTest {

    private static final double DELTA = 0.0000001;
    private final TestFixtures testFixtures = new TestFixtures();

    @Autowired
    private IntersectionMapper mapper;

    private IntersectionOrganization orgEntry(String name) {
        Organization org = new Organization();
        org.setName(name);
        IntersectionOrganization io = new IntersectionOrganization();
        io.setOrganization(org);
        return io;
    }

    private IntersectionOrganization orgEntryNullOrg() {
        IntersectionOrganization io = new IntersectionOrganization();
        io.setOrganization(null);
        return io;
    }

    private IntersectionOrganization orgEntryNullName() {
        Organization org = new Organization();
        org.setName(null);
        IntersectionOrganization io = new IntersectionOrganization();
        io.setOrganization(org);
        return io;
    }

    @Nested
    @DisplayName("toDto(Intersection) — entity to DTO mapping")
    class ToDtoTests {

        @Test
        @DisplayName("null input returns null")
        void toDto_null_returnsNull() {
            assertNull(mapper.toDto(null));
        }

        @Test
        @DisplayName("full entity maps all fields correctly")
        void toDto_fullEntity_mapsAllFields() throws UnknownHostException {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setIntersectionName("Main St & 1st Ave");
            entity.setRefPt(testFixtures.createPoint(-104.9903, 39.7392));
            entity.setBbox(testFixtures.createBBox(39.73, -105.00, 39.74, -104.99));
            entity.setOriginIp(InetAddress.getByName("192.168.1.1"));
            entity.setIntersectionOrganizations(List.of(orgEntry("CDOT"), orgEntry("Denver")));

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertEquals(12109, result.getIntersectionId());
            assertEquals("Main St & 1st Ave", result.getIntersectionName());
            assertEquals("192.168.1.1", result.getOriginIp());

            assertNotNull(result.getRefPt());
            assertEquals(39.7392, result.getRefPt().getLatitude(), DELTA);
            assertEquals(-104.9903, result.getRefPt().getLongitude(), DELTA);

            assertNotNull(result.getBbox());
            assertEquals(39.73, result.getBbox().getLatitude1(), DELTA);
            assertEquals(-105.00, result.getBbox().getLongitude1(), DELTA);
            assertEquals(39.74, result.getBbox().getLatitude2(), DELTA);
            assertEquals(-104.99, result.getBbox().getLongitude2(), DELTA);

            assertEquals(List.of("CDOT", "Denver"), result.getOrganizations());
        }

        @Test
        @DisplayName("intersectionNumber maps to intersectionId")
        void toDto_intersectionNumber_mapsToIntersectionId() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("99999");

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertEquals(99999, result.getIntersectionId());
        }

        @Test
        @DisplayName("rsus field is always null — it is intentionally ignored")
        void toDto_rsusField_alwaysNull() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertNull(result.getRsus());
        }

        @Test
        @DisplayName("null refPt maps to null refPt in DTO")
        void toDto_nullRefPt_mapsToNull() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setRefPt(null);

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertNull(result.getRefPt());
        }

        @Test
        @DisplayName("null bbox maps to null bbox in DTO")
        void toDto_nullBbox_mapsToNull() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setBbox(null);

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertNull(result.getBbox());
        }

        @Test
        @DisplayName("null intersectionName maps to null in DTO")
        void toDto_nullIntersectionName_mapsToNull() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setIntersectionName(null);

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertNull(result.getIntersectionName());
        }

        @Test
        @DisplayName("null originIp maps to null in DTO")
        void toDto_nullOriginIp_mapsToNull() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setOriginIp(null);

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertNull(result.getOriginIp());
        }

        @Test
        @DisplayName("InetAddress originIp is serialized to dotted-decimal string")
        void toDto_originIp_serializedToString() throws UnknownHostException {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setOriginIp(InetAddress.getByName("10.0.0.1"));

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertEquals("10.0.0.1", result.getOriginIp());
        }

        @Test
        @DisplayName("null intersectionOrganizations maps to empty organizations list")
        void toDto_nullOrganizations_mapsToEmptyList() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setIntersectionOrganizations(null);

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertNotNull(result.getOrganizations());
            assertTrue(result.getOrganizations().isEmpty());
        }

        @Test
        @DisplayName("organizations with null org entries are filtered out")
        void toDto_organizationsWithNullOrg_filtered() {
            Intersection entity = new Intersection();
            entity.setIntersectionNumber("12109");
            entity.setIntersectionOrganizations(Arrays.asList(orgEntryNullOrg(), orgEntry("CDOT")));

            IntersectionDto result = mapper.toDto(entity);

            assertNotNull(result);
            assertEquals(List.of("CDOT"), result.getOrganizations());
        }
    }

    // -------------------------------------------------------------------------
    // mapOrgNames (default method, tested via the interface)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("mapOrgNames — IntersectionOrganization list to String list")
    class MapOrgNamesTests {

        @Test
        @DisplayName("null input returns empty list")
        void mapOrgNames_null_returnsEmptyList() {
            List<String> result = mapper.mapOrgNames(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("empty list returns empty list")
        void mapOrgNames_emptyList_returnsEmptyList() {
            List<String> result = mapper.mapOrgNames(Collections.emptyList());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("single valid entry returns list with that name")
        void mapOrgNames_singleValidEntry_returnsName() {
            List<String> result = mapper.mapOrgNames(List.of(orgEntry("CDOT")));

            assertEquals(List.of("CDOT"), result);
        }

        @Test
        @DisplayName("multiple valid entries returns all names in order")
        void mapOrgNames_multipleValidEntries_returnsAllNames() {
            List<IntersectionOrganization> orgs = List.of(
                    orgEntry("CDOT"), orgEntry("Denver"), orgEntry("USDOT"));

            List<String> result = mapper.mapOrgNames(orgs);

            assertEquals(List.of("CDOT", "Denver", "USDOT"), result);
        }

        @Test
        @DisplayName("entry with null Organization is filtered out")
        void mapOrgNames_nullOrganization_filtered() {
            List<IntersectionOrganization> orgs = Arrays.asList(orgEntryNullOrg(), orgEntry("CDOT"));

            List<String> result = mapper.mapOrgNames(orgs);

            assertEquals(List.of("CDOT"), result);
        }

        @Test
        @DisplayName("entry with null org name is filtered out")
        void mapOrgNames_nullOrgName_filtered() {
            List<IntersectionOrganization> orgs = Arrays.asList(orgEntryNullName(), orgEntry("Denver"));

            List<String> result = mapper.mapOrgNames(orgs);

            assertEquals(List.of("Denver"), result);
        }

        @Test
        @DisplayName("all invalid entries returns empty list")
        void mapOrgNames_allInvalidEntries_returnsEmptyList() {
            List<IntersectionOrganization> orgs = Arrays.asList(orgEntryNullOrg(), orgEntryNullName());

            List<String> result = mapper.mapOrgNames(orgs);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("mix of valid and invalid entries returns only valid names")
        void mapOrgNames_mixedEntries_returnsOnlyValid() {
            List<IntersectionOrganization> orgs = Arrays.asList(
                    orgEntry("CDOT"),
                    orgEntryNullOrg(),
                    orgEntry("Denver"),
                    orgEntryNullName(),
                    orgEntry("USDOT"));

            List<String> result = mapper.mapOrgNames(orgs);

            assertEquals(List.of("CDOT", "Denver", "USDOT"), result);
        }
    }
}
