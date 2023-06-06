import Keycloak from 'keycloak-js'
import EnvironmentVars from './EnvironmentVars'

const keycloak = new Keycloak({
  url: `http://${EnvironmentVars.KEYCLOAK_HOST_IP}:8084/`,
  realm: 'cvmanager',
  clientId: 'cvmanager-gui',
})

keycloak.onReady = (authenticated) => {
  if (!authenticated) {
    // Push an alert or show a notification to the user
    alert('Failed to authenticate with Keycloak')
  }
}

keycloak.onAuthError = () => {
  // Push an alert or show a notification to the user
  alert('Failed to authenticate with Keycloak')
}

export default keycloak
