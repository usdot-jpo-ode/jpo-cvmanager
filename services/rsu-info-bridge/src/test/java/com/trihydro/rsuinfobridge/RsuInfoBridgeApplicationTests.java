package com.trihydro.rsuinfobridge;

import com.trihydro.rsuinfobridge.testutil.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RsuInfoBridgeApplicationTests {

    @Test
    void contextLoads() {
    }

}
