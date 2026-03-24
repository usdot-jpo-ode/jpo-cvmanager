package us.dot.its.jpo.ode.api.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.mappers.RsuInfoMapper;
import us.dot.its.jpo.ode.api.mappers.RsuPatchMapper;
import us.dot.its.jpo.ode.api.models.devices.RsuInfoDto;
import us.dot.its.jpo.ode.api.models.devices.management.ModifyRsuAllowedSelections;
import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.repositories.ConsecutiveFirmwareUpgradeFailureRepository;
import us.dot.its.jpo.ode.api.repositories.MaxRetryLimitReachedInstanceRepository;
import us.dot.its.jpo.ode.api.repositories.OrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.PingRepository;
import us.dot.its.jpo.ode.api.repositories.RsuCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.RsuIntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuModelRepository;
import us.dot.its.jpo.ode.api.repositories.RsuOptionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.ScmsHealthRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpMsgfwdConfigRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpProtocolRepository;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuCredential;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuModel;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOption;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOrganization;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpCredential;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpProtocol;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;

@Service
@Slf4j
@RequiredArgsConstructor
public class RsuManagementService {

    private final ConsecutiveFirmwareUpgradeFailureRepository consecutiveFirmwareUpgradeFailureRepository;
    private final MaxRetryLimitReachedInstanceRepository maxRetryLimitReachedInstanceRepository;
    private final OrganizationRepository organizationRepository;
    private final PingRepository pingRepository;
    private final RsuCredentialRepository rsuCredentialRepository;
    private final RsuIntersectionRepository rsuIntersectionRepository;
    private final RsuOrganizationRepository rsuOrganizationRepository;
    private final RsuModelRepository rsuModelRepository;
    private final RsuRepository rsuRepository;
    private final RsuOptionRepository rsuOptionRepository;
    private final ScmsHealthRepository scmsHealthRepository;
    private final SnmpCredentialRepository snmpCredentialRepository;
    private final SnmpMsgfwdConfigRepository snmpMsgfwdConfigRepository;
    private final SnmpProtocolRepository snmpProtocolRepository;
    private final RsuInfoMapper rsuMapper;
    private final RsuPatchMapper rsuPatchMapper;

    public RsuInfoDto getRsuInfo(String ipv4Address) {
        try {
            Rsu rsu = rsuRepository.findByIpv4Address(InetAddress.getByName(ipv4Address));
            return rsu != null ? rsuMapper.toDto(rsu) : null;
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: " + ipv4Address, e);
        }
    }

    public Page<RsuInfoDto> getAllRsuInfo(String orgName, String search, Pageable pageable) {
        Page<Rsu> rsus = rsuRepository.findAllByOrganization(orgName, search, pageable);
        return rsus.map(rsuMapper::toDto);
    }

    public ModifyRsuAllowedSelections getAllowedSelections(CvManagerAuthToken userToken) {
        ModifyRsuAllowedSelections allowed = new ModifyRsuAllowedSelections();

        allowed.setPrimaryRoutes(rsuRepository.findAllPrimaryRoutes());
        allowed.setRsuModels(rsuRepository.findAllRsuModels().stream()
                .map(v -> String.format("%s %s", v.getManufacturer(),
                        v.getModel()))
                .toList());
        allowed.setSshCredentialGroups(rsuCredentialRepository.findAllNicknames());
        allowed.setSnmpCredentialGroups(snmpCredentialRepository.findAllNicknames());
        allowed.setSnmpVersionGroups(snmpProtocolRepository.findAllNicknames());
        allowed.setOrganizations(userToken.getQualifiedOrgList("ADMIN"));

        return allowed;
    }

    @Transactional
    public Rsu createRsu(RsuInfoDto rsuInfoDto, List<String> orgsToAdd) {
        Rsu rsu = rsuMapper.toEntity(rsuInfoDto);
        updateRelationships(rsu, rsuInfoDto);

        InetAddress ipv4Address = rsu.getIpv4Address();
        if (ipv4Address != null && rsuRepository.findByIpv4Address(ipv4Address) != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "RSU with IP " + ipv4Address.getHostAddress() + " already exists");
        }

        Rsu createdRsu = rsuRepository.save(rsu);

        RsuOption rsuOption = new RsuOption();
        rsuOption.setRsu(createdRsu);
        rsuOption.setTimDeposit(rsuInfoDto.getTimDeposit());
        rsuOption.setSnmpMonitoring(rsuInfoDto.getSnmpMonitoring());
        rsuOptionRepository.save(rsuOption);

        var toCreate = new ArrayList<RsuOrganization>();
        for (String orgName : orgsToAdd) {
            toCreate.add(createRsuOrgRelationship(orgName, rsu));
        }
        rsuOrganizationRepository.saveAll(toCreate);

