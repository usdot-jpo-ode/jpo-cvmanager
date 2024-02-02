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
import { useSelector, useDispatch } from 'react-redux'
import {
  selectDisplayMap,

  // Actions
  getRsuData,
  getRsuInfoOnly,
} from './generalSlices/rsuSlice'
import { selectAuthLoginData, selectRole, selectLoadingGlobal } from './generalSlices/userSlice'
import { SecureStorageManager } from './managers'
import { ReactKeycloakProvider } from '@react-keycloak/web'
import keycloak from './keycloak-config'
import { keycloakLogin } from './generalSlices/userSlice'
import { ThunkDispatch } from 'redux-thunk'
import { RootState } from './store'
import { AnyAction } from '@reduxjs/toolkit'
import { BrowserRouter, Link, Routes, Route, Router, Navigate } from 'react-router-dom'
import Dashboard from './Dashboard'
import NotFound from './pages/404'
import { theme } from './styles'
import { ThemeProvider } from '@mui/material'

let loginDispatched = false

const App = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const displayMap = useSelector(selectDisplayMap)
  const authLoginData = useSelector(selectAuthLoginData)
  const userRole = useSelector(selectRole)
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

  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
        <Routes>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard/*" element={<Dashboard />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
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
