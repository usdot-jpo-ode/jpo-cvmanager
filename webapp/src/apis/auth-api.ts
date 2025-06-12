import EnvironmentVars from '../EnvironmentVars'

class AuthApi {
  async logIn(token: string) {
    const content = await fetch(EnvironmentVars.authEndpoint, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token,
      },
    })

    let json: UserAuthResponse | null = null
    if (content.status === 200) {
      json = (await content.json()) as UserAuthResponse
    }

    return {
      json: json,
      status: content.status,
    }
  }
}

const authApiInstance = new AuthApi()
export default authApiInstance
