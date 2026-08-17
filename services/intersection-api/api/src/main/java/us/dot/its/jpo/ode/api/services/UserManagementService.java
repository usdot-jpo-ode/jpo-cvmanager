package us.dot.its.jpo.ode.api.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import us.dot.its.jpo.ode.api.keycloak.config.KeycloakAdminConfig;
import us.dot.its.jpo.ode.api.mappers.UserMapper;
import us.dot.its.jpo.ode.api.mappers.UserPatchMapper;
import us.dot.its.jpo.ode.api.models.users.ModifyUserAllowedSelections;
import us.dot.its.jpo.ode.api.models.users.UserDto;
import us.dot.its.jpo.ode.api.models.users.UserOrganizationDto;
import us.dot.its.jpo.ode.api.models.users.UserPatch;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.repositories.OrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RoleRepository;
import us.dot.its.jpo.ode.api.repositories.UserOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;
import us.dot.its.jpo.ode.api.models.postgres.tables.Role;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.models.postgres.tables.UserOrganization;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final OrganizationRepository organizationRepository;
    private final UserMapper userMapper;
    private final UserPatchMapper userPatchMapper;
    private final KeycloakAdminConfig keycloakAdminConfig;

    public UserDto getUser(String email) {
        return userMapper.toDto(userRepository.findByEmail(email));
    }

    public Page<UserDto> getUsers(String orgName, String search, Pageable pageable) {
        Page<User> users = userRepository.findAllByOrganization(orgName, search, pageable);
        return users.map(userMapper::toDto);
    }

    public ModifyUserAllowedSelections getAllowedSelections(CvManagerAuthToken authToken) {
        ModifyUserAllowedSelections allowed = new ModifyUserAllowedSelections();

        allowed.setRoles(roleRepository.findAllRoleNames());

        if (authToken.isSuperUser()) {
            allowed.setOrganizations(organizationRepository.findAllOrganizationNames());
        } else {
            allowed.setOrganizations(authToken.getQualifiedOrgList(UserRole.ADMIN));
        }

        return allowed;
    }

    @Transactional
    public User createUser(UserDto userDto) {
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(userDto.getEmail());
        kcUser.setEmail(userDto.getEmail());
        kcUser.setFirstName(userDto.getFirstName());
        kcUser.setLastName(userDto.getLastName());
        kcUser.setEnabled(true);

        try (Response userCreationResponse = keycloakAdminConfig.keyCloakBuilder()
                .realm(keycloakAdminConfig.getRealm())
                .users()
                .create(kcUser)) {
            if (userCreationResponse.getStatus() != 201) {
                if (userCreationResponse.getStatus() == 409) {
                    throw new UserEmailAlreadyExistsException(
                            "A user with the email " + userDto.getEmail() + " already exists in keycloak.");
                } else {
                    throw new RuntimeException(
                            "Failed to create user in Keycloak: " + userCreationResponse.getEntity());
                }
            }
        }

        User createdUser = userRepository.findByEmail(userDto.getEmail());

        // Add super user
        if (userDto.getSuperUser()) {
            createdUser.setSuperUser(true);
            createdUser = userRepository.save(createdUser);
        }

        var toCreate = new ArrayList<UserOrganization>();
        for (UserOrganizationDto userOrgDto : userDto.getOrganizations()) {
            toCreate.add(createUserOrgRelationship(userOrgDto, createdUser));
        }
        userOrganizationRepository.saveAll(toCreate);

        return createdUser;
    }

    public UserOrganization createUserOrgRelationship(UserOrganizationDto userOrgDto, User user) {
        Organization organization = organizationRepository.findByName(userOrgDto.getOrganization())
                .orElseThrow(
                        () -> new IllegalArgumentException("Organization not found: " + userOrgDto.getOrganization()));

        Role role = roleRepository.findByNameIgnoreCase(userOrgDto.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + userOrgDto.getRole()));

        UserOrganization userOrg = new UserOrganization();
        userOrg.setOrganization(organization);
        userOrg.setUser(user);
        userOrg.setRole(role);
        return userOrg;
    }

    @Transactional
    public UserDto modifyUser(String email, UserPatch userPatch, CvManagerAuthToken authToken) {
        List<String> authorizedOrgs;
        if (authToken.isSuperUser()) {
            authorizedOrgs = organizationRepository.findAllOrganizationNames();
        } else {
            authorizedOrgs = authToken.getQualifiedOrgList(UserRole.ADMIN);
        }

        // 1. Find existing User by email
        User existingUser = userRepository.findByEmail(email);

        // 2. Update only non-null fields using MapStruct
        userPatchMapper.updateUserFromPatch(userPatch, existingUser);

        // 3. Handle organization additions/removals
        handleOrganizationChanges(existingUser, userPatch, authorizedOrgs);

        // 4. Save updated entity (JPA handles UPDATE SQL)
        User savedUser = userRepository.save(existingUser);

        // 5. Return DTO
        return userMapper.toDto(savedUser);
    }

    private void handleOrganizationChanges(User user, UserPatch patch, List<String> authorizedOrgs) {

        // Add organizations
        if (patch.getOrganizationsToAdd() != null && !patch.getOrganizationsToAdd().isEmpty()) {
            List<UserOrganizationDto> unqualifiedAdds = patch.getOrganizationsToAdd().stream()
                    .filter(org -> !authorizedOrgs.contains(org.getOrganization()))
                    .toList();
            if (!unqualifiedAdds.isEmpty()) {
                throw new AccessDeniedException("User does not have permission to add User to organization(s): "
                        + String.join(", ",
                                unqualifiedAdds.stream().map(UserOrganizationDto::getOrganization).toList()));
            }
            for (UserOrganizationDto org : patch.getOrganizationsToAdd()) {
                // Check if already associated
                boolean exists = userRepository.existsByEmailAndOrganizations(
                        user.getEmail(),
                        List.of(org.getOrganization()));

                if (!exists) {
                    Organization organization = organizationRepository.findByName(org.getOrganization())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Organization not found: " + org.getOrganization()));

                    Role role = roleRepository.findByNameIgnoreCase(org.getRole())
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + org.getRole()));

                    UserOrganization userOrg = new UserOrganization();
                    userOrg.setUser(user);
                    userOrg.setRole(role);
                    userOrg.setOrganization(organization);

                    // Save to repository
                    userOrganizationRepository.save(userOrg);
                }
            }
        }

        // Remove organizations
        if (patch.getOrganizationsToRemove() != null && !patch.getOrganizationsToRemove().isEmpty()) {
            List<String> unqualifiedRemoves = patch.getOrganizationsToRemove().stream()
                    .filter(org -> !authorizedOrgs.contains(org))
                    .toList();
            if (!unqualifiedRemoves.isEmpty()) {
                throw new AccessDeniedException("User does not have permission to remove User from organization(s): "
                        + String.join(", ", unqualifiedRemoves));
            }
            for (String org : patch.getOrganizationsToRemove()) {
                // Find and delete the specific association
                userOrganizationRepository.findByUserAndOrganization_Name(
                        user,
                        org).ifPresent(userOrganizationRepository::delete);
            }
        }

        if (patch.getOrganizationsToModify() != null && !patch.getOrganizationsToModify().isEmpty()) {
            List<UserOrganizationDto> unqualifiedModifies = patch.getOrganizationsToModify().stream()
                    .filter(org -> !authorizedOrgs.contains(org.getOrganization()))
                    .toList();
            if (!unqualifiedModifies.isEmpty()) {
                throw new AccessDeniedException(
                        "User does not have permission to modify User's role in organization(s): "
                                + String.join(", ", unqualifiedModifies.stream()
                                        .map(UserOrganizationDto::getOrganization).toList()));
            }
            for (UserOrganizationDto org : patch.getOrganizationsToModify()) {
                userOrganizationRepository.findByUserAndOrganization_Name(
                        user,
                        org.getOrganization()).ifPresent(userOrg -> {
                            Role role = roleRepository.findByNameIgnoreCase(org.getRole())
                                    .orElseThrow(
                                            () -> new IllegalArgumentException(
                                                    "Role not found: " + org.getRole()));
                            userOrg.setRole(role);
                            userOrganizationRepository.save(userOrg);
                        });
            }
        }
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        // Check if User exists
        User user = userRepository.findByEmail(email);

        // Delete related entities first to maintain referential integrity
        userOrganizationRepository.removeUserOrganizationByEmail(email);

        // Finally, delete the User
        userRepository.delete(user);
    }

    @Transactional
    public void deleteMultipleUsersByEmail(List<String> emails) {

        // Check if all Users exist
        List<User> existingUsers = userRepository.findByEmailIn(emails);
        if (existingUsers.size() != emails.size()) {
            // Find which emails don't exist
            List<String> existingEmails = existingUsers.stream()
                    .map(user -> user.getEmail())
                    .toList();
            List<String> missingEmails = emails.stream()
                    .filter(email -> !existingEmails.contains(email))
                    .toList();
            throw new EntityNotFoundException(
                    "User(s) not found with email(s): " + String.join(", ", missingEmails));
        } else if (existingUsers.isEmpty()) {
            throw new IllegalArgumentException("No valid user emails provided");
        }

        userOrganizationRepository.removeMultipleUserOrganizationsByEmail(emails);
        userRepository.deleteAll(existingUsers);
    }

    public static class UserEmailAlreadyExistsException extends RuntimeException {
        public UserEmailAlreadyExistsException(String message) {
            super(message);
        }
    }
}