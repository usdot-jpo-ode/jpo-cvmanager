import React, { useState, useEffect } from 'react'
import Grid from '@material-ui/core/Grid'
import { GoogleLogin } from '@react-oauth/google'
import './Menu.js'
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
    keycloakLogin,
    logout,
    changeOrganization,
    setLoginFailure,
} from '../slices/userSlice'
import { useKeycloak } from '@react-keycloak/web'

import './css/Header.css'

const Header = () => {
    const dispatch = useDispatch()

    const authLoginData = useSelector(selectAuthLoginData)
    const organizationName = useSelector(selectOrganizationName)
    const userName = useSelector(selectName)
    const userEmail = useSelector(selectEmail)
    const loginFailure = useSelector(selectLoginFailure)

    // const [tokenExpired, setTokenExpired] = useState(false)

    const { keycloak, initialized } = useKeycloak()

    useEffect(() => {
        setLoginFailure(!authLoginData)
    }, [authLoginData])

    // useEffect(() => {
    //     setTokenExpired(Date.now() < tokenExpiration)
    // }, [tokenExpiration])

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
                                        onChange={(event) =>
                                            dispatch(
                                                changeOrganization(
                                                    event.target.value
                                                )
                                            )
                                        }
                                    >
                                        {(
                                            authLoginData?.data
                                                ?.organizations ?? []
                                        ).map((permission) => (
                                            <option
                                                key={permission.name + 'Option'}
                                                value={permission.name}
                                            >
                                                {permission.name} (
                                                {permission.role})
                                            </option>
                                        ))}
                                    </select>
                                </Grid>
                                <button
                                    id="logout"
                                    onClick={() => keycloak?.logout()}
                                >
                                    Logout
                                </button>
                            </Grid>
                        </div>
                    </Grid>
                </header>
            ) : (
                <div id="frontpage">
                    <Grid
                        container
                        id="frontgrid"
                        alignItems="center"
                        direction="column"
                    >
                        <Grid
                            container
                            justifyContent="center"
                            alignItems="center"
                        >
                            <img id="frontpagelogo" src={logo} alt="Logo" />
                            <h1 id="header-text">CDOT CV Manager</h1>
                        </Grid>
                        <div id="googlebtn">
                            {/* <GoogleLogin
                                onSuccess={(res) => dispatch(login(res))}
                                text="signin_with"
                                size="large"
                                theme="outline"
                            /> */}
                        </div>
                        {loginFailure && (
                            <h3 id="loginMessage">User Unauthorized</h3>
                        )}
                        {/* {!keycloak. && (
                            <h3 id="loginMessage">Login Timed Out</h3>
                        )} */}

                        <div id="keycloakbtn">
                            <button
                                type="keycloak_button"
                                onClick={() => keycloak.login()}
                            >
                                Login with Keycloak
                            </button>
                        </div>

                        <div>{`Keycloak User${
                            keycloak?.idTokenParsed
                                ? ': "' + keycloak?.idTokenParsed?.name + '"'
                                : ''
                        } is ${
                            !keycloak?.authenticated ? 'NOT ' : ''
                        }authenticated`}</div>

                        {keycloak?.authenticated && [
                            <div>
                                <button
                                    type="button"
                                    onClick={() => keycloak?.logout()}
                                >
                                    Logout KeyCloak user
                                </button>
                            </div>,
                            <div>
                                <button
                                    type="button"
                                    onClick={() => {
                                        let token =
                                            localStorage.getItem(
                                                'keycloakToken'
                                            )
                                        console.log(token)
                                        dispatch(keycloakLogin(token))
                                    }}
                                >
                                    Sign in To CV Manager
                                </button>
                            </div>,
                        ]}
                    </Grid>
                </div>
            )}
        </div>
    )
}

export default Header
