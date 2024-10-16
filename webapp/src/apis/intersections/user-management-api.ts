import { authApiHelper } from './api-helper-cviz'

class UserManagementApi {
  async createUserCreationRequest({
    email,
    first_name,
    last_name,
    role,
  }: {
    email: string
    first_name: string
    last_name: string
    role: UserRole
  }): Promise<boolean> {
    return (await authApiHelper.invokeApi({
      path: `/users/create_user_creation_request`,
      method: 'POST',
      body: {
        email: email,
        firstName: first_name,
        lastName: last_name,
        role: role,
      },
      booleanResponse: true,
      toastOnSuccess: true,
      successMessage: 'User created successfully',
      failureMessage: 'Failed to create user creation request',
      tag: 'intersection',
    })) as boolean
  }

  async getUserCreationRequests({ token }: { token: string }): Promise<User[]> {
    const creationRequests: User[] = (
      (await authApiHelper.invokeApi({
        path: `/users/find_user_creation_request`,
        token: token,
        method: 'GET',
        failureMessage: 'Failed to get user creation requests',
        tag: 'intersection',
      })) ?? []
    ).map((user: any) => ({
      ...user,
      first_name: user.firstName,
      last_name: user.lastName,
      role: user.role.toLowerCase(),
    }))

    return creationRequests
  }

  async createUser({
    token,
    email,
    first_name,
    last_name,
    role,
  }: {
    token: string
    email: string
    first_name: string
    last_name: string
    role: UserRole
  }): Promise<boolean> {
    return (await authApiHelper.invokeApi({
      path: `/users/accept_user_creation_request`,
      token: token,
      method: 'POST',
      body: {
        email: email,
        firstName: first_name,
        lastName: last_name,
        role: role,
        id: '',
      },
      booleanResponse: true,
      toastOnSuccess: true,
      successMessage: 'User created successfully',
      failureMessage: 'Failed to create new user',
      tag: 'intersection',
    })) as boolean
  }

  async removeUserCreationRequest({ token, email }: { token: string; email: string }): Promise<boolean> {
    return (await authApiHelper.invokeApi({
      path: `/users/delete_user_creation_request`,
      token: token,
      method: 'DELETE',
      queryParams: { email },
      booleanResponse: true,
      failureMessage: 'Failed to remove user creation request from database',
      tag: 'intersection',
    })) as boolean
  }

  async updateUserEmailPreference({
    token,
    email,
    preferences,
  }: {
    token: string
    email: string
    preferences: EmailPreferences
  }): Promise<boolean> {
    return (await authApiHelper.invokeApi({
      path: `/users/update_user_email_preference`,
      token: token,
      method: 'POST',
      body: preferences,
      booleanResponse: true,
      toastOnSuccess: true,
      successMessage: 'Email preferences updated successfully',
      failureMessage: 'Failed to update email preferences',
      tag: 'intersection',
    })) as boolean
  }

  async getUserEmailPreference({ token }: { token: string }): Promise<EmailPreferences> {
    return (
      (await authApiHelper.invokeApi({
        path: `/users/get_user_email_preference`,
        method: 'POST',
        token: token,
        toastOnFailure: false,
        tag: 'intersection',
      })) ?? {
        receiveAnnouncements: true,
        notificationFrequency: 'ONCE_PER_DAY',
        receiveCeaseBroadcastRecommendations: true,
        receiveCriticalErrorMessages: true,
        receiveNewUserRequests: false,
      }
    )
  }
}

export default new UserManagementApi()
