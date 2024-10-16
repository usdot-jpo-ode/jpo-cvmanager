import React, { useEffect } from 'react'
import { css } from '@emotion/react'
import RingLoader from 'react-spinners/RingLoader'
import Header from './components/Header'
import Menu from './features/menu/Menu'
import Help from './components/Help'
import Admin from './pages/Admin'
import Grid2 from '@mui/material/Grid2'
import Tabs, { TabItem } from './components/Tabs'
import Map from './pages/Map'
import './App.css'
import {
  // Actions
  getRsuData,
} from './generalSlices/rsuSlice'
import { selectAuthLoginData, selectLoadingGlobal, selectOrganizationName } from './generalSlices/userSlice'
import { SecureStorageManager } from './managers'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'
import { keycloakLogin } from './generalSlices/userSlice'
import { Routes, Route, Navigate } from 'react-router-dom'
import IntersectionMapView from './pages/IntersectionMapView'
import IntersectionDashboard from './pages/IntersectionDashboard'
import { NotFound } from './pages/404'
import AdminNotificationTab from './features/adminNotificationTab/AdminNotificationTab'
import { useAppDispatch, useAppSelector } from './hooks'

let loginDispatched = false

const Dashboard = () => {
  const dispatch = useAppDispatch()
  const authLoginData = useAppSelector(selectAuthLoginData)
  const loadingGlobal = useAppSelector(selectLoadingGlobal)
  const organizationName = useAppSelector(selectOrganizationName)

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

  useEffect(() => {}, [organizationName])

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
        <Grid2 container id="content-grid" alignItems="center">
          <Header />
          {authLoginData && keycloak?.authenticated ? (
            <>
              <Tabs>
                <TabItem label={'RSU Map'} path={'map'} />
                <TabItem label={'Intersection Map'} path={'intersectionMap'} />
                <TabItem label={'Intersection Dashboard'} path={'intersectionDashboard'} />
                {SecureStorageManager.getUserRole() !== 'admin' ? <></> : <TabItem label={'Admin'} path={'admin'} />}
                <TabItem label={'Help'} path={'help'} />
                <TabItem label={'User Settings'} path={'settings'} />
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
                    <Route path="intersectionMap/*" element={<IntersectionMapView />} />
                    <Route path="intersectionDashboard/*" element={<IntersectionDashboard />} />
                    <Route path="admin/*" element={<Admin />} />
                    <Route path="settings/*" element={<AdminNotificationTab />} />
                    <Route path="help" element={<Help />} />
                    <Route path="*" element={<NotFound />} />
                  </Routes>
                </div>
              </div>
            </>
          ) : (
            <div></div>
          )}
        </Grid2>
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
