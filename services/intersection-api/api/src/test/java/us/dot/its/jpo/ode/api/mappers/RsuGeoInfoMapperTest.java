package us.dot.its.jpo.ode.api.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoPropertiesDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuModel;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOption;

@ExtendWith(SpringExtension.class)
@Import({ RsuGeoInfoMapperImpl.class, INetMapperImpl.class })
public class RsuGeoInfoMapperTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double DELTA = 0.0000001;

    @Autowired
    private RsuGeoInfoMapper mapper;

    private Rsu buildRsu(boolean withOption) throws UnknownHostException {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName("Commsignia");

        RsuModel model = new RsuModel();
        model.setName("ITS-RS4-M");
        model.setManufacturer(manufacturer);

        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(-104.9903, 39.7392));

        Rsu rsu = new Rsu();
        rsu.setId(42);
        rsu.setMilepost(12.5);
        rsu.setIpv4Address(InetAddress.getByName("192.168.10.5"));
        rsu.setSerialNumber("SERIAL-001");
        rsu.setPrimaryRoute("I-25");
        rsu.setModel(model);
        rsu.setGeography(point);

        if (withOption) {
            RsuOption option = new RsuOption();
            option.setTimDeposit(true);
            option.setSnmpMonitoring(false);
            rsu.setRsuOption(option);
        }

        return rsu;
    }

    @Nested
    @DisplayName("toDto(Rsu) - entity to GeoJSON DTO")
    class ToDtoTests {

        @Test
        @DisplayName("null input returns null")
        void toDto_null_returnsNull() {
            assertNull(mapper.toDto(null));
        }

        @Test
        @DisplayName("full entity maps geometry and properties")
        void toDto_fullEntity_mapsAllFields() throws UnknownHostException {
            Rsu rsu = buildRsu(true);

            RsuGeoInfoDto result = mapper.toDto(rsu);

            assertNotNull(result);
            assertEquals("Feature", result.getType());
            assertEquals(42, result.getId());

            assertNotNull(result.getGeometry());
            assertEquals("Point", result.getGeometry().getType());
            assertNotNull(result.getGeometry().getCoordinates());
            assertEquals(2, result.getGeometry().getCoordinates().length);
            assertEquals(-104.9903, result.getGeometry().getCoordinates()[0], DELTA);
            assertEquals(39.7392, result.getGeometry().getCoordinates()[1], DELTA);

            assertNotNull(result.getProperties());
            assertEquals(42, result.getProperties().getRsuId());
            assertEquals(12.5, result.getProperties().getMilepost(), DELTA);
            assertEquals("192.168.10.5", result.getProperties().getIpv4Address());
            assertEquals("SERIAL-001", result.getProperties().getSerialNumber());
            assertEquals("I-25", result.getProperties().getPrimaryRoute());
            assertTrue(result.getProperties().getTimDeposit());
            assertFalse(result.getProperties().getSnmpMonitoring());
            assertEquals("ITS-RS4-M", result.getProperties().getModelName());
            assertEquals("Commsignia", result.getProperties().getManufacturerName());
        }

        @Test
        @DisplayName("null geography maps to null geometry")
        void toDto_nullGeography_mapsNullGeometry() throws UnknownHostException {
            Rsu rsu = buildRsu(true);
            rsu.setGeography(null);

            RsuGeoInfoDto result = mapper.toDto(rsu);

            assertNotNull(result);
            assertNull(result.getGeometry());
        }
    }

    @Nested
    @DisplayName("toPropertiesDto(Rsu) - properties block mapping")
    class ToPropertiesDtoTests {

        @Test
        @DisplayName("null input returns null")
        void toPropertiesDto_null_returnsNull() {
            assertNull(mapper.toPropertiesDto(null));
        }

        @Test
        @DisplayName("null rsuOption defaults timDeposit/snmpMonitoring to false")
        void toPropertiesDto_nullOption_defaultsToFalse() throws UnknownHostException {
            Rsu rsu = buildRsu(false);

            RsuGeoInfoPropertiesDto result = mapper.toPropertiesDto(rsu);

            assertNotNull(result);
            assertNotNull(result.getTimDeposit());
            assertNotNull(result.getSnmpMonitoring());
            assertFalse(result.getTimDeposit());
            assertFalse(result.getSnmpMonitoring());
        }

        @Test
        @DisplayName("nested model and manufacturer names are mapped")
        void toPropertiesDto_nestedNames_mapped() throws UnknownHostException {
            Rsu rsu = buildRsu(true);

            RsuGeoInfoPropertiesDto result = mapper.toPropertiesDto(rsu);

            assertNotNull(result);
            assertEquals("ITS-RS4-M", result.getModelName());
            assertEquals("Commsignia", result.getManufacturerName());
        }
    }

}
