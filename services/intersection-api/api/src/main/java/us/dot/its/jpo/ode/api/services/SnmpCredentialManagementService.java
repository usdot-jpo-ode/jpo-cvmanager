package us.dot.its.jpo.ode.api.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import us.dot.its.jpo.ode.api.controllers.credentials.SnmpCredentialController;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpCredential;
import us.dot.its.jpo.ode.api.repositories.OrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpCredentialRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnmpCredentialManagementService {
    private final SnmpCredentialRepository snmpCredentialRepository;
    private final OrganizationRepository organizationRepository;

    public SnmpCredential create(SnmpCredentialController.SnmpCredentialCreateRequest request) throws SnmpCredentialAlreadyExistsException, EntityNotFoundException {
        if (snmpCredentialRepository.existsByNickname(request.getNickname())) {
            throw new SnmpCredentialAlreadyExistsException("A credential with nickname " + request.getNickname() + " already exists.");
        }
        SnmpCredential snmpCredential = new SnmpCredential();
        snmpCredential.setNickname(request.getNickname());
        snmpCredential.setUsername(request.getUsername());
        snmpCredential.setPassword(request.getPassword());

        Optional<Organization> organization = organizationRepository.findByName(request.getOrganization());
        if (organization.isEmpty()) {
            throw new EntityNotFoundException("Organization " + request.getOrganization() + " not found.");
        }
        snmpCredential.setOwnerOrganization(organization.get());

        return snmpCredentialRepository.save(snmpCredential);
    }

    public SnmpCredential getByNickname(String nickname) throws EntityNotFoundException {
        return snmpCredentialRepository.findByNickname(nickname).orElseThrow(() -> new EntityNotFoundException("No credential found with nickname " + nickname));
    }

    public SnmpCredential update(SnmpCredentialController.SnmpCredentialPatch patch) throws EntityNotFoundException {
        SnmpCredential credential = snmpCredentialRepository.findByNickname(patch.getNickname()).orElseThrow(() -> new EntityNotFoundException("No credential found with nickname " + patch.getNickname()));

        if (patch.getUsername() != null) {
            credential.setUsername(patch.getUsername());
        }
        if (patch.getPassword() != null) {
            credential.setPassword(patch.getPassword());
        }
        if (patch.getOrganization() != null) {
            Optional<Organization> newOrganization = organizationRepository.findByName(patch.getOrganization());
            if (newOrganization.isEmpty()) {
                throw new EntityNotFoundException("Organization " + patch.getOrganization() + " not found.");
            }
            credential.setOwnerOrganization(newOrganization.get());
        }
        return snmpCredentialRepository.save(credential);
    }

    public void deleteByNickname(String nickname) throws EntityNotFoundException {
        Optional<SnmpCredential> credentialOptional = snmpCredentialRepository.findByNickname(nickname);
        if (credentialOptional.isEmpty()) {
            throw new EntityNotFoundException("No credential found with nickname " + nickname);
        }
        SnmpCredential credential = credentialOptional.get();

        snmpCredentialRepository.delete(credential);
    }

    public static class SnmpCredentialAlreadyExistsException extends Exception {
        public SnmpCredentialAlreadyExistsException(String message) {
            super(message);
        }
    }
}
