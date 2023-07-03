import React, { useState, useEffect } from 'react'
import Grid from '@material-ui/core/Grid'
import { GoogleLogin } from '@react-oauth/google'
// import './Menu.js'
import logo from '../images/cdot_logo.png'
import EnvironmentVars from '../EnvironmentVars'
import { useSelector, useDispatch } from 'react-redux'
import {
  selectOrganizationName,
  selectName,
  selectEmail,
  selectAuthLoginData,
  selectLoginFailure,
  selectLoadingGlobal,
  selectKcFailure,

  // actions
  keycloakLogin,
  logout,
  changeOrganization,
  setLoginFailure,
  setKcFailure,
} from '../generalSlices/userSlice'
import { useKeycloak } from '@react-keycloak/web'

import './css/Header.css'

const Header = () => {
  const dispatch = useDispatch()
  const { keycloak } = useKeycloak()

  const authLoginData = useSelector(selectAuthLoginData)
  const organizationName = useSelector(selectOrganizationName)
  const userName = useSelector(selectName)
  const userEmail = useSelector(selectEmail)
  const loginFailure = useSelector(selectLoginFailure)
  const kcFailure = useSelector(selectKcFailure)

  useEffect(() => {
    setLoginFailure(!authLoginData)
  }, [authLoginData])

  useEffect(() => {
    if (!keycloak?.authenticated) {
      const timer = setTimeout(() => {
        dispatch(setKcFailure(true))
      }, 2500)
      return () => clearTimeout(timer)
    } else {
      dispatch(setKcFailure(false))
    }
  }, [keycloak, keycloak?.authenticated, dispatch])

  return (
    <div>
      {authLoginData && keycloak?.authenticated ? (
        <header id="header">
          <Grid container alignItems="center">
            <img id="logo" src={logo} alt="Logo" />
            <h1 id="header-text">CDOT CV Manager</h1>
            <div id="login">
              <Grid container alignItems="center">
                <Grid id="userInfoGrid">
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
                </Grid>
                <button id="logout" onClick={() => keycloak?.logout()}>
                  Logout
                </button>
              </Grid>
            </div>
          </Grid>
        </header>
      ) : (
        <div id="frontpage">
          <Grid container id="frontgrid" alignItems="center" direction="column">
            <Grid container justifyContent="center" alignItems="center">
              <img id="frontpagelogo" src={logo} alt="Logo" />
              <h1 id="header-text">CDOT CV Manager</h1>
            </Grid>

            <div id="keycloakbtndiv">
              {keycloak?.authenticated && (
                <button className="keycloak-button" onClick={() => keycloak.logout()}>
                  Logout User
                </button>
              )}
            </div>
            {loginFailure && <h3 id="loginMessage">User Unauthorized, Please Request Access</h3>}
            {kcFailure && <h3 id="loginMessage">Application Authentication Error!</h3>}
          </Grid>
        </div>
      )}
    </div>
  )
}

export default Header
