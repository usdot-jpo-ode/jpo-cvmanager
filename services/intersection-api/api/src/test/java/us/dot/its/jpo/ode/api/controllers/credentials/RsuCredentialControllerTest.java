package us.dot.its.jpo.ode.api.controllers.credentials;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import us.dot.its.jpo.ode.api.mappers.RsuCredentialMapper;
import us.dot.its.jpo.ode.api.mappers.RsuCredentialMapperImpl;
import us.dot.its.jpo.ode.api.models.credentials.RsuCredentialDTO;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuCredential;
import us.dot.its.jpo.ode.api.services.RsuCredentialManagementService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RsuCredentialControllerTest {

    RsuCredentialManagementService mockRsuCredentialManagementService;

    RsuCredentialMapper rsuCredentialMapper;

    RsuCredentialController rsuCredentialController;

    @BeforeEach()
    void setUp() {
        mockRsuCredentialManagementService = mock(RsuCredentialManagementService.class);
        rsuCredentialMapper = new RsuCredentialMapperImpl();
    }

    @Test
    void testCreateRsuCredential_Success() throws RsuCredentialManagementService.RsuCredentialAlreadyExistsException, EntityNotFoundException {
        // Arrange
        String nickname = "testNickname";
        String username = "testUser";
        String password = "testPassword";
        String organization = "testOrg";
        int mockRsuCredentialId = 1;
        int mockOrganizationId = 2;
        RsuCredential mockRsuCredential = mock();
        when(mockRsuCredential.getId()).thenReturn(mockRsuCredentialId);
        when(mockRsuCredential.getNickname()).thenReturn(nickname);
        when(mockRsuCredential.getUsername()).thenReturn(username);
        when(mockRsuCredential.getPassword()).thenReturn(password);
        
        Organization mockOrganization = mock(Organization.class);
        when(mockOrganization.getId()).thenReturn(mockOrganizationId);
        when(mockRsuCredential.getOwnerOrganization()).thenReturn(mockOrganization);
        
        RsuCredentialController.RsuCredentialCreateRequest rsuCredentialCreateRequest = new RsuCredentialController.RsuCredentialCreateRequest(nickname, username, password, organization);
        when(mockRsuCredentialManagementService.create(rsuCredentialCreateRequest)).thenReturn(mockRsuCredential);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        RsuCredentialDTO expected = new RsuCredentialDTO(mockRsuCredentialId, nickname, username, password, mockOrganizationId);

        // Act
        RsuCredentialDTO response = rsuCredentialController.createRsuCredential(rsuCredentialCreateRequest);

        // Assert
        assert(response != null);
        assert(response.equals(expected));
        verify(mockRsuCredentialManagementService).create(rsuCredentialCreateRequest);
    }

    @Test
    void testCreateRsuCredential_Failure_AlreadyExists() throws EntityNotFoundException, RsuCredentialManagementService.RsuCredentialAlreadyExistsException {
        // Arrange
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        String organization = "organization";
        RsuCredentialController.RsuCredentialCreateRequest rsuCredentialCreateRequest = new RsuCredentialController.RsuCredentialCreateRequest(nickname, username, password, organization);
        when(mockRsuCredentialManagementService.create(rsuCredentialCreateRequest)).thenThrow(RsuCredentialManagementService.RsuCredentialAlreadyExistsException.class);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        // Act & Assert
        assertThrows(RsuCredentialManagementService.RsuCredentialAlreadyExistsException.class, () -> rsuCredentialController.createRsuCredential(rsuCredentialCreateRequest));
    }

    @Test
    void testCreateRsuCredential_Failure_OrganizationNotFound() throws EntityNotFoundException, RsuCredentialManagementService.RsuCredentialAlreadyExistsException {
        // Arrange
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        String organization = "organization";
        RsuCredentialController.RsuCredentialCreateRequest rsuCredentialCreateRequest = new RsuCredentialController.RsuCredentialCreateRequest(nickname, username, password, organization);
        doThrow(EntityNotFoundException.class).when(mockRsuCredentialManagementService).create(rsuCredentialCreateRequest);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rsuCredentialController.createRsuCredential(rsuCredentialCreateRequest));
    }

    @Test
    void testGetByNickname_Success() throws EntityNotFoundException {
        // Arrange
        String nickname = "testNickname";
        String username = "testUser";
        String password = "testPassword";
        int mockRsuCredentialId = 1;
        int mockOrganizationId = 2;
        RsuCredential mockRsuCredential = mock();
        when(mockRsuCredential.getId()).thenReturn(mockRsuCredentialId);
        when(mockRsuCredential.getNickname()).thenReturn(nickname);
        when(mockRsuCredential.getUsername()).thenReturn(username);
        when(mockRsuCredential.getPassword()).thenReturn(password);
        
        Organization mockOrganization = mock(Organization.class);
        when(mockOrganization.getId()).thenReturn(mockOrganizationId);
        when(mockRsuCredential.getOwnerOrganization()).thenReturn(mockOrganization);
        when(mockRsuCredentialManagementService.getByNickname(nickname)).thenReturn(mockRsuCredential);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        RsuCredentialController.RsuCredentialGetRequest rsuCredentialGetRequest = new RsuCredentialController.RsuCredentialGetRequest(nickname);
        RsuCredentialDTO expected = new RsuCredentialDTO(mockRsuCredentialId, nickname, username, password, mockOrganizationId);

        // Act
        RsuCredentialDTO actual = rsuCredentialController.getByNickname(rsuCredentialGetRequest);

        // Assert
        assert(actual != null);
        assert(actual.equals(expected));
        verify(mockRsuCredentialManagementService).getByNickname(nickname);
    }

    @Test
    void testGetByNickname_Failure_NotFound() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        when(mockRsuCredentialManagementService.getByNickname(nickname)).thenThrow(EntityNotFoundException.class);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);
        RsuCredentialController.RsuCredentialGetRequest rsuCredentialGetRequest = new RsuCredentialController.RsuCredentialGetRequest(nickname);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rsuCredentialController.getByNickname(rsuCredentialGetRequest));
    }

    @Test
    void testUpdate_Success() throws EntityNotFoundException {
        // Arrange
        String nickname = "testNickname";
        String username = "testUser";
        String updatedPassword = "updatedPassword";
        int mockRsuCredentialId = 1;
        int mockOrganizationId = 2;
        RsuCredential mockUpdatedRsuCredential = mock();
        when(mockUpdatedRsuCredential.getId()).thenReturn(mockRsuCredentialId);
        when(mockUpdatedRsuCredential.getNickname()).thenReturn(nickname);
        when(mockUpdatedRsuCredential.getUsername()).thenReturn(username);
        
        Organization mockOrganization = mock(Organization.class);
        when(mockOrganization.getId()).thenReturn(mockOrganizationId);
        when(mockUpdatedRsuCredential.getOwnerOrganization()).thenReturn(mockOrganization);
        
        when(mockUpdatedRsuCredential.getPassword()).thenReturn(updatedPassword);

        RsuCredentialController.RsuCredentialPatch rsuCredentialPatch = new RsuCredentialController.RsuCredentialPatch(nickname);
        rsuCredentialPatch.setPassword(updatedPassword);

        when(mockRsuCredentialManagementService.update(rsuCredentialPatch)).thenReturn(mockUpdatedRsuCredential);

        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        // Act
        RsuCredentialDTO response = rsuCredentialController.update(rsuCredentialPatch);

        // Assert
        assert(response != null);
        assert(response.getId().equals(mockRsuCredentialId));
        verify(mockRsuCredentialManagementService).update(rsuCredentialPatch);
    }

    @Test
    void testUpdate_Failure_CredentialNotFound() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        RsuCredentialController.RsuCredentialPatch rsuCredentialPatch = new RsuCredentialController.RsuCredentialPatch(nickname);
        rsuCredentialPatch.setPassword("");
        when(mockRsuCredentialManagementService.update(rsuCredentialPatch)).thenThrow(EntityNotFoundException.class);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rsuCredentialController.update(rsuCredentialPatch));
    }

    @Test
    void testUpdate_Failure_OrganizationNotFound() throws EntityNotFoundException {
        // Arrange
        String nickname = "nickname";
        RsuCredentialController.RsuCredentialPatch rsuCredentialPatch = new RsuCredentialController.RsuCredentialPatch(nickname);
        when(mockRsuCredentialManagementService.update(rsuCredentialPatch)).thenThrow(EntityNotFoundException.class);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rsuCredentialController.update(rsuCredentialPatch));
    }

    @Test
    void testDelete_Success() {
        // Arrange
        String nickname = "testNickname";

        doNothing().when(mockRsuCredentialManagementService).deleteByNickname(nickname);

        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);

        RsuCredentialController.RsuCredentialDeleteRequest deleteRequest = new RsuCredentialController.RsuCredentialDeleteRequest(nickname);

        // Act
        rsuCredentialController.deleteByNickname(deleteRequest);

        // Assert
        verify(mockRsuCredentialManagementService).deleteByNickname(nickname);
    }

    @Test
    void testDelete_Failure() {
        // Arrange
        String nickname = "nickname";
        doThrow(EntityNotFoundException.class).when(mockRsuCredentialManagementService).deleteByNickname(nickname);
        rsuCredentialController = new RsuCredentialController(mockRsuCredentialManagementService, rsuCredentialMapper);
        RsuCredentialController.RsuCredentialDeleteRequest deleteRequest = new RsuCredentialController.RsuCredentialDeleteRequest(nickname);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rsuCredentialController.deleteByNickname(deleteRequest));
    }

}