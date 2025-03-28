import React, { useEffect, useMemo, useState } from 'react'
import Grid2 from '@mui/material/Grid2'
import { useSelector, useDispatch } from 'react-redux'
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
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import {
  Button,
  Divider,
  FormControl,
  FormControlLabel,
  Menu,
  MenuItem,
  Paper,
  Radio,
  RadioGroup,
  Typography,
  useTheme,
  Box,
} from '@mui/material'
import { ArrowDropDown } from '@mui/icons-material'

const Header = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const { keycloak } = useKeycloak()

  const authLoginData = useSelector(selectAuthLoginData)
  const organizationName = useSelector(selectOrganizationName)
  const userName = useSelector(selectName)
  const userEmail = useSelector(selectEmail)
  const loginFailure = useSelector(selectLoginFailure)
  const kcFailure = useSelector(selectKcFailure)
  const loginMessage = useSelector(selectLoginMessage)

  const [anchorElem, setAnchorElem] = useState<null | HTMLElement>(null)
  const open = Boolean(anchorElem)

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

  const handleMenuOpen = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorElem(event.currentTarget)
  }

  const handleMenuClose = () => {
    setAnchorElem(null)
  }

  return (
    <div>
      {authLoginData && keycloak?.authenticated ? (
        <Paper id="header" elevation={0}>
          <Grid2 container alignItems="center">
            <Grid2 display="flex">
              <img id="logo" src={iconPath} alt="Logo" height="34px" />
            </Grid2>
            <Grid2 size="grow">
              <h2 id="header-text">{EnvironmentVars.DOT_NAME} CV Manager</h2>
            </Grid2>
            <Grid2>
              <Button
                id="userInfoButton"
                aria-controls={open ? 'user-menu' : undefined}
                aria-haspopup="true"
                aria-expanded={open ? 'true' : undefined}
                variant="text"
                color="primary"
                endIcon={<ArrowDropDown color="info" sx={{ marginLeft: 1 }} />}
                onClick={handleMenuOpen}
                className="user-info-btn"
              >
                <Box display="flex" flexDirection="column" alignItems="start">
                  <Typography fontSize="small" color={theme.palette.text.primary} className="capital-case">
                    {userName}
                    <Typography fontSize="small" color={theme.palette.text.primary} className="capital-case">
                      {organizationName}
                    </Typography>
                  </Typography>
                </Box>
              </Button>
              <Menu
                id="user-menu"
                anchorEl={anchorElem}
                open={open}
                onClose={handleMenuClose}
                MenuListProps={{
                  'aria-labelledby': 'userInfoButton',
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignContent: 'center',
                    justifyContent: 'center',
                    padding: '10px',
                  }}
                >
                  <FormControl sx={{ mt: 0.2, minWidth: 200 }} size="small">
                    <Typography fontSize="medium" sx={{ margin: '10px' }}>
                      Organizations
                    </Typography>
                    <RadioGroup
                      id="organizationRadioGroup"
                      onChange={(event) => dispatch(changeOrganization(event.target.value))}
                      defaultValue={organizationName}
                    >
                      {(authLoginData?.data?.organizations ?? []).map((permission) => (
                        <FormControlLabel
                          label={permission.name}
                          control={<Radio size="small" />}
                          value={permission.name}
                          color="info"
                          sx={{
                            marginLeft: '10px',
                          }}
                        />
                      ))}
                    </RadioGroup>
                  </FormControl>
                  <Divider color="info" sx={{ margin: '10px' }} />
                  <Typography fontSize="small" color="info" sx={{ margin: '10px' }}>
                    {userEmail}
                  </Typography>
                  <MenuItem onClick={handleUserLogout} color="primary">
                    Logout
                  </MenuItem>
                </div>
              </Menu>
            </Grid2>
          </Grid2>
        </Paper>
      ) : (
        <Paper id="frontpage">
          <Grid2 container id="frontgrid" alignItems="center" direction="column">
            <Grid2 container justifyContent="center" alignItems="center">
              <img id="frontpagelogo" src={iconPath} alt="Logo" />
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
