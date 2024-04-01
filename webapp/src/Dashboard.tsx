import React, { useEffect } from 'react'
import { css } from '@emotion/react'
import RingLoader from 'react-spinners/RingLoader'
import Header from './components/Header'
import Menu from './features/menu/Menu'
import Help from './components/Help'
import Admin from './pages/Admin'
import Grid from '@material-ui/core/Grid'
import Tabs, { TabItem } from './components/Tabs'
import Map from './pages/Map'
import RsuMapView from './pages/RsuMapView'
import './App.css'
import { useSelector, useDispatch } from 'react-redux'
import {
  selectDisplayMap,

  // Actions
  getRsuData,
} from './generalSlices/rsuSlice'
import { selectAuthLoginData, selectRole, selectLoadingGlobal, setRouteNotFound } from './generalSlices/userSlice'
import { SecureStorageManager } from './managers'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'
import { keycloakLogin } from './generalSlices/userSlice'
import { ThunkDispatch } from 'redux-thunk'
import { RootState } from './store'
import { AnyAction } from '@reduxjs/toolkit'
import { Routes, Route, Navigate } from 'react-router-dom'
import { NotFoundRedirect } from './pages/404'

let loginDispatched = false

const Dashboard = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const authLoginData = useSelector(selectAuthLoginData)
  const loadingGlobal = useSelector(selectLoadingGlobal)

  useEffect(() => {
    keycloak
      .updateToken(300)
      .then(function (refreshed: boolean) {
        if (refreshed) {
          console.debug('Token was successfully refreshed')
        } else {
          console.debug('Token is still valid')
        }
      })
      .catch(function () {
        console.error('Failed to refresh the token, or the session has expired')
      })
  }, [])

  useEffect(() => {
    // Refresh Data
    console.debug('Authorizing the user with the API')
    dispatch(getRsuData())
  }, [authLoginData, dispatch])

  console.log('Auth Role', SecureStorageManager.getUserRole())

  return (
    <ReactKeycloakProvider
      initOptions={{ onLoad: 'login-required' }}
      authClient={keycloak}
      onTokens={({ token }: { token: string }) => {
        // Logic to prevent multiple login triggers
        if (!loginDispatched && token) {
          console.debug('onTokens loginDispatched:')
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
            <>
              <Tabs>
                <TabItem label={'Map'} path={'map'} />
                <TabItem label={'RSU Map'} path={'rsuMap'} />
                <TabItem label={'Admin'} path={'admin'} />
                <TabItem label={'Help'} path={'help'} />
              </Tabs>
              <div className="tabs">
                <div className="tab-content">
                  <Routes>
                    <Route index element={<Navigate to="map" replace />} />
                    <Route
                      path="map"
                      element={
                        <>
                          <Menu />
                          <Map auth={true} />
                        </>
                      }
                    />
                    {/* <Route path="rsuMap" element={<RsuMapView auth={true} />} /> */}
                    <Route path="admin/*" element={<Admin />} />
                    <Route path="help" element={<Help />} />
                    <Route path="*" element={<NotFoundRedirect />} />
                  </Routes>
                </div>
              </div>
            </>
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

export default Dashboard
