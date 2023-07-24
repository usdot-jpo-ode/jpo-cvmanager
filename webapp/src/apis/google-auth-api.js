import EnvironmentVars from '../EnvironmentVars'

class GoogleAuthApi {
  async logIn(token) {
    const content = await fetch(EnvironmentVars.googleAuthEndpoint, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token,
      },
    })

    const json = await content.json()
    return {
      json: json,
      status: content.status,
      message: content.statusText
    }
  }
}

export default new GoogleAuthApi()
