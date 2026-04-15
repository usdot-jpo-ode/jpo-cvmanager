package us.dot.its.jpo.ode.api.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

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

    public UserDto getUser(String email) {
        return userMapper.toDto(userRepository.findByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email)));
    }

    public Page<UserDto> getUsers(String orgName, String search, Pageable pageable) {
        Page<User> users = userRepository.findAllByOrganization(orgName, search, pageable);
        return users.map(userMapper::toDto);
    }

    public ModifyUserAllowedSelections getAllowedSelections(CvManagerAuthToken authToken) {
        ModifyUserAllowedSelections allowed = new ModifyUserAllowedSelections();

        allowed.setRoles(roleRepository.findAllRoleNames());
        allowed.setOrganizations(authToken.getQualifiedOrgList(UserRole.ADMIN));

        return allowed;
    }

    @Transactional
    public UserDto modifyUser(String email, UserPatch userPatch, CvManagerAuthToken authToken) {
        List<String> authorizedOrgs = authToken.getQualifiedOrgList(UserRole.ADMIN);

        // 1. Find existing User by email
        User existingUser = userRepository.findByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

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
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User does not have permission to add User to organization(s): "
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
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Organization not found: " + org.getOrganization()));

                    Role role = roleRepository.findByName(org.getRole())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Role not found: " + org.getRole()));

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
            List<UserOrganizationDto> unqualifiedRemoves = patch.getOrganizationsToRemove().stream()
                    .filter(org -> !authorizedOrgs.contains(org.getOrganization()))
                    .toList();
            if (!unqualifiedRemoves.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User does not have permission to remove User from organization(s): "
                                + String.join(", ", unqualifiedRemoves.stream()
                                        .map(UserOrganizationDto::getOrganization).toList()));
            }
            for (UserOrganizationDto org : patch.getOrganizationsToRemove()) {
                // Find and delete the specific association
                userOrganizationRepository.findByUserAndOrganization_Name(
                        user,
                        org.getOrganization()).ifPresent(userOrganizationRepository::delete);
            }
        }
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        // Check if User exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User(s) not found with email(s): " + String.join(", ", missingEmails));
        } else if (existingUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid user emails provided");
        }

        userOrganizationRepository.removeMultipleUserOrganizationsByEmail(emails);
        userRepository.deleteAll(existingUsers);
    }
}