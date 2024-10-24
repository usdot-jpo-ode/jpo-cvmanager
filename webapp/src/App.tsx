import React, { useEffect } from 'react'
import './App.css'
import { useSelector, useDispatch } from 'react-redux'
import {
  // Actions
  getRsuData,
} from './generalSlices/rsuSlice'
import { selectAuthLoginData, selectRouteNotFound } from './generalSlices/userSlice'
import keycloak from './keycloak-config'
import { ThunkDispatch } from 'redux-thunk'
import { RootState } from './store'
import { AnyAction } from '@reduxjs/toolkit'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Dashboard from './Dashboard'
import { NotFound } from './pages/404'
import { theme } from './styles'
import { getIntersections } from './generalSlices/intersectionSlice'
import { Toaster, ToastOptions } from 'react-hot-toast'
import { ThemeProvider, StyledEngineProvider, CssBaseline } from '@mui/material'

const App = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const authLoginData = useSelector(selectAuthLoginData)
  const routeNotFound = useSelector(selectRouteNotFound)

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
    dispatch(getIntersections())
  }, [authLoginData, dispatch])

  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline />
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
              fontFamily: 'Arial, Helvetica, sans-serif',
            },
          }}
        />
      </ThemeProvider>
    </StyledEngineProvider>
  )
}

export default App
