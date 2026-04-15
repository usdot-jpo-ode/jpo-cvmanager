package us.dot.its.jpo.ode.api.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.mappers.INetMapper;
import us.dot.its.jpo.ode.api.mappers.IntersectionMapper;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.admin.intersection.AllowedSelections;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionDto;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionListResponse;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionPatch;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionSingleResponse;
import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;
import us.dot.its.jpo.ode.api.models.postgres.tables.IntersectionOrganization;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuIntersection;
import us.dot.its.jpo.ode.api.repositories.IntersectionOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.IntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.OrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuIntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for admin intersection management.
 *
 * This service is responsible only for business logic and repository
 * operations. All authorization
 * (role checks, intersection resource access, and org restriction enforcement)
 * is handled by AdminIntersectionController before this service is called.
 *
 * Allowed-selections context is computed via PermissionService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminIntersectionService {

    private final IntersectionRepository intersectionRepository;
    private final IntersectionOrganizationRepository intersectionOrganizationRepository;
    private final RsuIntersectionRepository rsuIntersectionRepository;
    private final OrganizationRepository organizationRepository;
    private final RsuRepository rsuRepository;
    private final IntersectionMapper intersectionMapper;
    private final INetMapper inetMapper;
    private final PermissionService permissionService;

    /**
     * Returns a single intersection by intersection_number, plus allowed_selections
     * for UI dropdowns.
     * Access is already verified by @PreAuthorize in the controller; all
     * intersection orgs are returned.
     * AllowedSelections is computed via PermissionService based on the current
     * user's OPERATOR-qualified orgs.
     *
     * @param intersectionId the intersection_number to look up
     * @return response containing intersection_data and allowed_selections
     */
    public IntersectionSingleResponse getIntersection(Integer intersectionId) {
        log.info("Fetching intersection with id: {}", intersectionId);

        Intersection intersection = intersectionRepository.findByIntersectionNumberWithOrgs(intersectionId)
                .orElseThrow(() -> {
                    log.error("Intersection with id {} not found", intersectionId);
                    return new EntityNotFoundException("Intersection with id " + intersectionId + " not found");
                });

        IntersectionDto dto = intersectionMapper.toDto(intersection);

        List<String> orgNames = intersection.getIntersectionOrganizations().stream()
                .filter(io -> io.getOrganization() != null)
                .map(io -> io.getOrganization().getName())
                .collect(Collectors.toList());
        dto.setOrganizations(orgNames);

        List<String> rsuIps = rsuIntersectionRepository.findRsuIpsByIntersectionNumber(intersectionId)
                .stream()
                .map(inetMapper::mapInetAddressToString)
                .collect(Collectors.toList());
        dto.setRsus(rsuIps);

        log.debug("Successfully fetched intersection {}. Org count: {}, RSU count: {}", intersectionId, orgNames.size(),
                rsuIps.size());
        return new IntersectionSingleResponse(dto, buildAllowedSelections());
    }

    /**
     * Builds the AllowedSelections for the current user: the orgs and RSUs they may
     * assign
     * to an intersection. Scoped to OPERATOR-qualified orgs since OPERATOR is
     * required to modify.
     * Superusers receive all orgs and RSUs.
     */
    private AllowedSelections buildAllowedSelections() {
        if (permissionService.isSuperUser()) {
            List<String> allOrgNames = organizationRepository.findAll().stream()
                    .map(Organization::getName)
                    .collect(Collectors.toList());
            List<String> allRsuIps = rsuRepository.findAll().stream()
                    .map(rsu -> inetMapper.mapInetAddressToString(rsu.getIpv4Address()))
                    .collect(Collectors.toList());
            return new AllowedSelections(allOrgNames, allRsuIps);
        }
        var token = permissionService.getCvManagerAuthToken();
        List<String> operatorOrgs = token.getQualifiedOrgList(UserRole.OPERATOR);
        List<String> rsuIps = rsuRepository.findAllowedRsuIpsInOrganizations(operatorOrgs).stream()
                .map(inetMapper::mapInetAddressToString)
                .collect(Collectors.toList());
        return new AllowedSelections(operatorOrgs, rsuIps);
    }

    /**
     * Returns all intersections for the specified organization.
     * The organization parameter is always required; the controller enforces this
     * via a
     * mandatory request header.
     *
     * @param organization the organization to scope results to
     * @return response containing intersection_data as a list of intersections for
     *         the organization
     */
    public IntersectionListResponse getAllIntersections(String organization) {
        log.info("Fetching intersections for organization: {}", organization);
        List<Intersection> intersections = intersectionRepository.findAllByOrgNameWithOrgs(organization);

        if (intersections.isEmpty()) {
            log.warn("No intersections found for organization '{}'", organization);
            throw new EntityNotFoundException(
                    "No accessible intersections found for organization '" + organization + "'");
        }

        List<IntersectionDto> dtos = intersections.stream()
                .map(intersectionMapper::toDto)
                .collect(Collectors.toList());

        List<String> intersectionNumbers = intersections.stream()
                .map(Intersection::getIntersectionNumber)
                .collect(Collectors.toList());

        Map<Integer, List<String>> rsusByIntersection = loadRsuIpsByIntersection(intersectionNumbers);
        log.debug("RSU IP mapping resolved for {}/{} intersections.", rsusByIntersection.size(),
                intersectionNumbers.size());

        for (IntersectionDto dto : dtos) {
            dto.setRsus(rsusByIntersection.getOrDefault(dto.getIntersectionId(), Collections.emptyList()));
        }

        log.debug("Successfully fetched {} intersections.", dtos.size());
        return new IntersectionListResponse(dtos);
    }

    /**
     * Updates an intersection's properties and modifies its org/RSU relationships.
     * The controller has already enforced all authorization before this is called.
     * Wraps all writes in a single transaction.
     *
     * @param patch the patch request body
     */
    @Transactional
    public void patchIntersection(IntersectionPatch patch) {
        String origNumber = patch.getOrigIntersectionId().toString();
        String newNumber = patch.getIntersectionId().toString();

        log.info("Patching intersection. Original ID: {}, New ID: {}", origNumber, newNumber);

        Intersection intersection = intersectionRepository.findByIntersectionNumber(origNumber)
                .orElseThrow(() -> {
                    log.error("Intersection not found for patching: {}", origNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Intersection not found: " + origNumber);
                });
        log.debug("Found intersection {} for patching.", origNumber);

        // Step 1: Update the intersection record
        log.debug(
                "Step 1: Updating intersection base record fields. intersectionNumber={}, refPt={}, bbox={}, intersectionName={}, originIp={}",
                newNumber,
                patch.getRefPt(),
                patch.getBbox() != null ? "provided" : "unchanged",
                patch.getIntersectionName() != null ? patch.getIntersectionName() : "unchanged",
                patch.getOriginIp() != null ? patch.getOriginIp() : "unchanged");
        var updatedIntersection = intersectionMapper.partialUpdate(intersection, patch);

        intersectionRepository.save(updatedIntersection);
        log.debug("Step 1: Intersection base record saved.");

        // Step 2: Add org associations
        if (!patch.getOrganizationsToAdd().isEmpty()) {
            log.debug("Step 2: Adding {} organization association(s): {}", patch.getOrganizationsToAdd().size(),
                    patch.getOrganizationsToAdd());
            List<Organization> orgs = organizationRepository.findByNameIn(patch.getOrganizationsToAdd());
            if (orgs.size() != patch.getOrganizationsToAdd().size()) {
                log.warn("Step 2: Requested {} org(s) to add but only {} resolved in DB. Requested: {}",
                        patch.getOrganizationsToAdd().size(), orgs.size(), patch.getOrganizationsToAdd());
            }
            List<IntersectionOrganization> newAssocs = orgs.stream()
                    .map(org -> {
                        IntersectionOrganization io = new IntersectionOrganization();
                        io.setIntersection(intersection);
                        io.setOrganization(org);
                        return io;
                    })
                    .collect(Collectors.toList());
            intersectionOrganizationRepository.saveAll(newAssocs);
            log.debug("Step 2: Saved {} org association(s).", newAssocs.size());
        } else {
            log.debug("Step 2: No org associations to add.");
        }

        // Step 3: Remove org associations
        if (!patch.getOrganizationsToRemove().isEmpty()) {
            log.debug("Step 3: Removing {} organization association(s): {}", patch.getOrganizationsToRemove().size(),
                    patch.getOrganizationsToRemove());
            intersectionOrganizationRepository.deleteByIntersectionNumberAndOrganizationNameIn(
                    newNumber, patch.getOrganizationsToRemove());
            log.debug("Step 3: Org association removal complete.");
        } else {
            log.debug("Step 3: No org associations to remove.");
        }

        // Step 4: Add RSU associations
        if (!patch.getRsusToAdd().isEmpty()) {
            log.debug("Step 4: Adding {} RSU association(s): {}", patch.getRsusToAdd().size(), patch.getRsusToAdd());
            List<InetAddress> ipsToAdd = patch.getRsusToAdd().stream()
                    .map(inetMapper::mapStringToInetAddress)
                    .collect(Collectors.toList());
            List<Rsu> rsus = rsuRepository.findByIpv4AddressIn(ipsToAdd);
            if (rsus.size() != patch.getRsusToAdd().size()) {
                log.warn("Step 4: Requested {} RSU(s) to add but only {} resolved in DB. Requested: {}",
                        patch.getRsusToAdd().size(), rsus.size(), patch.getRsusToAdd());
            }
            List<RsuIntersection> newRsuAssocs = rsus.stream()
                    .filter(rsu -> !rsuIntersectionRepository.existsByRsuAndIntersection(rsu, intersection))
                    .map(rsu -> {
                        RsuIntersection ri = new RsuIntersection();
                        ri.setIntersection(intersection);
                        ri.setRsu(rsu);
                        return ri;
                    })
                    .collect(Collectors.toList());
            rsuIntersectionRepository.saveAll(newRsuAssocs);
            log.debug("Step 4: Saved {} RSU association(s).", newRsuAssocs.size());
        } else {
            log.debug("Step 4: No RSU associations to add.");
        }

        // Step 5: Remove RSU associations
        if (!patch.getRsusToRemove().isEmpty()) {
            log.debug("Step 5: Removing {} RSU association(s): {}", patch.getRsusToRemove().size(),
                    patch.getRsusToRemove());
            List<InetAddress> ipsToRemove = patch.getRsusToRemove().stream()
                    .map(inetMapper::mapStringToInetAddress)
                    .collect(Collectors.toList());
            rsuIntersectionRepository.deleteByIntersectionNumberAndRsuIpv4AddressIn(
                    newNumber, ipsToRemove);
            log.debug("Step 5: RSU association removal complete.");
        } else {
            log.debug("Step 5: No RSU associations to remove.");
        }
        log.info("Successfully patched intersection {}", origNumber);
    }

    /**
     * Deletes an intersection and all its relationship records.
     * The controller has already enforced all authorization before this is called.
     * Wraps all writes in a single transaction (fixes known issue #1).
     * Throws 404 if the intersection does not exist (fixes known issue #2).
     *
     * @param intersectionId the intersection_number to delete
     */
    @Transactional
    public void deleteIntersection(String intersectionId) {
        log.info("Deleting intersection with id: {}", intersectionId);
        Intersection intersection = intersectionRepository.findByIntersectionNumber(intersectionId)
                .orElseThrow(() -> {
                    log.error("Intersection not found for deletion: {}", intersectionId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Intersection not found: " + intersectionId);
                });

        // Delete in FK dependency order: intersection_organization → rsu_intersection →
        // intersections
        log.debug("Deleting relationship records for intersection {}", intersectionId);
        intersectionOrganizationRepository
                .deleteIntersectionOrganizationByIntersection_IntersectionNumber(intersectionId);
        rsuIntersectionRepository.deleteByIntersection_IntersectionNumber(intersectionId);
        intersectionRepository.delete(intersection);
        log.info("Successfully deleted intersection {}", intersectionId);
    }

    private Map<Integer, List<String>> loadRsuIpsByIntersection(List<String> intersectionNumbers) {
        log.debug("Loading RSU IPs for {} intersection(s).", intersectionNumbers.size());
        List<RsuIntersectionRepository.IntersectionRsuProjection> projections = rsuIntersectionRepository
                .findRsuIpsByIntersectionNumbers(intersectionNumbers);
        log.debug("Retrieved {} RSU-intersection projection record(s).", projections.size());
        Map<Integer, List<String>> result = new HashMap<>();
        for (RsuIntersectionRepository.IntersectionRsuProjection proj : projections) {
            String ip = inetMapper.mapInetAddressToString(proj.getRsuIp());
            result.computeIfAbsent(proj.getIntersectionNumber(), _ -> new ArrayList<>()).add(ip);
        }
        return result;
    }

}
