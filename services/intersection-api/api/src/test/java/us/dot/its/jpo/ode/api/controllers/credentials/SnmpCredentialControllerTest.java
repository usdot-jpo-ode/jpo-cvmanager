package us.dot.its.jpo.ode.api.controllers.credentials;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import us.dot.its.jpo.ode.api.mappers.SnmpCredentialMapper;
import us.dot.its.jpo.ode.api.mappers.SnmpCredentialMapperImpl;
import us.dot.its.jpo.ode.api.models.credentials.SnmpCredentialDTO;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpCredential;
import us.dot.its.jpo.ode.api.services.SnmpCredentialManagementService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnmpCredentialControllerTest {

    SnmpCredentialManagementService mockSnmpCredentialManagementService;

    SnmpCredentialMapper snmpCredentialMapper;

    SnmpCredentialController snmpCredentialController;

    @BeforeEach
    void setUp() {
        mockSnmpCredentialManagementService = mock(SnmpCredentialManagementService.class);
        snmpCredentialMapper = new SnmpCredentialMapperImpl();
    }

    @Test
    void testCreateSnmpCredential_Success() throws SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException, EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        String organization = "organization";
        int mockOrganizationId = 1;
        SnmpCredentialController.SnmpCredentialCreateRequest request = new SnmpCredentialController.SnmpCredentialCreateRequest(nickname, username, password, organization);

        SnmpCredential snmpCredential = new SnmpCredential();
        snmpCredential.setNickname(nickname);
        snmpCredential.setUsername(username);
        snmpCredential.setPassword(password);
        us.dot.its.jpo.ode.api.models.postgres.tables.Organization mockOrganization = mock(us.dot.its.jpo.ode.api.models.postgres.tables.Organization.class);
        when(mockOrganization.getId()).thenReturn(mockOrganizationId);
        snmpCredential.setOwnerOrganization(mockOrganization);

        SnmpCredentialDTO expected = new SnmpCredentialDTO(null, nickname, username, password, mockOrganizationId);

        when(mockSnmpCredentialManagementService.create(request)).thenReturn(snmpCredential);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

        // Act
        SnmpCredentialDTO response = snmpCredentialController.createSnmpCredential(request);

        // Assert
        assertNotNull(response);
        assertEquals(expected, response);
        verify(mockSnmpCredentialManagementService).create(request);
    }

    @Test
    void testCreateSnmpCredential_Failure_AlreadyExists() throws SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException, EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        String organization = "organization";
        SnmpCredentialController.SnmpCredentialCreateRequest request = new SnmpCredentialController.SnmpCredentialCreateRequest(nickname, username, password, organization);
        when(mockSnmpCredentialManagementService.create(request)).thenThrow(SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException.class);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

        // Act & Assert
        assertThrows(SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException.class, () -> snmpCredentialController.createSnmpCredential(request));
    }

    @Test
    void testCreateSnmpCredential_Failure_OrganizationNotFound() throws SnmpCredentialManagementService.SnmpCredentialAlreadyExistsException, EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        String organization = "organization";
        SnmpCredentialController.SnmpCredentialCreateRequest request = new SnmpCredentialController.SnmpCredentialCreateRequest(nickname, username, password, organization);
        when(mockSnmpCredentialManagementService.create(request)).thenThrow(EntityNotFoundException.class);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> snmpCredentialController.createSnmpCredential(request));
    }

    @Test
    void testGetByNickname_Success() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        int mockRsuCredentialId = 1;
        int mockOrganizationId = 1;

        SnmpCredentialController.SnmpCredentialGetRequest request = new SnmpCredentialController.SnmpCredentialGetRequest(nickname);

        SnmpCredential existingCredential = new SnmpCredential();
        existingCredential.setId(mockRsuCredentialId);
        existingCredential.setNickname(nickname);
        existingCredential.setUsername(username);
        existingCredential.setPassword(password);
        us.dot.its.jpo.ode.api.models.postgres.tables.Organization mockOrganization = mock(us.dot.its.jpo.ode.api.models.postgres.tables.Organization.class);
        when(mockOrganization.getId()).thenReturn(mockOrganizationId);
        existingCredential.setOwnerOrganization(mockOrganization);

        SnmpCredentialDTO expected = new SnmpCredentialDTO(mockRsuCredentialId, nickname, username, password, mockOrganizationId);

        when(mockSnmpCredentialManagementService.getByNickname(nickname)).thenReturn(existingCredential);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

        // Act
        SnmpCredentialDTO actual = snmpCredentialController.getByNickname(request);

        // Assert
        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(mockSnmpCredentialManagementService).getByNickname(nickname);
    }

    @Test
    void testGetByNickname_Failure_NotFound() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        SnmpCredentialController.SnmpCredentialGetRequest request = new SnmpCredentialController.SnmpCredentialGetRequest(nickname);
        when(mockSnmpCredentialManagementService.getByNickname(nickname)).thenThrow(EntityNotFoundException.class);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> snmpCredentialController.getByNickname(request));
    }

    @Test
    void testUpdate_Success() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        String username = "username";
        String updatedPassword = "updatedPassword";
        int mockRsuCredentialId = 1;
        int mockOrganizationId = 2;
       SnmpCredential snmpCredential = new SnmpCredential();
       snmpCredential.setId(mockRsuCredentialId);
       snmpCredential.setNickname(nickname);
       snmpCredential.setUsername(username);
       snmpCredential.setPassword(updatedPassword);
       us.dot.its.jpo.ode.api.models.postgres.tables.Organization mockOrganization = mock(us.dot.its.jpo.ode.api.models.postgres.tables.Organization.class);
       when(mockOrganization.getId()).thenReturn(mockOrganizationId);
       snmpCredential.setOwnerOrganization(mockOrganization);

       SnmpCredentialController.SnmpCredentialPatch patch = new SnmpCredentialController.SnmpCredentialPatch(nickname);
       patch.setPassword(updatedPassword);

       when(mockSnmpCredentialManagementService.update(patch)).thenReturn(snmpCredential);
       snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

       SnmpCredentialDTO expected = new SnmpCredentialDTO(mockRsuCredentialId, nickname, username, updatedPassword, mockOrganizationId);

       // Act
       SnmpCredentialDTO response = snmpCredentialController.update(patch);

       // Assert
       assertNotNull(response);
       assertEquals(expected, response);
       verify(mockSnmpCredentialManagementService).update(patch);
    }

    @Test
    void testUpdate_Failure_EntityNotFound() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        SnmpCredentialController.SnmpCredentialPatch patch = new SnmpCredentialController.SnmpCredentialPatch(nickname);
        patch.setPassword("");
        when(mockSnmpCredentialManagementService.update(patch)).thenThrow(EntityNotFoundException.class);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> snmpCredentialController.update(patch));
    }

    @Test
    void testDeleteByNickname_Success() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";

        SnmpCredentialController.SnmpCredentialDeleteRequest request = new SnmpCredentialController.SnmpCredentialDeleteRequest(nickname);

        doNothing().when(mockSnmpCredentialManagementService).deleteByNickname(nickname);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);

        // Act
        snmpCredentialController.deleteByNickname(request);

        // Assert
        verify(mockSnmpCredentialManagementService).deleteByNickname(nickname);
    }

    @Test
    void testDelete_Failure() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        doThrow(EntityNotFoundException.class).when(mockSnmpCredentialManagementService).deleteByNickname(nickname);
        snmpCredentialController = new SnmpCredentialController(mockSnmpCredentialManagementService, snmpCredentialMapper);
        SnmpCredentialController.SnmpCredentialDeleteRequest request = new SnmpCredentialController.SnmpCredentialDeleteRequest(nickname);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> snmpCredentialController.deleteByNickname(request));

    }

}