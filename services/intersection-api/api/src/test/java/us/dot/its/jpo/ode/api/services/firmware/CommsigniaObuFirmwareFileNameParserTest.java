package us.dot.its.jpo.ode.api.services.firmware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareDeviceType;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;

class CommsigniaObuFirmwareFileNameParserTest {
    private final CommsigniaObuFirmwareFileNameParser parser = new CommsigniaObuFirmwareFileNameParser();
    private final Pattern pattern = Pattern.compile(parser.fileNamePattern());

    // Keep these cases identical to test_is_valid_firmware_name in
    // services/addons/tests/obu_ota_server/test_commsignia_manifest.py
    static Stream<Arguments> validityCases() {
        return Stream.of(
                Arguments.of("ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig", true),
                Arguments.of("ob4-generic-rw-y20.48.2-b228647.tar.sig", true),
                Arguments.of("rs4-generic-ro-y20.48.2-b228647.tar.sig", true),
                Arguments.of("ob4-generic-custom_variant-ro-secureboot-y20.48.2-b228647.tar.sig", true),
                Arguments.of("ob4-generic-ro-secureboot-y20.48.2-b228647.tar", false),
                Arguments.of("ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig.bak", false),
                Arguments.of("ob4-generic-ro-secureboot-y20.48.2-b228647xtarxsig", false),
                Arguments.of("../ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig", false),
                Arguments.of("ob4-generic-ro-secureboot-../../etc/passwd.tar.sig", false),
                Arguments.of("ob4-generic-ro-secureboot-y20/b228647.tar.sig", false),
                Arguments.of("ob4-generic-ro-.tar.sig", false),
                Arguments.of("ob5-generic-ro-y20.48.2-b228647.tar.sig", false),
                Arguments.of("ob4-generic--ro-y20.48.2.tar.sig", false),
                Arguments.of("ob4-generic-ro-" + "1".repeat(106) + ".tar.sig", false),
                Arguments.of("ob4-generic-ro-" + "1".repeat(105) + ".tar.sig", true));
    }

    @ParameterizedTest
    @MethodSource("validityCases")
    void acceptsTheSameNamesAsTheOtaServer(String fileName, boolean expected) {
        assertThat(parser.parse(fileName).isPresent()).as(fileName).isEqualTo(expected);
    }

    // Keep these cases identical to test_parse_firmware_name in
    // services/addons/tests/obu_ota_server/test_commsignia_manifest.py
    static Stream<Arguments> parseCases() {
        return Stream.of(
                Arguments.of("ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig",
                        metadata("ob4-generic", ObuFirmwareDeviceType.OBU, false, true, "y20.48.2-b228647")),
                Arguments.of("rs4-generic-rw-y20.48.2-b228647.tar.sig",
                        metadata("rs4-generic", ObuFirmwareDeviceType.RSU, true, false, "y20.48.2-b228647")),
                Arguments.of("ob4-generic-custom_variant-ro-y20.48.2.tar.sig",
                        metadata("ob4-generic-custom_variant", ObuFirmwareDeviceType.OBU, false, false, "y20.48.2")),
                // The last rw/ro marker that leaves a valid release wins, as with a greedy variant
                Arguments.of("ob4-generic-ro-rw-y20.48.2.tar.sig",
                        metadata("ob4-generic-ro", ObuFirmwareDeviceType.OBU, true, false, "y20.48.2")),
                Arguments.of("ob4-generic-ro-y20-ro-.tar.sig",
                        metadata("ob4-generic", ObuFirmwareDeviceType.OBU, false, false, "y20-ro-")),
                // secureboot without a following release is the release itself
                Arguments.of("ob4-generic-ro-secureboot-.tar.sig",
                        metadata("ob4-generic", ObuFirmwareDeviceType.OBU, false, false, "secureboot-")));
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    void parsesTheSameMetadataAsTheOtaServer(String fileName, ObuFirmwareFileMetadata expected) {
        assertThat(parser.parse(fileName)).contains(expected);
    }

    @Test
    void rejectsNullAndLongNamesWithoutBacktracking() {
        assertThat(parser.parse(null)).isEmpty();
        String longName = "ob4-generic" + "-ro".repeat(10_000) + ".tar.sig";
        org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> assertThat(parser.parse(longName)).isEmpty());
    }

    @Test
    void webappPatternAcceptsExactlyTheNamesTheParserAccepts() {
        // The webapp checks names with the published pattern before uploading, so
        // it must never reject a name the API accepts or accept one the API rejects
        List<String> tokens = List.of("ob4", "rs4", "ob5", "generic", "ro", "rw", "secureboot",
                "y20.48.2", "b228647", "", "x_y", ".z", "-");
        List<String> endings = List.of(".tar.sig", ".tar", "", ".tar.sig.bak");
        Random random = new Random(381);
        int accepted = 0;
        for (int i = 0; i < 50_000; i++) {
            StringBuilder name = new StringBuilder(random.nextBoolean() ? "ob4-generic" : tokens.get(random.nextInt(4)));
            int count = random.nextInt(7);
            for (int token = 0; token < count; token++) {
                name.append('-').append(tokens.get(random.nextInt(tokens.size())));
            }
            name.append(endings.get(random.nextInt(endings.size())));
            String fileName = name.toString();

            boolean parsed = parser.parse(fileName).isPresent();
            boolean matched = fileName.length() <= ObuFirmwareFileNameParser.MAX_FILE_NAME_LENGTH
                    && pattern.matcher(fileName).matches();
            assertThat(matched).as(fileName).isEqualTo(parsed);
            if (parsed) {
                accepted++;
            }
        }
        assertThat(accepted).as("the generated names include accepted names").isGreaterThan(1000);
    }

    @Test
    void describesTheCommsigniaConvention() {
        assertThat(parser.manufacturerName()).isEqualTo("Commsignia");
        assertThat(parser.fileExtension()).isEqualTo(".tar.sig");
        assertThat(parser.parse(parser.exampleFileName())).isPresent();
    }

    @Test
    void registryFindsParsersByManufacturerIgnoringCase() {
        var registry = new ObuFirmwareParserRegistry(List.of(parser));

        assertThat(registry.find(" COMMSIGNIA ")).containsSame(parser);
        assertThat(registry.find("Kapsch")).isEmpty();
        assertThat(registry.find(null)).isEmpty();
        assertThatThrownBy(() -> new ObuFirmwareParserRegistry(List.of(parser, new CommsigniaObuFirmwareFileNameParser())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Commsignia");
    }

    private static ObuFirmwareFileMetadata metadata(String variant, ObuFirmwareDeviceType type, boolean writable,
            boolean secure, String release) {
        return new ObuFirmwareFileMetadata(type, release, variant, writable, secure);
    }
}
