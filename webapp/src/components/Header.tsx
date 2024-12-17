import React, { useEffect, useMemo } from 'react'
import Grid2 from '@mui/material/Grid2'
import EnvironmentVars from '../EnvironmentVars'
import {
  selectOrganizationName,
  selectName,
  selectEmail,
  selectAuthLoginData,
  selectLoginFailure,
  selectKcFailure,

  // actions
  logout,
  changeOrganization,
  setKcFailure,
  selectLoginMessage,
} from '../generalSlices/userSlice'
import { useKeycloak } from '@react-keycloak/web'

import './css/Header.css'

import ContactSupportMenu from './ContactSupportMenu'
import { useAppDispatch, useAppSelector } from '../hooks'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { Button, FormControl, InputLabel, MenuItem, Paper, Select, useTheme } from '@mui/material'
import LogoutIcon from '@mui/icons-material/Logout'
import { LightButton } from '../styles/components/LightButton'

const Header = () => {
    const dispatch = useAppDispatch()
  const theme = useTheme()
  const { keycloak } = useKeycloak()

  const authLoginData = useAppSelector(selectAuthLoginData)
  const organizationName = useAppSelector(selectOrganizationName)
  const userName = useAppSelector(selectName)
  const userEmail = useAppSelector(selectEmail)
  const loginFailure = useAppSelector(selectLoginFailure)
  const kcFailure = useAppSelector(selectKcFailure)
  const loginMessage = useAppSelector(selectLoginMessage)

  const iconPath = useMemo(() => {
    return theme.palette.mode === 'dark' ? '/icons/logo_dark.png' : '/icons/logo_light.png'
  }, [theme.palette.mode])

  useEffect(() => {
    const kcFailureDelay = 500000
    const kcFailureTimer = setTimeout(() => {
      if (!keycloak?.authenticated) {
        console.debug('Login failure logic: User is not authenticated with Keycloak')
        dispatch(setKcFailure(true))
      } else {
        console.debug('Login failure logic: User is now authenticated with Keycloak')
        dispatch(setKcFailure(false))
      }
    }, kcFailureDelay)

    return () => clearTimeout(kcFailureTimer)
  }, [keycloak, keycloak?.authenticated, dispatch])

  const handleUserLogout = () => {
    console.debug('handleUserLogout')
    dispatch(logout())
    keycloak?.logout()
  }

  return (
    <div>
      {authLoginData && keycloak?.authenticated ? (
        <Paper id="header">
          <Grid2 container alignItems="center" style={{ height: 'fit-content' }}>
            <img id="logo" src={iconPath} alt="Logo" height="90px" />
            <h1 id="header-text">{EnvironmentVars.DOT_NAME} CV Manager</h1>
            <div id="login" style={{ lineHeight: 1.1, marginTop: 10 }}>
              <Grid2 container alignItems="center">
                <Grid2 id="userInfoGrid">
                  <h3 id="nameText">{userName}</h3>
                  <h3 id="emailText">{userEmail}</h3>
                  <FormControl sx={{ mt: 0.2, minWidth: 200 }} size="small">
                    <Select
                      value={organizationName}
                      onChange={(event) => dispatch(changeOrganization(event.target.value))}
                    >
                      {(authLoginData?.data?.organizations ?? []).map((permission) => (
                        <MenuItem key={permission.name + 'Option'} value={permission.name} color="primary">
                          {permission.name} ({permission.role})
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid2>
                <LightButton
                  startIcon={<LogoutIcon />}
                  onClick={() => handleUserLogout()}
                  sx={{
                    padding: 1,
                    paddingLeft: 2,
                    paddingRight: 2,
                    right: '10px',
                  }}
                >
                  Logout
                </LightButton>
              </Grid2>
            </div>
          </Grid2>
        </Paper>
      ) : (
        <Paper id="frontpage">
          <Grid2 container id="frontgrid" alignItems="center" direction="column">
            <Grid2 container justifyContent="center" alignItems="center">
              <img id="frontpagelogo" src={iconPath} alt="Logo" height="90px" />
              <h1 id="header-text">{EnvironmentVars.DOT_NAME} CV Manager</h1>
            </Grid2>
            {loginFailure && <h3 id="loginMessage">{loginMessage}</h3>}
            <div id="keycloakbtndiv">
              {loginFailure && (
                <Button variant="contained" onClick={() => handleUserLogout()}>
                  Logout User
                </Button>
              )}
            </div>
            {kcFailure && <h3 id="loginMessage">Application Authentication Error!</h3>}

            <br />

            {loginFailure && <ContactSupportMenu />}
          </Grid2>
        </Paper>
      )}
    </div>
  )
}

export default Header