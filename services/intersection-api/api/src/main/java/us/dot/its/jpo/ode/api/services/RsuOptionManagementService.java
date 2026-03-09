package us.dot.its.jpo.ode.api.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOption;
import us.dot.its.jpo.ode.api.repositories.RsuOptionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

/**
 * Service for managing RSU options (TIM deposit and SNMP monitoring).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RsuOptionManagementService {

    private final RsuRepository rsuRepository;
    private final RsuOptionRepository rsuOptionRepository;

    /**
     * Modifies RSU options for the given RSU IP. Creates a new entry if needed.
     * Only saves if changes are detected.
     */
    public void modifyRsuOption(String rsuIp, RsuPatch rsuPatch) {
        log.debug("Modifying Rsu option with IP: {}", rsuIp);

        // Early return if no option fields are provided
        if (rsuPatch.getTimDeposit() == null && rsuPatch.getSnmpMonitoring() == null) {
            log.trace("Patch does not contain tim_deposit or snmp_monitoring values, no modification necessary");
            return;
        }

        Rsu existingRsu = findRsuByIp(rsuIp);

        RsuOption rsuOption = getOrCreateRsuOption(existingRsu);
        boolean isNewOption = rsuOption.getId() == null;

        boolean modified = updateRsuOptionFields(rsuOption, rsuPatch, isNewOption);

        saveRsuOptionIfModified(rsuOption, modified, isNewOption);

        log.debug("Done modifying Rsu option with IP: {}", rsuIp);
    }

    // Finds RSU by IP address or throws NOT_FOUND/BAD_REQUEST
    private Rsu findRsuByIp(String rsuIp) {
        try {
            InetAddress inetAddress = InetAddress.getByName(rsuIp);
            Rsu rsu = rsuRepository.findByIpv4Address(inetAddress);

            if (rsu == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RSU not found with IP: " + rsuIp);
            }

            return rsu;
        } catch (UnknownHostException e) {
            log.error("Invalid IP address: {}", rsuIp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: " + rsuIp, e);
        }
    }

    // Gets existing RsuOption or creates a new one
    private RsuOption getOrCreateRsuOption(Rsu rsu) {
        Optional<RsuOption> rsuOptionOptional = rsuOptionRepository.findByRsuId(rsu.getId());

        if (rsuOptionOptional.isPresent()) {
            log.trace("Found existing rsu_option for RSU with ID: {}", rsu.getId());
            return rsuOptionOptional.get();
        } else {
            log.trace("Creating new rsu_option for RSU with ID: {}", rsu.getId());
            RsuOption newOption = new RsuOption();
            newOption.setRsu(rsu);
            return newOption;
        }
    }

    // Updates option fields from patch, returns true if any changes made
    private boolean updateRsuOptionFields(RsuOption rsuOption, RsuPatch rsuPatch, boolean isNewOption) {
        boolean modified = false;

        modified |= updateTimDepositField(rsuOption, rsuPatch.getTimDeposit(), isNewOption);
        modified |= updateSnmpMonitoringField(rsuOption, rsuPatch.getSnmpMonitoring(), isNewOption);

        return modified;
    }

    // Updates tim_deposit if provided and different from current
    private boolean updateTimDepositField(RsuOption rsuOption, Boolean proposedValue, boolean isNewOption) {
        if (proposedValue == null) {
            return false;
        }

        log.trace("Proposed tim_deposit value: {}", proposedValue);

        if (isNewOption || !proposedValue.equals(rsuOption.getTimDeposit())) {
            if (!isNewOption) {
                log.trace("Current tim_deposit value: {}, changing to: {}",
                        rsuOption.getTimDeposit(), proposedValue);
            }
            rsuOption.setTimDeposit(proposedValue);
            return true;
        } else {
            log.trace("tim_deposit value unchanged: {}", proposedValue);
            return false;
        }
    }

    // Updates snmp_monitoring if provided and different from current
    private boolean updateSnmpMonitoringField(RsuOption rsuOption, Boolean proposedValue, boolean isNewOption) {
        if (proposedValue == null) {
            return false;
        }

        log.trace("Proposed snmp_monitoring value: {}", proposedValue);

        if (isNewOption || !proposedValue.equals(rsuOption.getSnmpMonitoring())) {
            if (!isNewOption) {
                log.trace("Current snmp_monitoring value: {}, changing to: {}",
                        rsuOption.getSnmpMonitoring(), proposedValue);
            }
            rsuOption.setSnmpMonitoring(proposedValue);
            return true;
        } else {
            log.trace("snmp_monitoring value unchanged: {}", proposedValue);
            return false;
        }
    }

    // Saves to database only if modifications were detected
    private void saveRsuOptionIfModified(RsuOption rsuOption, boolean modified, boolean isNewOption) {
        if (modified) {
            log.debug("Saving {} rsu_option entry - tim_deposit: {}, snmp_monitoring: {}",
                    isNewOption ? "new" : "modified",
                    rsuOption.getTimDeposit(),
                    rsuOption.getSnmpMonitoring());
            rsuOptionRepository.save(rsuOption);
        } else {
            log.trace("No changes detected, skipping save");
        }
    }
}
