import EnvironmentVars from '../EnvironmentVars'

class AuthApi {
  async logIn(token) {
    const response = await fetch(EnvironmentVars.authEndpoint, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token,
      },
    })
    return response
  }
}

export default new AuthApi()
