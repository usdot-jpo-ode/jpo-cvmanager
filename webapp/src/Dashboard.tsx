import React from 'react'
import { css } from '@emotion/react'
import RingLoader from 'react-spinners/RingLoader'
import Header from './components/Header'
import Menu from './features/menu/Menu'
import Help from './components/Help'
import Admin from './pages/Admin'
import Tabs, { TabItem } from './components/Tabs'
import Map from './pages/Map'
import './App.css'
import { useSelector } from 'react-redux'
import { selectAuthLoginData, selectLoadingGlobal } from './generalSlices/userSlice'
import { SecureStorageManager } from './managers'
import keycloak from './keycloak-config'
import { Routes, Route, Navigate } from 'react-router-dom'
import IntersectionMapView from './pages/IntersectionMapView'
import IntersectionDashboard from './pages/IntersectionDashboard'
import { NotFound } from './pages/404'
import AdminNotificationTab from './features/adminNotificationTab/AdminNotificationTab'
import { ConditionalRenderRsu, IntersectionRouteGuard } from './feature-flags'
import { Paper, useTheme } from '@mui/material'
import { headerTabHeight } from './styles/index'

const Dashboard = () => {
  const theme = useTheme()
  const authLoginData = useSelector(selectAuthLoginData)
  const loadingGlobal = useSelector(selectLoadingGlobal)

  return (
    <Paper id="masterdiv" style={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
      <Header />
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
                        <Map />
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
