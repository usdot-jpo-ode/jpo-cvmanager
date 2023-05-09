import Keycloak from 'keycloak-js'

const keycloak = new Keycloak({
    url: 'http://localhost:8084/',
    realm: 'cvmanager',
    clientId: 'cvmanager-gui',
})

export default keycloak
