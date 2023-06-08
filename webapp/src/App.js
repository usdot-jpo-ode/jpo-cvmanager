import React, { useEffect } from 'react'
import { css } from '@emotion/react'
import RingLoader from 'react-spinners/RingLoader'
import Header from './components/Header'
import Menu from './features/menu/Menu'
import Help from './components/Help'
import Admin from './pages/Admin'
import Grid from '@material-ui/core/Grid'
import Tabs from './components/Tabs'
import Map from './pages/Map'
import RsuMapView from './pages/RsuMapView'
import './App.css'
import { UserManager } from './managers'
import { useSelector, useDispatch } from 'react-redux'
import {
  selectDisplayMap,

  // Actions
  getRsuData,
  getRsuInfoOnly,
} from './generalSlices/rsuSlice'
import {
  selectAuthLoginData,
  selectRole,
  selectLoadingGlobal,

  // Actions
  logout,
} from './generalSlices/userSlice'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'
import { keycloakLogin } from './generalSlices/userSlice'

let loginDispatched = false

const App = () => {
  const dispatch = useDispatch()

  const displayMap = useSelector(selectDisplayMap)
  const authLoginData = useSelector(selectAuthLoginData)
  const userRole = useSelector(selectRole)
  const loadingGlobal = useSelector(selectLoadingGlobal)

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

  useEffect(() => {
    // Refresh Data
    dispatch(getRsuData({ test: 'test' }))
  }, [authLoginData, dispatch])

  const isLoginActive = () => {
    const isLoginActive = UserManager.isLoginActive(authLoginData)
    if (!isLoginActive) {
      dispatch(logout())
    }
    return isLoginActive
  }

  return (
    <ReactKeycloakProvider
      initOptions={{ onLoad: 'login-required' }}
      authClient={keycloak}
      onTokens={({ token }) => {
        console.debug('onTokens loginDispatched:', loginDispatched, token)
        // Logic to prevent multiple login triggers
        if (!loginDispatched && token) {
          console.debug('Keycloak token update')
          dispatch(keycloakLogin(token))
          loginDispatched = true
        }
        setTimeout(() => (loginDispatched = false), 5000)
      }}
    >
      <div id="masterdiv">
        <Grid container id="content-grid" alignItems="center">
          <Header />
          {authLoginData && keycloak?.authenticated ? (
            <Tabs isLoginActive={isLoginActive}>
              <div label="CV Map">
                {displayMap ? null : <Menu />}
                {displayMap ? <RsuMapView auth={true} /> : <Map auth={true} />}
              </div>
              {userRole === 'admin' && (
                <div label="Admin">
                  <div label="Admin">
                    <Admin updateRsuData={() => dispatch(getRsuInfoOnly())} />
                  </div>
                </div>
              )}
              <div label="Help">
                <Help />
              </div>
            </Tabs>
          ) : (
            <div></div>
          )}
        </Grid>
        <RingLoader css={loadercss} size={200} color={'#13d48d'} loading={loadingGlobal} speedMultiplier={1} />
      </div>
    </ReactKeycloakProvider>
  )
}

const loadercss = css`
  display: block;
  margin: 0 auto;
  position: absolute;
  top: 50%;
  left: 50%;
  margin-top: -125px;
  margin-left: -125px;
`

export default App
