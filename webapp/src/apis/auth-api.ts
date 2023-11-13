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

    const json = await content.json()
    return json
  }
}

export default new AuthApi()
