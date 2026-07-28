package us.dot.its.jpo.ode.api.services;

import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import us.dot.its.jpo.ode.api.models.postgres.dtos.FirmwareUpgradeCheckResponseDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.FirmwareUpgradeResultDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpgradeRule;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.repositories.FirmwareUpgradeRuleRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsuUpgradeService {

    private final RsuUpgradeContextService rsuUpgradeContextService;
    private final FirmwareUpgradeRuleRepository firmwareUpgradeRuleRepository;
    private final RsuRepository rsuRepository;

    @Value("${firmwareManagerEndpoint:}")
    private String firmwareManagerEndpoint;

    private final RestTemplate restTemplate;
    private final PlatformTransactionManager transactionManager;

    public FirmwareUpgradeCheckResponseDto checkFirmwareUpgrade(String rsuIp) {
        Rsu rsu = rsuUpgradeContextService.findRsuByIp(rsuIp);
        if (rsu == null) {
            throw new EntityNotFoundException(
                    "Provided RSU IP does not have complete RSU data: " + rsuIp);
        }

        FirmwareUpgradeInfo upgradeInfo = checkForUpgrade(rsu);
        FirmwareImage upgradeImage = upgradeInfo.upgradeImage();

        return new FirmwareUpgradeCheckResponseDto(
                upgradeInfo.upgradeAvailable(),
                upgradeImage != null && upgradeImage.getId() != null ? upgradeImage.getId().longValue() : -1L,
                upgradeImage != null && upgradeImage.getName() != null ? upgradeImage.getName() : "",
                upgradeImage != null && upgradeImage.getVersion() != null ? upgradeImage.getVersion() : "");
    }

    public Map<String, FirmwareUpgradeResultDto> startFirmwareUpgradeForRsus(List<String> rsuIps) {
        Map<String, FirmwareUpgradeResultDto> response = new LinkedHashMap<>();

        for (String rsuIp : rsuIps) {
            if (!rsuUpgradeContextService.hasCompleteRsuData(rsuIp)) {
                response.put(rsuIp, new FirmwareUpgradeResultDto(
                        HttpStatus.NOT_FOUND.value(),
                        "Provided RSU IP does not have complete RSU data: " + rsuIp));
                continue;
            }

            try {
                UpgradeExecutionResult result = executeUpgradeForRsu(rsuIp);
                response.put(rsuIp, new FirmwareUpgradeResultDto(result.statusCode(), result.body()));
            } catch (EntityNotFoundException ex) {
                response.put(rsuIp, new FirmwareUpgradeResultDto(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
            } catch (FirmwareUpgradeUnavailableException ex) {
                response.put(rsuIp, new FirmwareUpgradeResultDto(HttpStatus.CONFLICT.value(), ex.getMessage()));
            } catch (ResponseStatusException ex) {
                response.put(rsuIp, new FirmwareUpgradeResultDto(
                        ex.getStatusCode().value(),
                        ex.getReason() == null || ex.getReason().isBlank() ? ex.getMessage() : ex.getReason()));
            } catch (RuntimeException ex) {
                log.warn("Failed to start firmware upgrade for RSU {}", rsuIp, ex);
                response.put(rsuIp, new FirmwareUpgradeResultDto(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage() == null || ex.getMessage().isBlank()
                                ? "Failed to initiate firmware upgrade for RSU '" + rsuIp + "'"
                                : ex.getMessage()));
            }
        }

        return response;
    }

    protected UpgradeExecutionResult executeUpgradeForRsu(String rsuIp) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return Objects.requireNonNull(
                transactionTemplate.execute(status -> markRsuForUpgrade(rsuIp)),
                "Upgrade execution result must not be null");
    }

    protected UpgradeExecutionResult markRsuForUpgrade(String rsuIp) {
        if (firmwareManagerEndpoint == null || firmwareManagerEndpoint.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                    "The firmware manager is not supported for this CV Manager deployment");
        }

        Rsu rsu = rsuUpgradeContextService.findRsuByIp(rsuIp);
        if (rsu == null) {
            throw new EntityNotFoundException(
                    "Provided RSU IP does not have complete RSU data: " + rsuIp);
        }

        FirmwareUpgradeInfo upgradeInfo = checkForUpgrade(rsu);

        if (!upgradeInfo.upgradeAvailable()) {
            throw new FirmwareUpgradeUnavailableException(
                    "Requested RSU '" + rsuIp + "' is already up to date with the latest firmware");
        }

        rsu.setTargetFirmwareVersion(upgradeInfo.upgradeImage());
        rsuRepository.save(rsu);

        try {
            Map<String, String> postBody = Map.of("rsu_ip", rsuIp);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    firmwareManagerEndpoint + "/init_firmware_upgrade",
                    new HttpEntity<>(postBody, headers),
                    Map.class);

            Object responseBody = response.getBody() == null ? Map.of() : response.getBody();
            log.info("Firmware manager response for {}: {}", rsuIp, responseBody);
            return new UpgradeExecutionResult(responseBody, response.getStatusCode().value());
        } catch (HttpStatusCodeException ex) {
            String errorMessage = ex.getResponseBodyAsString();
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "Firmware manager returned " + ex.getStatusCode().value();
            }
            throw new ResponseStatusException(ex.getStatusCode(), errorMessage, ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to initiate firmware upgrade for RSU '" + rsuIp + "'", ex);
        }
    }

    protected FirmwareUpgradeInfo checkForUpgrade(Rsu rsu) {
        FirmwareImage currentFirmware = rsu.getFirmwareVersion();

        if (currentFirmware == null || currentFirmware.getId() == null) {
            return new FirmwareUpgradeInfo(false, null);
        }

        FirmwareUpgradeRule upgradeRule = firmwareUpgradeRuleRepository
                .findFirstByFrom_Id(currentFirmware.getId())
                .orElse(null);

        if (upgradeRule == null) {
            return new FirmwareUpgradeInfo(false, null);
        }

        return new FirmwareUpgradeInfo(true, upgradeRule.getTo());
    }

    public record UpgradeExecutionResult(Object body, int statusCode) {
    }

    public record FirmwareUpgradeInfo(boolean upgradeAvailable, FirmwareImage upgradeImage) {
    }

    public static class FirmwareUpgradeUnavailableException extends RuntimeException {
        public FirmwareUpgradeUnavailableException(String message) {
            super(message);
        }
    }
}
