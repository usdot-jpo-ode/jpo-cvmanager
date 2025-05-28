import React, { useEffect, useMemo } from 'react'
import './App.css'
import { useSelector, useDispatch } from 'react-redux'
import {
  // Actions
  getRsuData,
} from './generalSlices/rsuSlice'
import { keycloakLogin, selectAuthLoginData, selectRouteNotFound } from './generalSlices/userSlice'
import keycloak from './keycloak-config'
import { ThunkDispatch } from 'redux-thunk'
import { RootState } from './store'
import { AnyAction } from '@reduxjs/toolkit'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Dashboard from './Dashboard'
import { NotFound } from './pages/404'
import { getCurrentTheme } from './styles'
import { getIntersections } from './generalSlices/intersectionSlice'
import { Toaster } from 'react-hot-toast'
import { ThemeProvider, StyledEngineProvider, CssBaseline, GlobalStyles } from '@mui/material'
import EnvironmentVars from './EnvironmentVars'
import { useThemeDetector as useBrowserThemeDetector } from './hooks/use-browser-theme-detector'
import '../src/styles/fonts/museo-slab.css'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import { syncWithNtp } from './generalSlices/timeSyncSlice'

let loginDispatched = false

const App = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const authLoginData = useSelector(selectAuthLoginData)
  const routeNotFound = useSelector(selectRouteNotFound)

  const isDarkTheme = useBrowserThemeDetector()
  const theme = useMemo(
    () => getCurrentTheme(isDarkTheme, EnvironmentVars.WEBAPP_THEME_LIGHT, EnvironmentVars.WEBAPP_THEME_DARK),
    [isDarkTheme, EnvironmentVars.WEBAPP_THEME_LIGHT, EnvironmentVars.WEBAPP_THEME_DARK]
  )
  useEffect(() => {
    keycloak.updateToken(300).catch(function () {
      console.error('Failed to refresh the token, or the session has expired')
    })
  }, [])

  // Sync NTP timing slice
  useEffect(() => {
    // Start background synchronization
    dispatch(syncWithNtp())
    const interval = setInterval(() => {
      dispatch(syncWithNtp())
    }, 60000) // Sync every 60 seconds

    return () => clearInterval(interval)
  }, [dispatch])

  useEffect(() => {
    dispatch(getRsuData())
    dispatch(getIntersections())
  }, [authLoginData, dispatch])

  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <ReactKeycloakProvider
          initOptions={{ onLoad: 'login-required' }}
          authClient={keycloak}
          onTokens={({ token }: { token: string }) => {
            // Logic to prevent multiple login triggers
            if (!loginDispatched && token) {
              dispatch(keycloakLogin(token))
              loginDispatched = true
            }
            setTimeout(() => (loginDispatched = false), 5000)
          }}
        >
          <BrowserRouter>
            {routeNotFound ? (
              <NotFound offsetHeight={0} />
            ) : (
              <Routes>
                <Route index element={<Navigate to="dashboard" replace />} />
                <Route path="dashboard/*" element={<Dashboard />} />
                <Route path="*" element={<NotFound shouldRedirect={true} />} />
              </Routes>
            )}
          </BrowserRouter>
          <Toaster
            toastOptions={{
              style: {
                fontFamily: '"museo-slab", Arial, Helvetica, sans-serif',
              },
            }}
          />
        </ReactKeycloakProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  )
}

export default App
