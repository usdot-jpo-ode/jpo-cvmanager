import Keycloak from 'keycloak-js'
import EnvironmentVars from './EnvironmentVars'

const keycloak = new Keycloak({
    url: `http://${EnvironmentVars.KEYCLOAK_HOST_IP}:8084/`,
    realm: 'cvmanager',
    clientId: 'cvmanager-gui',
})

export default keycloak