        return rsu;
    }

    public RsuOrganization createRsuOrgRelationship(String orgName, Rsu rsu) {
        Organization organization = organizationRepository.findByName(orgName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Organization not found: " + orgName));

        RsuOrganization rsuOrg = new RsuOrganization();
        rsuOrg.setOrganization(organization);
        rsuOrg.setRsu(rsu);
        return rsuOrg;
    }

    private void updateRelationships(Rsu rsu, RsuInfoDto rsuInfoDto) {
        // Update model if provided
        if (rsuInfoDto.getModel() != null) {
            RsuModel model = findRsuModelByName(rsuInfoDto.getModel());
            rsu.setModel(model);
        }

        // Update SSH credential if provided
        if (rsuInfoDto.getSshCredentialGroup() != null) {
            RsuCredential credential = rsuCredentialRepository
                    .findByNickname(rsuInfoDto.getSshCredentialGroup())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "SSH credential not found: " + rsuInfoDto.getSshCredentialGroup()));
            rsu.setCredential(credential);
        }

        // Update SNMP credential if provided
        if (rsuInfoDto.getSnmpCredentialGroup() != null) {
            SnmpCredential snmpCredential = snmpCredentialRepository
                    .findByNickname(rsuInfoDto.getSnmpCredentialGroup())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "SNMP credential not found: " + rsuInfoDto.getSnmpCredentialGroup()));
            rsu.setSnmpCredential(snmpCredential);
        }

        // Update SNMP protocol if provided
        if (rsuInfoDto.getSnmpVersionGroup() != null) {
            SnmpProtocol snmpProtocol = snmpProtocolRepository
                    .findByNickname(rsuInfoDto.getSnmpVersionGroup())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "SNMP protocol not found: " + rsuInfoDto.getSnmpVersionGroup()));
            rsu.setSnmpProtocol(snmpProtocol);
        }
    }

    @Transactional
    public RsuInfoDto modifyRsu(String rsuIp, RsuPatch rsuPatch, CvManagerAuthToken userToken) {
        try {
            List<String> authorizedOrgs = userToken.getQualifiedOrgList("ADMIN");

            // 1. Find existing RSU by original IP
            InetAddress inetAddress = InetAddress.getByName(rsuIp);
            Rsu existingRsu = rsuRepository.findByIpv4Address(inetAddress);

            if (existingRsu == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RSU not found with IP: " + rsuIp);
            }

            // 2. If IP is being changed, check for conflicts
            if (rsuPatch.getIpv4Address() != null && !rsuPatch.getIpv4Address().equals(rsuIp)) {
                InetAddress newIp = InetAddress.getByName(rsuPatch.getIpv4Address());
                Rsu conflictingRsu = rsuRepository.findByIpv4Address(newIp);
                if (conflictingRsu != null && !conflictingRsu.getIpv4Address().equals(existingRsu.getIpv4Address())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "RSU with IP " + rsuPatch.getIpv4Address() + " already exists");
                }
            }

            // 3. Update only non-null fields using MapStruct
            rsuPatchMapper.updateRsuFromPatch(rsuPatch, existingRsu);

            // 4. Update relationships that require database lookups
            updateRelationships(existingRsu, rsuPatch);

            // 5. Handle organization additions/removals
            handleOrganizationChanges(existingRsu, rsuPatch, authorizedOrgs);

            // 6. Save updated entity (JPA handles UPDATE SQL)
            Rsu savedRsu = rsuRepository.save(existingRsu);

            // 7. Return DTO
            return rsuMapper.toDto(savedRsu);

        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: " + rsuIp, e);
        }
    }

    private void updateRelationships(Rsu rsu, RsuPatch patch) {
        // Update model if provided
        if (patch.getModel() != null) {
            RsuModel model = findRsuModelByName(patch.getModel());
            rsu.setModel(model);
        }

        // Update SSH credential if provided
        if (patch.getSshCredentialGroup() != null) {
            RsuCredential credential = rsuCredentialRepository
                    .findByNickname(patch.getSshCredentialGroup())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "SSH credential not found: " + patch.getSshCredentialGroup()));
            rsu.setCredential(credential);
        }

        // Update SNMP credential if provided
        if (patch.getSnmpCredentialGroup() != null) {
            SnmpCredential snmpCredential = snmpCredentialRepository
                    .findByNickname(patch.getSnmpCredentialGroup())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "SNMP credential not found: " + patch.getSnmpCredentialGroup()));
            rsu.setSnmpCredential(snmpCredential);
        }

        // Update SNMP protocol if provided
        if (patch.getSnmpVersionGroup() != null) {
            SnmpProtocol snmpProtocol = snmpProtocolRepository
                    .findByNickname(patch.getSnmpVersionGroup())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "SNMP protocol not found: " + patch.getSnmpVersionGroup()));
            rsu.setSnmpProtocol(snmpProtocol);
        }
    }

    private void handleOrganizationChanges(Rsu rsu, RsuPatch patch, List<String> authorizedOrgs) {

        // Add organizations
        if (patch.getOrganizationsToAdd() != null && !patch.getOrganizationsToAdd().isEmpty()) {
            List<String> unqualifiedAdds = patch.getOrganizationsToAdd().stream()
                    .filter(org -> !authorizedOrgs.contains(org))
                    .toList();
            if (!unqualifiedAdds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User does not have permission to add RSU to organization(s): "
                                + String.join(", ", unqualifiedAdds));
            }
            for (String orgName : patch.getOrganizationsToAdd()) {
                // Check if already associated
                boolean exists = rsuRepository.existsByIpAndOrganizations(
                        rsu.getIpv4Address(),
                        List.of(orgName));

                if (!exists) {
                    Organization org = organizationRepository.findByName(orgName)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Organization not found: " + orgName));

                    RsuOrganization rsuOrg = new RsuOrganization();
                    rsuOrg.setRsu(rsu);
                    rsuOrg.setOrganization(org);

                    rsuOrganizationRepository.save(rsuOrg);
                }
            }
        }

        // Remove organizations
        if (patch.getOrganizationsToRemove() != null && !patch.getOrganizationsToRemove().isEmpty()) {
            List<String> unqualifiedRemoves = patch.getOrganizationsToRemove().stream()
                    .filter(org -> !authorizedOrgs.contains(org))
                    .toList();
            if (!unqualifiedRemoves.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User does not have permission to remove RSU from organization(s): "
                                + String.join(", ", unqualifiedRemoves));
            }
            for (String orgName : patch.getOrganizationsToRemove()) {
                // Find and delete the specific association
                rsuOrganizationRepository.findByRsuIpv4AddressAndOrganization_Name(
                        rsu.getIpv4Address(),
                        orgName).ifPresent(rsuOrganizationRepository::delete);
            }
        }
    }

    private RsuModel findRsuModelByName(String modelStr) {
        // Parse "Manufacturer Model" format
        String[] parts = modelStr.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid model format. Expected 'Manufacturer Model', got: " + modelStr);
        }

        String manufacturerName = parts[0];
        String modelName = parts[1];

        return rsuModelRepository.findByNameAndManufacturerName(modelName, manufacturerName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model not found: " + modelStr));
    }

    @Transactional
    public void deleteRsuByIpv4Address(String ipv4Address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipv4Address);

            // Check if RSU exists
            Rsu rsu = rsuRepository.findByIpv4Address(inetAddress);
            if (rsu == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RSU not found with IP: " + ipv4Address);
            }

            // Delete related entities first to maintain referential integrity
            pingRepository.removePingByIpv4Address(inetAddress);
            rsuOrganizationRepository.removeRsuOrganizationByIpv4Address(inetAddress);
            scmsHealthRepository.removeScmsHealthByIpv4Address(inetAddress);
            snmpMsgfwdConfigRepository.removeSnmpMsgfwdConfigByIpv4Address(inetAddress);
            rsuIntersectionRepository.removeRsuIntersectionByIpv4Address(inetAddress);
            consecutiveFirmwareUpgradeFailureRepository
                    .removeConsecutiveFirmwareUpgradeFailureByIpv4Address(inetAddress);
            maxRetryLimitReachedInstanceRepository.removeMaxRetryLimitReachedInstanceByIpv4Address(inetAddress);
            rsuOptionRepository.removeRsuOptionByIpv4Address(inetAddress);

            // Finally, delete the RSU itself
            rsuRepository.removeRsuByIpv4Address(inetAddress);
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: " + ipv4Address, e);
        }
    }

    @Transactional
    public void deleteMultipleRsusByIpv4Address(List<String> rsuIps) {
        List<InetAddress> inetAddresses = rsuIps.stream().map(ip -> {
            try {
                return InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: " + ip, e);
            }
        }).toList();

        // Check if all RSUs exist
        List<Rsu> existingRsus = rsuRepository.findByIpv4AddressIn(inetAddresses);
        if (existingRsus.size() != inetAddresses.size()) {
            // Find which IPs don't exist
            List<String> existingIps = existingRsus.stream()
                    .map(rsu -> rsu.getIpv4Address().getHostAddress())
                    .toList();
            List<String> missingIps = rsuIps.stream()
                    .filter(ip -> !existingIps.contains(ip))
                    .toList();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "RSU(s) not found with IP(s): " + String.join(", ", missingIps));
        } else if (existingRsus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid RSU IP addresses provided");
        }

        pingRepository.removeMultiplePingsByIpv4Address(inetAddresses);
        rsuOrganizationRepository.removeMultipleRsuOrganizationsByIpv4Address(inetAddresses);
        scmsHealthRepository.removeMultipleScmsHealthByIpv4Address(inetAddresses);
        snmpMsgfwdConfigRepository.removeMultipleSnmpMsgfwdConfigByIpv4Address(inetAddresses);
        rsuIntersectionRepository.removeMultipleRsuIntersectionsByIpv4Address(inetAddresses);
        consecutiveFirmwareUpgradeFailureRepository
                .removeMultipleConsecutiveFirmwareUpgradeFailuresByIpv4Address(inetAddresses);
        maxRetryLimitReachedInstanceRepository
                .removeMultipleMaxRetryLimitReachedInstancesByIpv4Address(inetAddresses);
        rsuOptionRepository.removeMultipleRsuOptionsByIpv4Address(inetAddresses);
        rsuRepository.removeByIpv4AddressIn(inetAddresses);
    }
}