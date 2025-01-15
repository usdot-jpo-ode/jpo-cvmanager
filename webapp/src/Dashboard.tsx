import React, { useEffect } from 'react'
import { css } from '@emotion/react'
import RingLoader from 'react-spinners/RingLoader'
import Header from './components/Header'
import Menu from './features/menu/Menu'
import Help from './components/Help'
import Admin from './pages/Admin'
import Tabs, { TabItem } from './components/Tabs'
import Map from './pages/Map'
import './App.css'
import { useSelector, useDispatch } from 'react-redux'
import {
  // Actions
  getRsuData,
} from './generalSlices/rsuSlice'
import { selectAuthLoginData, selectLoadingGlobal, selectOrganizationName } from './generalSlices/userSlice'
import { SecureStorageManager } from './managers'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'
import { keycloakLogin } from './generalSlices/userSlice'
import { ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from './store'
import { AnyAction } from '@reduxjs/toolkit'
import { Routes, Route, Navigate } from 'react-router-dom'
import IntersectionMapView from './pages/IntersectionMapView'
import IntersectionDashboard from './pages/IntersectionDashboard'
import { NotFound } from './pages/404'
import AdminNotificationTab from './features/adminNotificationTab/AdminNotificationTab'
import { ConditionalRenderRsu, IntersectionRouteGuard } from './feature-flags'
import { Paper, useTheme } from '@mui/material'
import { headerTabHeight } from './styles/index'

let loginDispatched = false

const Dashboard = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const authLoginData = useSelector(selectAuthLoginData)
  const loadingGlobal = useSelector(selectLoadingGlobal)
  const organizationName = useSelector(selectOrganizationName)

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
      <Paper id="masterdiv" style={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
        <div style={{ flex: '0 0 100px' }}>
          <Header />
        </div>
        <div style={{ flex: '1 1 auto', display: 'flex', flexDirection: 'column' }}>
          {authLoginData && keycloak?.authenticated ? (
            <>
              <Tabs>
                <TabItem label={'Map'} path={'map'} />
                <TabItem label={'Intersection Map'} path={'intersectionMap'} tag={'intersection'} />
                <TabItem label={'Intersection Dashboard'} path={'intersectionDashboard'} tag={'intersection'} />
                {SecureStorageManager.getUserRole() !== 'admin' ? <></> : <TabItem label={'Admin'} path={'admin'} />}
                <TabItem label={'Help'} path={'help'} />
                <TabItem label={'User Settings'} path={'settings'} />
              </Tabs>
              <div
                className="tabs"
                style={{
                  height: `calc(100vh - ${headerTabHeight}px)`,
                  overflow: 'auto',
                }}
              >
                <div className="tab-content">
                  <Routes>
                    <Route index element={<Navigate to="map" replace />} />
                    <Route
                      path="map"
                      element={
                        <>
                          <ConditionalRenderRsu>
                            <Menu />
                          </ConditionalRenderRsu>
                          <Map auth={true} />
                        </>
                      }
                    />
                    <Route
                      path="intersectionMap/*"
                      element={
                        <IntersectionRouteGuard>
                          <IntersectionMapView />
                        </IntersectionRouteGuard>
                      }
                    />
                    <Route
                      path="intersectionDashboard/*"
                      element={
                        <IntersectionRouteGuard>
                          <IntersectionDashboard />
                        </IntersectionRouteGuard>
                      }
                    />
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
        </div>
        <RingLoader
          css={loadercss}
          color={theme.palette.primary.main}
          size={200}
          loading={loadingGlobal}
          speedMultiplier={1}
        />
      </Paper>
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
