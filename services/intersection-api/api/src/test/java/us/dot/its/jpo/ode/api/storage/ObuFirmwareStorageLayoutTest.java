package us.dot.its.jpo.ode.api.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class ObuFirmwareStorageLayoutTest {
    @ParameterizedTest
    @ValueSource(strings = { "ota", "OTA", " ota " })
    void reservesTheOtaRootIgnoringCase(String name) {
        assertThat(ObuFirmwareStorageLayout.isReservedName(name)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "Commsignia", "otas", "ota/x" })
    void doesNotReserveOtherNames(String name) {
        assertThat(ObuFirmwareStorageLayout.isReservedName(name)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({ "ota/Commsignia/r1/a.tar.sig,true", "OTA/a,true", "ota,true", "otab/a,false",
            "Commsignia/ota/a,false", "'',false" })
    void recognizesObjectsBeneathTheRoot(String objectName, boolean expected) {
        assertThat(ObuFirmwareStorageLayout.isObuObjectName(objectName)).isEqualTo(expected);
    }

    @Test
    void buildsObjectNamesBeneathTheRoot() {
        assertThat(ObuFirmwareStorageLayout.objectName("Commsignia", "y20.48.2", "a.tar.sig"))
                .isEqualTo("ota/Commsignia/y20.48.2/a.tar.sig");
        assertThat(ObuFirmwareStorageLayout.isObuObjectName(null)).isFalse();
    }
}
