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
import { UserManager } from './managers'
import { useSelector, useDispatch } from 'react-redux'
import {
  selectLoading,
  selectDisplayMap,
  selectBsmLoading,

  // Actions
  getRsuData,
  getRsuInfoOnly,
} from './generalSlices/rsuSlice'
import {
  selectAuthLoginData,
  selectRole,
  selectLoading as selectUserLoading,
  selectLoadingGlobal,

  // Actions
  keycloakLogin,
  logout,
} from './generalSlices/userSlice'
import { store } from './store'
import { selectLoading as selectWzdxLoading } from './slices/wzdxSlice'
import { selectLoading as selectConfigLoading } from './slices/configSlice'

import { useKeycloak } from '@react-keycloak/web'

const App = () => {
  const dispatch = useDispatch()

  const { keycloak, initialized } = useKeycloak()

  const loading = useSelector(selectLoading)
  const displayMap = useSelector(selectDisplayMap)

  const userLoading = useSelector(selectUserLoading)
  const authLoginData = useSelector(selectAuthLoginData)
  const userRole = useSelector(selectRole)

  const wzdxLoading = useSelector(selectWzdxLoading)

  const configLoading = useSelector(selectConfigLoading)

  const bsmLoading = useSelector(selectBsmLoading)
  const loadingGlobal = useSelector(selectLoadingGlobal)

  useEffect(() => {
    // Refresh Data
    dispatch(getRsuData({ test: 'test' }))
  }, [authLoginData, dispatch])

  const isLoginActive = () => {
    const isLoginActive = UserManager.isLoginActive(authLoginData)
    if (!isLoginActive) {
      dispatch(logout())
    }
    return isLoginActive
  }

  return (
    <div id="masterdiv">
      <Grid container id="content-grid" alignItems="center">
        <Header />
        {authLoginData && keycloak?.authenticated ? (
          <Tabs isLoginActive={isLoginActive}>
            <div label="CV Map">
              {displayMap ? null : <Menu />}
              {displayMap ? <RsuMapView auth={true} /> : <Map auth={true} />}
            </div>
            {userRole === 'admin' && (
              <div label="Admin">
                <Admin
                  authLoginData={authLoginData}
                  isLoginActive={() => authLoginData != null}
                  setLoading={(loadingVal) => dispatch(setLoading(loadingVal))}
                  updateRsuData={() => dispatch(getRsuInfoOnly())}
                />
              </div>
            )}
            <div label="Help">
              <Help />
            </div>
          </Tabs>
        ) : (
          <div></div>
        )}
      </Grid>
      <RingLoader
        css={loadercss}
        size={200}
        color={'#13d48d'}
        loading={!(!loading && !userLoading && !wzdxLoading && !configLoading && !bsmLoading)}
        speedMultiplier={1}
      />
    </div>
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
