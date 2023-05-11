import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import 'mapbox-gl/dist/mapbox-gl.css'
import App from './App'
import store from './store.js'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'

ReactDOM.render(
    <ReactKeycloakProvider
        initOptions={{ onLoad: 'check-sso' }}
        authClient={keycloak}
    >
        <Provider store={store}>
            <App />
        </Provider>
    </ReactKeycloakProvider>,
    document.getElementById('root')
)
