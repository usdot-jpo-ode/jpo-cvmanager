import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import 'mapbox-gl/dist/mapbox-gl.css'
import App from './App'
import store from './store.js'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'
import { keycloakLogin } from './slices/userSlice' // Import the Redux action

ReactDOM.render(
    <ReactKeycloakProvider
        initOptions={{ onLoad: 'check-sso' }}
        authClient={keycloak}
        onTokens={({ token }) => {
            store.dispatch(keycloakLogin(token))
        }}
    >
        <Provider store={store}>
            <App />
        </Provider>
    </ReactKeycloakProvider>,
    document.getElementById('root')
)
