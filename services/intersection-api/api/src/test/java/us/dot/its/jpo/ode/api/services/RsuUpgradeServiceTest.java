package us.dot.its.jpo.ode.api.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import us.dot.its.jpo.ode.api.models.postgres.dtos.FirmwareUpgradeCheckResponseDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.FirmwareUpgradeResultDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpgradeRule;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.repositories.FirmwareUpgradeRuleRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

@ExtendWith(MockitoExtension.class)
class RsuUpgradeServiceTest {

    @Mock
    private RsuUpgradeContextService rsuUpgradeContextService;

    @Mock
    private FirmwareUpgradeRuleRepository firmwareUpgradeRuleRepository;

    @Mock
    private RsuRepository rsuRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private RsuUpgradeService rsuUpgradeService;

    @Test
    void testCheckFirmwareUpgrade_Success() throws UnknownHostException {
        String rsuIp = "10.0.0.10";

        FirmwareImage currentImage = new FirmwareImage();
        currentImage.setId(1);

        FirmwareImage targetImage = new FirmwareImage();
        targetImage.setId(2);
        targetImage.setName("RSU Firmware 2.0");
        targetImage.setVersion("2.0");

        FirmwareUpgradeRule rule = new FirmwareUpgradeRule();
        rule.setFrom(currentImage);
        rule.setTo(targetImage);

        Rsu rsu = new Rsu();
        rsu.setIpv4Address(InetAddress.getByName(rsuIp));
        rsu.setFirmwareVersion(currentImage);

        when(rsuUpgradeContextService.findRsuByIp(rsuIp)).thenReturn(rsu);
        when(firmwareUpgradeRuleRepository.findFirstByFrom_Id(1)).thenReturn(Optional.of(rule));

        FirmwareUpgradeCheckResponseDto result = rsuUpgradeService.checkFirmwareUpgrade(rsuIp);

        assertEquals(true, result.getUpgradeAvailable());
        assertEquals(2L, result.getUpgradeId());
        assertEquals("RSU Firmware 2.0", result.getUpgradeName());
        assertEquals("2.0", result.getUpgradeVersion());
    }

    @Test
    void testStartFirmwareUpgradeForRsus_ReturnsPerRsuResultWhenRsuDataMissing() {
        String successIp = "10.0.0.10";
        String missingIp = "10.0.0.11";

        RsuUpgradeService serviceSpy = spy(rsuUpgradeService);
        when(rsuUpgradeContextService.hasCompleteRsuData(successIp)).thenReturn(true);
        when(rsuUpgradeContextService.hasCompleteRsuData(missingIp)).thenReturn(false);
        doReturn(new RsuUpgradeService.UpgradeExecutionResult(Map.of("message", "started"), 201))
                .when(serviceSpy).executeUpgradeForRsu(successIp);

        Map<String, FirmwareUpgradeResultDto> result = serviceSpy
                .startFirmwareUpgradeForRsus(List.of(successIp, missingIp));

        assertEquals(201, result.get(successIp).getCode());
        assertEquals(Map.of("message", "started"), result.get(successIp).getData());
        assertEquals(404, result.get(missingIp).getCode());
        assertEquals("Provided RSU IP does not have complete RSU data: 10.0.0.11", result.get(missingIp).getData());
    }

    @Test
    void testStartFirmwareUpgradeForRsus_ReturnsConflictWhenAlreadyUpToDate() {
        String rsuIp = "10.0.0.12";

        RsuUpgradeService serviceSpy = spy(rsuUpgradeService);
        when(rsuUpgradeContextService.hasCompleteRsuData(rsuIp)).thenReturn(true);
        doThrow(new RsuUpgradeService.FirmwareUpgradeUnavailableException("Requested RSU is already up to date"))
                .when(serviceSpy).executeUpgradeForRsu(rsuIp);

        Map<String, FirmwareUpgradeResultDto> result = serviceSpy.startFirmwareUpgradeForRsus(List.of(rsuIp));

        assertEquals(409, result.get(rsuIp).getCode());
        assertEquals("Requested RSU is already up to date", result.get(rsuIp).getData());
    }

