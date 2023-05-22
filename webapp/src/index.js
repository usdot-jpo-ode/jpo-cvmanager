import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import 'mapbox-gl/dist/mapbox-gl.css'
import App from './App'
import { setupStore } from './store.js'
import store from './store.js'

ReactDOM.render(
  <ReactKeycloakProvider
    initOptions={{ onLoad: 'check-sso' }}
    authClient={keycloak}
    onTokens={({ token }) => {
      store.dispatch(keycloakLogin(token))
    }}
  >
    <Provider store={setupStore({})}>
      <App />
    </Provider>
  </ReactKeycloakProvider>,
  document.getElementById('root')
)
