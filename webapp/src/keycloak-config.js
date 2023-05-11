import Keycloak from 'keycloak-js'

const keycloak = new Keycloak({
    url: 'http://172.29.146.207:8084/',
    realm: 'cvmanager',
    clientId: 'cvmanager-gui',
})

export default keycloak
