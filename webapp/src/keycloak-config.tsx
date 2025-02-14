import Keycloak from 'keycloak-js'
import EnvironmentVars from './EnvironmentVars'

const keycloak = new Keycloak({
  url: `${EnvironmentVars.KEYCLOAK_HOST_URL}`,
  realm: `${EnvironmentVars.KEYCLOAK_REALM}`,
  clientId: `${EnvironmentVars.KEYCLOAK_CLIENT_ID}`,
})

export default keycloak
