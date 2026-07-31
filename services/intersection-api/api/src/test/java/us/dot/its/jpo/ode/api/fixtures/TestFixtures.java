package us.dot.its.jpo.ode.api.fixtures;

import com.github.javafaker.Faker;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import us.dot.its.jpo.ode.api.models.postgres.tables.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestFixtures {

    private final Faker faker = new Faker();
    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    public Point createPoint(double lon, double lat) {
        return GF.createPoint(new Coordinate(lon, lat));
    }

    public Organization createOrg(String name) {
        Organization org = new Organization();
        org.setName(name);
        return org;
    }

    public Organization createRandomOrg() {
        return createOrg(faker.company().name() + "-" + faker.crypto().md5().substring(0, 8));
    }

    public Intersection createIntersection(String number) {
        Intersection i = new Intersection();
        i.setIntersectionNumber(number);
        i.setRefPt(createPoint(-105.0, 40.0));
        return i;
    }

    public IntersectionOrganization createIntersectionOrganization(Intersection intersection, Organization org) {
        IntersectionOrganization io = new IntersectionOrganization();
        io.setIntersection(intersection);
        io.setOrganization(org);
        return io;
    }

    public Manufacturer createRandomManufacturer() {
        Manufacturer mfr = new Manufacturer();
        mfr.setName(faker.company().name());
        return mfr;
    }

    public RsuModel createRandomRsuModel(Manufacturer manufacturer) {
        RsuModel model = new RsuModel();
        model.setName("Model-" + faker.code().asin());
        model.setSupportedRadio("DSRC");
        model.setManufacturer(manufacturer);
        return model;
    }

    public RsuCredential createRandomRsuCredential(Organization owner) {
        RsuCredential cred = new RsuCredential();
        cred.setUsername(faker.name().username());
        cred.setPassword(faker.internet().password());
        cred.setNickname("cred-" + faker.lorem().word() + faker.number().digits(3));
        cred.setOwnerOrganization(owner);
        return cred;
    }

    public SnmpCredential createRandomSnmpCredential(Organization owner) {
        SnmpCredential snmpCred = new SnmpCredential();
        snmpCred.setUsername("snmp-" + faker.name().username());
        snmpCred.setPassword(faker.internet().password());
        snmpCred.setNickname("snmp-" + faker.lorem().word() + faker.number().digits(3));
        snmpCred.setOwnerOrganization(owner);
        return snmpCred;
    }

    public SnmpProtocol createRandomSnmpProtocol() {
        SnmpProtocol proto = new SnmpProtocol();
        proto.setProtocolCode("NTCIP1218");
        proto.setNickname("NTCIP-" + faker.lorem().word() + faker.number().digits(3));
        return proto;
    }

    public Rsu createRsu(String ip, RsuModel model, RsuCredential cred, SnmpCredential snmpCred, SnmpProtocol proto) throws UnknownHostException {
        Rsu rsu = new Rsu();
        rsu.setIpv4Address(InetAddress.getByName(ip));
        rsu.setGeography(createPoint(-105.0, 40.0));
        rsu.setMilepost(faker.number().randomDouble(2, 0, 1000));
        rsu.setSerialNumber("SN-" + faker.crypto().md5().substring(0, 8));
        rsu.setIssScmsId("ISS-" + faker.crypto().md5().substring(0, 8));
        rsu.setPrimaryRoute(faker.address().streetName());
        rsu.setModel(model);
        rsu.setCredential(cred);
        rsu.setSnmpCredential(snmpCred);
        rsu.setSnmpProtocol(proto);
        return rsu;
    }

    public RsuIntersection createRsuIntersection(Rsu rsu, Intersection intersection) {
        RsuIntersection ri = new RsuIntersection();
        ri.setRsu(rsu);
        ri.setIntersection(intersection);
        return ri;
    }

    public RsuOrganization createRsuOrganization(Rsu rsu, Organization org) {
        RsuOrganization ro = new RsuOrganization();
        ro.setRsu(rsu);
        ro.setOrganization(org);
        return ro;
    }

    public Polygon createBBox(double minLat, double minLon, double maxLat, double maxLon) {
        return (Polygon) GF.toGeometry(new Envelope(minLon, maxLon, minLat, maxLat));
    }
}
