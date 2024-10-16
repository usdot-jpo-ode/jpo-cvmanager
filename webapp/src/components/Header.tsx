import React, { useEffect } from 'react'
import Grid2 from '@mui/material/Grid2'
import logo from '../icons/logo.png'
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

const Header = () => {
  const dispatch = useAppDispatch()
  const { keycloak } = useKeycloak()

  const authLoginData = useAppSelector(selectAuthLoginData)
  const organizationName = useAppSelector(selectOrganizationName)
  const userName = useAppSelector(selectName)
  const userEmail = useAppSelector(selectEmail)
  const loginFailure = useAppSelector(selectLoginFailure)
  const kcFailure = useAppSelector(selectKcFailure)
  const loginMessage = useAppSelector(selectLoginMessage)

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
        <header id="header">
          <Grid2 container alignItems="center" style={{ height: 'fit-content' }}>
            <img id="logo" src={logo} alt="Logo" />
            <h1 id="header-text">{EnvironmentVars.DOT_NAME} CV Manager</h1>
            <div id="login">
              <Grid2 container alignItems="center">
                <Grid2 id="userInfoGrid">
                  <h3 id="nameText">{userName}</h3>
                  <h3 id="emailText">{userEmail}</h3>
                  <select
                    id="organizationDropdown"
                    value={organizationName}
                    onChange={(event) => dispatch(changeOrganization(event.target.value))}
                  >
                    {(authLoginData?.data?.organizations ?? []).map((permission) => (
                      <option key={permission.name + 'Option'} value={permission.name}>
                        {permission.name} ({permission.role})
                      </option>
                    ))}
                  </select>
                </Grid2>
                <button id="logout" onClick={() => handleUserLogout()}>
                  Logout
                </button>
              </Grid2>
            </div>
          </Grid2>
        </header>
      ) : (
        <div id="frontpage">
          <Grid2 container id="frontgrid" alignItems="center" direction="column">
            <Grid2 container justifyContent="center" alignItems="center">
              <img id="frontpagelogo" src={logo} alt="Logo" />
              <h1 id="header-text">{EnvironmentVars.DOT_NAME} CV Manager</h1>
            </Grid2>
            {loginFailure && <h3 id="loginMessage">{loginMessage}</h3>}
            <div id="keycloakbtndiv">
              {loginFailure && (
                <button className="keycloak-button" onClick={() => handleUserLogout()}>
                  Logout User
                </button>
              )}
            </div>
            {kcFailure && <h3 id="loginMessage">Application Authentication Error!</h3>}

            <br />

            {loginFailure && <ContactSupportMenu />}
          </Grid2>
        </div>
      )}
    </div>
  )
}

export default Header
