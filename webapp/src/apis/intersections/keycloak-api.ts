import { authApiHelper } from './api-helper-cviz'
import EnvironmentVars from '../../EnvironmentVars'

const KEYCLOAK_ADMIN_ENDPOINT = `${EnvironmentVars.KEYCLOAK_HOST_URL}/admin/realms/${EnvironmentVars.KEYCLOAK_REALM}`
const KEYCLOAK_AUTH_ENDPOINT = `${EnvironmentVars.KEYCLOAK_HOST_URL}/realms/${EnvironmentVars.KEYCLOAK_REALM}`

class KeycloakApi {
  getEmailPreferences(attributes: Record<string, string[]>): EmailPreferences {
    return {
      receiveAnnouncements: attributes?.['receiveAnnouncements']?.[0] === 'true',
      notificationFrequency: (attributes?.['notificationFrequency']?.[0] ?? 'NEVER') as EmailFrequency,
      receiveCeaseBroadcastRecommendations: attributes?.['receiveCeaseBroadcastRecommendations']?.[0] === 'true',
      receiveCriticalErrorMessages: attributes?.['receiveCriticalErrorMessages']?.[0] === 'true',
      receiveNewUserRequests: attributes?.['receiveNewUserRequests']?.[0] === 'true',
    }
  }

  async getGroups({ token }: { token: string }): Promise<KeycloakRole[]> {
    return (
      (await authApiHelper.invokeApi({
        path: `/groups`,
        basePath: KEYCLOAK_ADMIN_ENDPOINT,
        token: token,
        failureMessage: 'Failed to get Keycloak groups',
      })) ?? []
    ).map((kRole: any) => {
      return {
        id: kRole.id,
        name: kRole.name,
      }
    })
  }

  async getUserRoles({ token, id }: { token: string; id: string }): Promise<UserRole | undefined> {
    return (
      (await authApiHelper.invokeApi({
        path: `/users/${id}/groups`,
        basePath: KEYCLOAK_ADMIN_ENDPOINT,
        token: token,
        failureMessage: 'Failed to get user roles',
      })) ?? []
    )
      .map((role: any) => role.name)
      .filter((role: string) => ['ADMIN', 'USER'].includes(role))
      .pop()
  }

  async getUsersList({ token }: { token: string }): Promise<User[]> {
    const users =
      (await authApiHelper.invokeApi({
        path: `/users`,
        basePath: KEYCLOAK_ADMIN_ENDPOINT,
        token: token,
        failureMessage: 'Failed to get users. If this failure repeats, please log out and log back in.',
      })) ?? []
    return await Promise.all(
      users.map(async (kUser: any) => {
        const user: User = {
          id: kUser.id,
          email: kUser.username,
          first_name: kUser.firstName,
          last_name: kUser.lastName,
          role: (await this.getUserRoles({ token, id: kUser.id })) ?? 'USER',
          email_preference: this.getEmailPreferences(kUser.attributes),
        }
        return user
      })
    )
  }

  async getUserInfo({ token, id }: { token: string; id: string }): Promise<User | undefined> {
    const kUser = await authApiHelper.invokeApi({
      path: `/users/${id}`,
      basePath: KEYCLOAK_ADMIN_ENDPOINT,
      token: token,
      failureMessage: 'Failed to get user info',
    })
    if (kUser) {
      return {
        id: kUser.id,
        email: kUser.username,
        first_name: kUser.firstName,
        last_name: kUser.lastName,
        role: (await this.getUserRoles({ token, id: kUser.id })) ?? 'USER',
        email_preference: kUser.attributes?.EMAIL_FREQUENCY?.[0] ?? 'NEVER',
      }
    } else {
      return undefined
    }
  }

  async removeUser({ token, id }: { token: string; id: string }): Promise<boolean> {
    return (await authApiHelper.invokeApi({
      path: `/users/${id}`,
      basePath: KEYCLOAK_ADMIN_ENDPOINT,
      method: 'DELETE',
      token: token,
      booleanResponse: true,
      failureMessage: 'Failed to get user info',
    })) as boolean
  }

  async addUserToGroup({ token, id, role }: { token: string; id: string; role: UserRole }): Promise<boolean> {
    const groupId: string | undefined = (await this.getGroups({ token })).find((r) => r.name == role)?.id
    return (await authApiHelper.invokeApi({
      path: `/users/${id}/groups/${groupId}`,
      basePath: KEYCLOAK_ADMIN_ENDPOINT,
      method: 'PUT',
      token: token,
      headers: {
        'Content-Type': 'application/json',
      },
      booleanResponse: true,
      failureMessage: `Failed to add user to ${role} group`,
    })) as boolean
  }

  async removeUserFromGroup({ token, id, role }: { token: string; id: string; role: UserRole }): Promise<boolean> {
    const groupId: string | undefined = (await this.getGroups({ token })).find((r) => r.name == role)?.id
    return (await authApiHelper.invokeApi({
      path: `/users/${id}/groups/${groupId}`,
      basePath: KEYCLOAK_ADMIN_ENDPOINT,
      method: 'DELETE',
      token: token,
      headers: {
        'Content-Type': 'application/json',
      },
      booleanResponse: true,
      failureMessage: `Failed to remove user from ${role} group`,
    })) as boolean
  }

  async updateUserInfo({
    token,
    id,
    email,
    first_name,
    last_name,
  }: {
    token: string
    id: string
    email?: string
    first_name?: string
    last_name?: string
  }): Promise<boolean> {
    const updatedParams = {}
    if (email) {
      updatedParams['username'] = email
      updatedParams['email'] = email
      updatedParams['emailVerified'] = false
    }
    if (first_name) {
      updatedParams['firstName'] = first_name
    }
    if (last_name) {
      updatedParams['lastName'] = last_name
    }

    return (await authApiHelper.invokeApi({
      path: `/users/${id}`,
      basePath: KEYCLOAK_ADMIN_ENDPOINT,
      method: 'PUT',
      token: token,
      body: updatedParams,
      headers: {
        'Content-Type': 'application/json',
      },
      booleanResponse: true,
      failureMessage: 'Failed to update user info',
    })) as boolean
  }

  async logout({ token, refresh_token }: { token?: string; refresh_token: string }): Promise<boolean> {
    return (await authApiHelper.invokeApi({
      path: `/protocol/openid-connect/logout`,
      basePath: KEYCLOAK_AUTH_ENDPOINT,
      method: 'POST',
      token: token,
      body: `client_id=${EnvironmentVars.KEYCLOAK_REALM}&refresh_token=${refresh_token}`,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      booleanResponse: true,
      failureMessage: 'Failed to logout user',
    })) as boolean
  }

  async validateToken({ token }: { token?: string }): Promise<boolean> {
    return (await authApiHelper.invokeApi({
      path: `/protocol/openid-connect/userinfo`,
      basePath: KEYCLOAK_AUTH_ENDPOINT,
      method: 'GET',
      token: token,
      booleanResponse: true,
    })) as boolean
  }
}

export default new KeycloakApi()
