import React, { useMemo, useState } from 'react'
import Grid2 from '@mui/material/Grid2'
import { useSelector, useDispatch } from 'react-redux'
import EnvironmentVars from '../EnvironmentVars'
import {
  selectOrganizationName,
  selectName,
  selectEmail,
  selectAuthLoginData,
  selectLoginFailure,

  // actions
  logout,
  changeOrganization,
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
  const loginMessage = useSelector(selectLoginMessage)

  const [anchorElem, setAnchorElem] = useState<null | HTMLElement>(null)
  const open = Boolean(anchorElem)

  const iconPath = useMemo(() => {
    return theme.palette.mode === 'dark' ? '/icons/logo_dark.png' : '/icons/logo_light.png'
  }, [theme.palette.mode])

  const handleUserLogout = () => {
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
              <h2 id="header-text" className="museo-slab">
                {EnvironmentVars.DOT_NAME} CV Manager
              </h2>
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
                  <Typography fontSize="small" color={theme.palette.text.primary} className="capital-case museo-slab">
                    {userName}
                  </Typography>
                  <Typography fontSize="small" color={theme.palette.text.primary} className="capital-case museo-slab">
                    {organizationName}
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
                sx={{
                  '& .MuiPaper-root': {
                    backgroundColor: theme.palette.background.default,
                  },
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
                    <Typography
                      className="capitalize trebuchet"
                      sx={{ margin: '10px', fontSize: '12px' }}
                      color="textSecondary"
                    >
                      Organizations
                    </Typography>
                    <RadioGroup
                      id="organizationRadioGroup"
                      onChange={(event) => dispatch(changeOrganization(event.target.value))}
                      defaultValue={organizationName}
                    >
                      {(authLoginData?.data?.organizations ?? []).map((permission) => (
                        <FormControlLabel
                          key={permission.name}
                          label={permission.name}
                          control={<Radio size="small" />}
                          value={permission.name}
                          sx={{
                            '& .MuiTypography-root': {
                              color:
                                permission.name === organizationName
                                  ? theme.palette.text.primary
                                  : theme.palette.text.secondary,
                              fontFamily: 'Trebuchet MS, Arial, Helvetica, sans-serif',
                            },
                            marginLeft: '10px',
                          }}
                        />
                      ))}
                    </RadioGroup>
                  </FormControl>
                  <Divider sx={{ margin: '10px' }} />
                  <Typography className="trebuchet" color="textSecondary" sx={{ margin: '10px', fontSize: '12px' }}>
                    {userEmail}
                  </Typography>
                  <Button
                    className="museo-slab"
                    variant="outlined"
                    color="info"
                    onClick={handleUserLogout}
                    sx={{ width: 'fit-content', marginLeft: '10px' }}
                  >
                    Logout
                  </Button>
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
              <h1 id="header-text" className="museo-slab">
                {EnvironmentVars.DOT_NAME} CV Manager
              </h1>
            </Grid2>
            {loginFailure && <h3 id="loginMessage">{loginMessage}</h3>}
            <div id="keycloakbtndiv">
              {loginFailure && (
                <Button variant="contained" onClick={() => handleUserLogout()}>
                  Logout User
                </Button>
              )}
            </div>
            <br />
            {loginFailure && <ContactSupportMenu />}
          </Grid2>
        </Paper>
      )}
    </div>
  )
}

export default Header
