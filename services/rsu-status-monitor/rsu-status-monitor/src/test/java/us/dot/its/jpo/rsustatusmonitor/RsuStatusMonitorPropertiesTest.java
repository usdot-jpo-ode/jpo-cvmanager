package us.dot.its.jpo.rsustatusmonitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.info.BuildProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RsuStatusMonitorPropertiesTest {

    @Mock
    private BuildProperties buildProperties;

    @InjectMocks
    private RsuStatusMonitorProperties rsuStatusMonitorProperties;

    @Test
    void testInitialize() {
        when(buildProperties.getGroup()).thenReturn("test-group");
        when(buildProperties.getArtifact()).thenReturn("test-artifact");
        when(buildProperties.getVersion()).thenReturn("1.0.0");

        rsuStatusMonitorProperties.initialize();

        verify(buildProperties).getGroup();
        verify(buildProperties).getArtifact();
        verify(buildProperties).getVersion();
    }

    @Test
    void testGetVersion() {
        String expectedVersion = "1.0.0";
        when(buildProperties.getVersion()).thenReturn(expectedVersion);

        String actualVersion = rsuStatusMonitorProperties.getVersion();

        assertEquals(expectedVersion, actualVersion);
    }
}
