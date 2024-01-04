import Keycloak from 'keycloak-js'
import EnvironmentVars from './EnvironmentVars'

const keycloak = new Keycloak({
  url: `${EnvironmentVars.KEYCLOAK_HOST_URL}`,
  realm: 'cvmanager',
  clientId: 'cvmanager-gui',
})

export default keycloak
