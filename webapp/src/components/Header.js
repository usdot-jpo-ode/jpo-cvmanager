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

  // actions
  keycloakLogin,
  logout,
  changeOrganization,
  setLoginFailure,
} from '../generalSlices/userSlice'
import { useKeycloak } from '@react-keycloak/web'

import './css/Header.css'

const Header = () => {
  const dispatch = useDispatch()
  const { keycloak, initialized } = useKeycloak()

  const authLoginData = useSelector(selectAuthLoginData)
  const organizationName = useSelector(selectOrganizationName)
  const userName = useSelector(selectName)
  const userEmail = useSelector(selectEmail)
  // const tokenExpiration = useSelector(selectTokenExpiration)
  const loginFailure = useSelector(selectLoginFailure)
  const loading = useSelector(selectLoadingGlobal)

  const [tokenExpired, setTokenExpired] = useState(false)

  useEffect(() => {
    setLoginFailure(!authLoginData)
  }, [authLoginData])

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
                <button className="keycloak-login-button" onClick={() => keycloak.logout()}>
                  Logout User
                </button>
              )}
            </div>
            {loginFailure && <h3 id="loginMessage">User Unauthorized, Please Request Access</h3>}

            {/* {keycloak?.authenticated && [
              <div>
                <button type="button" onClick={() => keycloak?.logout()}>
                  Logout KeyCloak user
                </button>
              </div>,
              <div>
                <button
                  type="button"
                  onClick={() => {
                    let token = localStorage.getItem('authLoginData')['token']
                    console.log(token)
                    dispatch(keycloakLogin(token))
                  }}
                >
                  Sign in To CV Manager
                </button>
              </div>,
            ]} */}
          </Grid>
        </div>
      )}
    </div>
  )
}

export default Header