    @Test
    void testStartFirmwareUpgradeForRsus_ReturnsStatusCodePerRsuWhenFirmwareManagerUnsupported() {
        String rsuIp = "10.0.0.15";

        RsuUpgradeService serviceSpy = spy(rsuUpgradeService);
        when(rsuUpgradeContextService.hasCompleteRsuData(rsuIp)).thenReturn(true);
        doThrow(new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "The firmware manager is not supported"))
                .when(serviceSpy).executeUpgradeForRsu(rsuIp);

        Map<String, FirmwareUpgradeResultDto> result = serviceSpy.startFirmwareUpgradeForRsus(List.of(rsuIp));

        assertEquals(501, result.get(rsuIp).getCode());
        assertEquals("The firmware manager is not supported", result.get(rsuIp).getData());
    }

    @Test
    void testMarkRsuForUpgrade_SuccessPostsJsonAndSavesTargetVersion() throws UnknownHostException {
        String rsuIp = "10.0.0.13";
        String endpoint = "http://firmware-manager";

        RestTemplate restTemplate = org.mockito.Mockito.mock(RestTemplate.class);
        ReflectionTestUtils.setField(rsuUpgradeService, "firmwareManagerEndpoint", endpoint);
        ReflectionTestUtils.setField(rsuUpgradeService, "restTemplate", restTemplate);

        FirmwareImage currentImage = new FirmwareImage();
        currentImage.setId(10);

        FirmwareImage targetImage = new FirmwareImage();
        targetImage.setId(11);
        targetImage.setName("Target Firmware");
        targetImage.setVersion("11.0");

        FirmwareUpgradeRule rule = new FirmwareUpgradeRule();
        rule.setFrom(currentImage);
        rule.setTo(targetImage);

        Rsu rsu = new Rsu();
        rsu.setIpv4Address(InetAddress.getByName(rsuIp));
        rsu.setFirmwareVersion(currentImage);

        when(rsuUpgradeContextService.findRsuByIp(rsuIp)).thenReturn(rsu);
        when(firmwareUpgradeRuleRepository.findFirstByFrom_Id(10)).thenReturn(Optional.of(rule));
        when(rsuRepository.save(any(Rsu.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForEntity(eq(endpoint + "/init_firmware_upgrade"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("message", "started"), HttpStatus.OK));

        RsuUpgradeService.UpgradeExecutionResult result = rsuUpgradeService.markRsuForUpgrade(rsuIp);

        assertEquals(200, result.statusCode());
        assertEquals(Map.of("message", "started"), result.body());
        assertEquals(targetImage, rsu.getTargetFirmwareVersion());

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.captor();
        verify(restTemplate).postForEntity(eq(endpoint + "/init_firmware_upgrade"), entityCaptor.capture(),
                eq(Map.class));

        HttpEntity<?> requestEntity = entityCaptor.getValue();
        assertNotNull(requestEntity);
        assertEquals(MediaType.APPLICATION_JSON, requestEntity.getHeaders().getContentType());
        assertEquals(Map.of("rsu_ip", rsuIp), requestEntity.getBody());
    }

    @Test
    void testMarkRsuForUpgrade_ThrowsConflictWhenAlreadyUpToDate() throws UnknownHostException {
        String rsuIp = "10.0.0.14";

        ReflectionTestUtils.setField(rsuUpgradeService, "firmwareManagerEndpoint", "http://firmware-manager");

        FirmwareImage currentImage = new FirmwareImage();
        currentImage.setId(20);

        Rsu rsu = new Rsu();
        rsu.setIpv4Address(InetAddress.getByName(rsuIp));
        rsu.setFirmwareVersion(currentImage);

        when(rsuUpgradeContextService.findRsuByIp(rsuIp)).thenReturn(rsu);
        when(firmwareUpgradeRuleRepository.findFirstByFrom_Id(20)).thenReturn(Optional.empty());

        RsuUpgradeService.FirmwareUpgradeUnavailableException exception = assertThrows(
                RsuUpgradeService.FirmwareUpgradeUnavailableException.class,
                () -> rsuUpgradeService.markRsuForUpgrade(rsuIp));

        assertTrue(exception.getMessage().contains("already up to date"));
    }
}
