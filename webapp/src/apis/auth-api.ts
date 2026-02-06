import { jwtDecode } from 'jwt-decode'
import { AuthToken } from '../models/AuthToken'
import EnvironmentVars from '../EnvironmentVars'

class AuthApi {
  async logIn(token: string) {
    // Decode JWT token to get user info
    const decodedToken = this.parseToken(token)

    // Verify token with Keycloak userinfo endpoint
    const isValid = await this.verifyToken(token)
    if (!isValid) {
      throw new Error('Token validation failed')
    }

    // Extract user info from decoded token and construct UserAuthResponse
    const userAuthResponse = this.getUserAuthResponse(decodedToken)

    return {
      token,
      expires_at: decodedToken.exp * 1000, // Convert to milliseconds
      data: userAuthResponse,
    }
  }

  async verifyToken(token: string): Promise<boolean> {
    try {
      // Call Keycloak's userinfo endpoint to validate the token
      // This endpoint requires a valid access token and returns user info if valid
      const response = await fetch(
        `${EnvironmentVars.KEYCLOAK_HOST_URL}/auth/realms/${EnvironmentVars.KEYCLOAK_REALM}/protocol/openid-connect/userinfo`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      )

      // Token is valid if we get a 200 response
      return response.ok
    } catch (error) {
      console.error('Token verification failed:', error)
      return false
    }
  }

  parseToken(token: string): AuthToken {
    try {
      return jwtDecode<AuthToken>(token)
    } catch (error) {
      console.error('Failed to decode JWT token:', error)
      throw new Error('Invalid JWT token')
    }
  }

  getUserAuthResponse(token: AuthToken): UserAuthResponse {
    return {
      email: token.email,
      first_name: token.given_name,
      last_name: token.family_name,
      super_user: token.cvmanager_data.super_user === '1',
      organizations: token.cvmanager_data.organizations.map((org) => ({
        name: org.org,
        role: org.role,
      })),
    }
  }
}

const authApiInstance = new AuthApi()
export default authApiInstance
