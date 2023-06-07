import React, { useEffect } from 'react'
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

const Root = () => {
  useEffect(() => {
    console.debug('useEffect')
    keycloak
      .updateToken(5)
      .then(function (refreshed) {
        if (refreshed) {
          console.debug('Token was successfully refreshed')
        } else {
          console.debug('Token is still valid')
        }
      })
      .catch(function () {
        console.debug('Failed to refresh the token, or the session has expired')
      })
  }, [keycloak])

  return (
    <ReactKeycloakProvider
      initOptions={{ onLoad: 'login-required' }}
      authClient={keycloak}
      onTokens={({ token }) => {
        console.debug('onTokens loginDispatched:', loginDispatched, token)
        // Logic to prevent multiple login triggers
        if (!loginDispatched && token) {
          console.debug('Keycloak token update')
          store.dispatch(keycloakLogin(token))
          loginDispatched = true
        }
        setTimeout(() => (loginDispatched = false), 5000)
      }}
    >
      <Provider store={store}>
        <App />
      </Provider>
    </ReactKeycloakProvider>
  )
}

ReactDOM.render(<Root />, document.getElementById('root'))
