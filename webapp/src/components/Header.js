import React, { useState, useEffect } from 'react'
import Grid from '@material-ui/core/Grid'
import { GoogleLogin } from '@react-oauth/google'
import '../features/menu/Menu.js'
import logo from '../images/cdot_logo.png'

import { useSelector, useDispatch } from 'react-redux'
import {
  selectOrganizationName,
  selectName,
  selectEmail,
  selectAuthLoginData,
  selectTokenExpiration,
  selectLoginFailure,

  // actions
  login,
  logout,
  changeOrganization,
  setLoginFailure,
  selectLoginMessage,
  setLoginMessage
} from '../generalSlices/userSlice'

import './css/Header.css'

import ContactSupportMenu from "./ContactSupportMenu";

const Header = (props) => {
  const dispatch = useDispatch()

  const authLoginData = useSelector(selectAuthLoginData)
  const organizationName = useSelector(selectOrganizationName)
  const userName = useSelector(selectName)
  const userEmail = useSelector(selectEmail)
  const tokenExpiration = useSelector(selectTokenExpiration)
  const loginFailure = useSelector(selectLoginFailure)
  const loginMessage = useSelector(selectLoginMessage)


  const [tokenExpired, setTokenExpired] = useState(false)

  useEffect(() => {
    setLoginFailure(!authLoginData)
  }, [authLoginData])

  useEffect(() => {
    setTokenExpired(Date.now() < tokenExpiration)
  }, [tokenExpiration])

  return (
    <div>
      {authLoginData ? (
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
                <button id="logout" onClick={() => dispatch(logout())}>
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
            <div id="googlebtn">
              <GoogleLogin
                onSuccess={(res) => dispatch(login(res))}
                onError={(err) => dispatch(setLoginMessage('Google Login Error: ' + err))}
                text="signin_with"
                size="large"
                theme="outline" />
            </div>
            {loginFailure && <h3 id="loginMessage">{ loginMessage }</h3>}
            {tokenExpired && <h3 id="loginMessage">Login Timed Out</h3>}

            <br />
            <ContactSupportMenu />
          </Grid>
        </div>
      )}
    </div>
  )
}

export default Header
