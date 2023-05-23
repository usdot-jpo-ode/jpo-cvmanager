import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import 'mapbox-gl/dist/mapbox-gl.css'
import App from './App'
import { setupStore } from './store.js'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'
import { keycloakLogin } from './generalSlices/userSlice'

const store = setupStore({})
let loginDispatched = false

ReactDOM.render(
  <ReactKeycloakProvider
    initOptions={{ onLoad: 'login-required' }}
    authClient={keycloak}
    onTokens={({ token }) => {
      // Logic to prevent multiple login triggers
      if (!loginDispatched && token) {
        console.debug('Keycloak token update')
        store.dispatch(keycloakLogin(token))
        loginDispatched = true
      }
    }}
  >
    <Provider store={store}>
      <App />
    </Provider>
  </ReactKeycloakProvider>,
  document.getElementById('root')
)
