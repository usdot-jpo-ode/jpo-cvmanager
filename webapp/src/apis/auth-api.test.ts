import { jwtDecode } from 'jwt-decode'
import AuthApi from './auth-api'
import EnvironmentVars from '../EnvironmentVars'
import { AuthToken } from '../models/AuthToken'
import { vi } from 'vitest'

// Mock jwt-decode
vi.mock('jwt-decode')

// Mock EnvironmentVars
vi.mock('../EnvironmentVars', () => ({
  default: {
    KEYCLOAK_HOST_URL: 'http://localhost:8084/',
    KEYCLOAK_REALM: 'cvmanager',
  },
}))

describe('AuthApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('parseToken', () => {
    it('should successfully decode a valid JWT token', () => {
      const mockToken = 'valid.jwt.token'
      const mockDecodedToken: AuthToken = {
        exp: 1770396901,
        iat: 1770395101,
        jti: '1b921158-2396-461f-8b93-cee772503c2e',
        iss: 'http://localhost:8084/realms/cvmanager',
        aud: 'account',
        sub: 'fc3d8729-8526-4aaa-805b-d64bf3b93860',
        typ: 'Bearer',
        azp: 'cvmanager-gui',
        sid: '75f26b63-1df5-4b9a-953b-61fd72bd8c6b',
        acr: '1',
        'allowed-origins': ['http://localhost:3000'],
        realm_access: { roles: ['offline_access'] },
        resource_access: { account: { roles: ['manage-account'] } },
        scope: 'openid email profile',
        email_verified: false,
        name: 'Test User',
        preferred_username: 'test@gmail.com',
        given_name: 'Test',
        family_name: 'User',
        cvmanager_data: {
          super_user: '1',
          organizations: [{ org: 'Test Org', role: 'admin' }],
          user_created_timestamp: 1746773527283,
        },
        email: 'test@gmail.com',
      }

      ;(jwtDecode as any).mockReturnValue(mockDecodedToken)

      const result = AuthApi.parseToken(mockToken)

      expect(jwtDecode).toHaveBeenCalledWith(mockToken)
      expect(result).toEqual(mockDecodedToken)
    })

    it('should throw error when JWT token is invalid', () => {
      const invalidToken = 'invalid.token'
      const mockError = new Error('Invalid token')

      ;(jwtDecode as any).mockImplementation(() => {
        throw mockError
      })

      expect(() => AuthApi.parseToken(invalidToken)).toThrow('Invalid JWT token')
      expect(jwtDecode).toHaveBeenCalledWith(invalidToken)
    })
  })

  describe('verifyToken', () => {
    it('should return true when Keycloak userinfo endpoint returns 200', async () => {
      const mockToken = 'valid.token'
      const mockResponse = {
        ok: true,
        status: 200,
      }

      global.fetch = vi.fn().mockResolvedValue(mockResponse)

      const result = await AuthApi.verifyToken(mockToken)

      expect(global.fetch).toHaveBeenCalledWith(
        `${EnvironmentVars.KEYCLOAK_HOST_URL}/realms/${EnvironmentVars.KEYCLOAK_REALM}/protocol/openid-connect/userinfo`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${mockToken}`,
          },
        }
      )
      expect(result).toBe(true)
    })

    it('should return false when Keycloak userinfo endpoint returns 401', async () => {
      const mockToken = 'invalid.token'
      const mockResponse = {
        ok: false,
        status: 401,
      }

      global.fetch = vi.fn().mockResolvedValue(mockResponse)

      const result = await AuthApi.verifyToken(mockToken)

      expect(global.fetch).toHaveBeenCalledWith(
        `${EnvironmentVars.KEYCLOAK_HOST_URL}/realms/${EnvironmentVars.KEYCLOAK_REALM}/protocol/openid-connect/userinfo`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${mockToken}`,
          },
        }
      )
      expect(result).toBe(false)
    })

    it('should return false when fetch throws an error', async () => {
      const mockToken = 'valid.token'
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      global.fetch = vi.fn().mockRejectedValue(new Error('Network error'))

      const result = await AuthApi.verifyToken(mockToken)

      expect(result).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalledWith('Token verification failed:', expect.any(Error))

      consoleErrorSpy.mockRestore()
    })
  })

  describe('getUserAuthResponse', () => {
    it('should convert AuthToken to UserAuthResponse format', () => {
      const mockAuthToken: AuthToken = {
        exp: 1770396901,
        iat: 1770395101,
        jti: '1b921158-2396-461f-8b93-cee772503c2e',
        iss: 'http://localhost:8084/realms/cvmanager',
        aud: 'account',
        sub: 'fc3d8729-8526-4aaa-805b-d64bf3b93860',
        typ: 'Bearer',
        azp: 'cvmanager-gui',
        sid: '75f26b63-1df5-4b9a-953b-61fd72bd8c6b',
        acr: '1',
        'allowed-origins': ['http://localhost:3000'],
        realm_access: { roles: ['offline_access'] },
        resource_access: { account: { roles: ['manage-account'] } },
        scope: 'openid email profile',
        email_verified: false,
        name: 'Test User',
        preferred_username: 'test@gmail.com',
        given_name: 'Test',
        family_name: 'User',
        cvmanager_data: {
          super_user: '1',
          organizations: [
            { org: 'Test Org', role: 'admin' },
            { org: 'Test Org 2', role: 'user' },
          ],
          user_created_timestamp: 1746773527283,
        },
        email: 'test@gmail.com',
      }

      const result = AuthApi.getUserAuthResponse(mockAuthToken)

      expect(result).toEqual({
        email: 'test@gmail.com',
        first_name: 'Test',
        last_name: 'User',
        name: 'Test User',
        super_user: true,
        organizations: [
          { organization: 'Test Org', role: 'ADMIN' },
          { organization: 'Test Org 2', role: 'USER' },
        ],
      })
    })

    it('should convert super_user "0" to false', () => {
      const mockAuthToken: AuthToken = {
        exp: 1770396901,
        iat: 1770395101,
        jti: '1b921158-2396-461f-8b93-cee772503c2e',
        iss: 'http://localhost:8084/realms/cvmanager',
        aud: 'account',
        sub: 'fc3d8729-8526-4aaa-805b-d64bf3b93860',
        typ: 'Bearer',
        azp: 'cvmanager-gui',
        sid: '75f26b63-1df5-4b9a-953b-61fd72bd8c6b',
        acr: '1',
        'allowed-origins': ['http://localhost:3000'],
        realm_access: { roles: ['offline_access'] },
        resource_access: { account: { roles: ['manage-account'] } },
        scope: 'openid email profile',
        email_verified: false,
        name: 'Regular User',
        preferred_username: 'user@gmail.com',
        given_name: 'Regular',
        family_name: 'User',
        cvmanager_data: {
          super_user: '0',
          organizations: [{ org: 'Test Org', role: 'user' }],
          user_created_timestamp: 1746773527283,
        },
        email: 'user@gmail.com',
      }

      const result = AuthApi.getUserAuthResponse(mockAuthToken)

      expect(result.super_user).toBe(false)
    })

    it('should handle empty organizations array', () => {
      const mockAuthToken: AuthToken = {
        exp: 1770396901,
        iat: 1770395101,
        jti: '1b921158-2396-461f-8b93-cee772503c2e',
        iss: 'http://localhost:8084/realms/cvmanager',
        aud: 'account',
        sub: 'fc3d8729-8526-4aaa-805b-d64bf3b93860',
        typ: 'Bearer',
        azp: 'cvmanager-gui',
        sid: '75f26b63-1df5-4b9a-953b-61fd72bd8c6b',
        acr: '1',
        'allowed-origins': ['http://localhost:3000'],
        realm_access: { roles: ['offline_access'] },
        resource_access: { account: { roles: ['manage-account'] } },
        scope: 'openid email profile',
        email_verified: false,
        name: 'Test User',
        preferred_username: 'test@gmail.com',
        given_name: 'Test',
        family_name: 'User',
        cvmanager_data: {
          super_user: '1',
          organizations: [],
          user_created_timestamp: 1746773527283,
        },
        email: 'test@gmail.com',
      }

      const result = AuthApi.getUserAuthResponse(mockAuthToken)

      expect(result.organizations).toEqual([])
    })
  })

  describe('logIn', () => {
    const mockToken = 'valid.jwt.token'
    const mockDecodedToken: AuthToken = {
      exp: 1770396901,
      iat: 1770395101,
      jti: '1b921158-2396-461f-8b93-cee772503c2e',
      iss: 'http://localhost:8084/realms/cvmanager',
      aud: 'account',
      sub: 'fc3d8729-8526-4aaa-805b-d64bf3b93860',
      typ: 'Bearer',
      azp: 'cvmanager-gui',
      sid: '75f26b63-1df5-4b9a-953b-61fd72bd8c6b',
      acr: '1',
      'allowed-origins': ['http://localhost:3000'],
      realm_access: { roles: ['offline_access'] },
      resource_access: { account: { roles: ['manage-account'] } },
      scope: 'openid email profile',
      email_verified: false,
      name: 'Test User',
      preferred_username: 'test@gmail.com',
      given_name: 'Test',
      family_name: 'User',
      cvmanager_data: {
        super_user: '1',
        organizations: [{ org: 'Test Org', role: 'admin' }],
        user_created_timestamp: 1746773527283,
      },
      email: 'test@gmail.com',
    }

    it('should successfully log in with valid token', async () => {
      ;(jwtDecode as any).mockReturnValue(mockDecodedToken)
      global.fetch = vi.fn().mockResolvedValue({ ok: true, status: 200 })

      const result = await AuthApi.logIn(mockToken)

      expect(jwtDecode).toHaveBeenCalledWith(mockToken)
      expect(global.fetch).toHaveBeenCalledWith(
        `${EnvironmentVars.KEYCLOAK_HOST_URL}/realms/${EnvironmentVars.KEYCLOAK_REALM}/protocol/openid-connect/userinfo`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${mockToken}`,
          },
        }
      )
      expect(result).toEqual({
        token: mockToken,
        data: {
          email: 'test@gmail.com',
          first_name: 'Test',
          last_name: 'User',
          name: 'Test User',
          super_user: true,
          organizations: [{ organization: 'Test Org', role: 'ADMIN' }],
        },
        expires_at: 1770396901000,
      })
    })

    it('should throw error when token verification fails', async () => {
      ;(jwtDecode as any).mockReturnValue(mockDecodedToken)
      global.fetch = vi.fn().mockResolvedValue({ ok: false, status: 401 })

      await expect(AuthApi.logIn(mockToken)).rejects.toThrow('Token validation failed')

      expect(jwtDecode).toHaveBeenCalledWith(mockToken)
      expect(global.fetch).toHaveBeenCalledWith(
        `${EnvironmentVars.KEYCLOAK_HOST_URL}/realms/${EnvironmentVars.KEYCLOAK_REALM}/protocol/openid-connect/userinfo`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${mockToken}`,
          },
        }
      )
    })

    it('should throw error when token cannot be decoded', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      ;(jwtDecode as any).mockImplementation(() => {
        throw new Error('Invalid token')
      })

      await expect(AuthApi.logIn(mockToken)).rejects.toThrow('Invalid JWT token')

      expect(jwtDecode).toHaveBeenCalledWith(mockToken)
      expect(global.fetch).not.toHaveBeenCalled()

      consoleErrorSpy.mockRestore()
    })

    it('should throw error when Keycloak request fails', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      ;(jwtDecode as any).mockReturnValue(mockDecodedToken)
      global.fetch = vi.fn().mockRejectedValue(new Error('Network error'))

      await expect(AuthApi.logIn(mockToken)).rejects.toThrow('Token validation failed')

      expect(consoleErrorSpy).toHaveBeenCalledWith('Token verification failed:', expect.any(Error))

      consoleErrorSpy.mockRestore()
    })

    it('should handle multiple organizations', async () => {
      const mockTokenMultiOrg = {
        ...mockDecodedToken,
        cvmanager_data: {
          super_user: '0',
          organizations: [
            { org: 'Org 1', role: 'admin' },
            { org: 'Org 2', role: 'user' },
            { org: 'Org 3', role: 'operator' },
          ],
          user_created_timestamp: 1746773527283,
        },
      }

      ;(jwtDecode as any).mockReturnValue(mockTokenMultiOrg)
      global.fetch = vi.fn().mockResolvedValue({ ok: true, status: 200 })

      const result = await AuthApi.logIn(mockToken)

      expect(result.data.organizations).toEqual([
        { organization: 'Org 1', role: 'ADMIN' },
        { organization: 'Org 2', role: 'USER' },
        { organization: 'Org 3', role: 'OPERATOR' },
      ])
      expect(result.data.super_user).toBe(false)
    })
  })
})
