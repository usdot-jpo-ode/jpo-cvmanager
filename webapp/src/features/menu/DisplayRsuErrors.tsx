import React, { useEffect, useState } from 'react'
import { useSelector, useDispatch } from 'react-redux'

import { getIssScmsStatus, selectRsuData } from '../../generalSlices/rsuSlice'

import '../../components/css/SnmpwalkMenu.css'
import { theme } from '../../styles'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { Action } from '@material-table/core'
import { RootState } from '../../store'
import { selectRsuOnlineStatus, selectIssScmsStatusData } from '../../generalSlices/rsuSlice'

import { PlaceOutlined, ArrowBackIos } from '@mui/icons-material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

import AdminTable from '../../components/AdminTable'
import { setMapViewState } from '../../pages/mapSlice'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Button,
  StyledEngineProvider,
  ThemeProvider,
  Typography,
} from '@mui/material'
import RsuErrorSummary from '../../components/RsuErrorSummary'

const DisplayRsuErrors = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const rsuData = useSelector(selectRsuData)
  const rsuOnlineStatus = useSelector(selectRsuOnlineStatus)
  const issScmsStatusData = useSelector(selectIssScmsStatusData)
  const [selectedRSU, setSelectedRSU] = useState(null)
  const [emailHidden, setEmailHidden] = useState(true)

  type RsuErrorRowType = {
    rsu: string
    online_status: string
    lat: number
    lon: number
    scms_status: string
  }

  // UseEffect to pull SCMS status data on first load
  useEffect(() => {
    dispatch(getIssScmsStatus())
  }, [])

  const getRSUOnlineStatus = (rsuIpv4: string) => {
    return rsuIpv4 in rsuOnlineStatus && rsuOnlineStatus[rsuIpv4].hasOwnProperty('current_status')
      ? rsuOnlineStatus[rsuIpv4].current_status
      : 'Offline'
  }

  const getRSULastOnline = (rsuIpv4: string): string => {
    return rsuIpv4 in rsuOnlineStatus && rsuOnlineStatus[rsuIpv4].hasOwnProperty('last_online')
      ? rsuOnlineStatus[rsuIpv4].last_online
      : 'No Data'
  }

  const getRSUSCMSStatus = (rsuIpv4: string) => {
    return issScmsStatusData.hasOwnProperty(rsuIpv4) && issScmsStatusData[rsuIpv4]
      ? issScmsStatusData[rsuIpv4].health
      : '0'
  }

  const getRSUSCMSExpiration = (rsuIpv4: string) => {
    return issScmsStatusData.hasOwnProperty(rsuIpv4) &&
      issScmsStatusData[rsuIpv4] !== null &&
      issScmsStatusData[rsuIpv4].hasOwnProperty('expiration')
      ? issScmsStatusData[rsuIpv4].expiration
      : 'Never downloaded certificates'
  }

  const getRSUSCMSDisplay = (rsuIpv4: string) => {
    if (getRSUSCMSStatus(rsuIpv4) === '0') {
      var rsu_scms_status = 'SCMS Unhealthy'
      let rsu_scms_expiration = getRSUSCMSExpiration(rsuIpv4)
      switch (rsu_scms_expiration) {
        case 'Never downloaded certificates':
          rsu_scms_status += ' (RSU Never downloaded certificates)'
          break
        default:
          try {
            let expiration_date = new Date(rsu_scms_expiration)
            let now = new Date()
            let diff = expiration_date.getTime() - now.getTime()
            if (diff < 0) {
              rsu_scms_status += ' (RSU SCMS certificate expired)'
            }
          } catch (e) {
            console.debug('Error parsing SCMS expiration date: ', e)
          }
          break
      }
    } else {
      rsu_scms_status = 'SCMS Healthy'
    }
    return rsu_scms_status
  }

  // Create RSU Errors Table Data
  const rsuTableData = rsuData.map((rsu) => {
    var rsu_online_status = 'RSU ' + getRSUOnlineStatus(rsu.properties.ipv4_address)

    var rsu_scms_status = getRSUSCMSDisplay(rsu.properties.ipv4_address)

    return {
      rsu: rsu.properties.ipv4_address,
      road: rsu.properties.primary_route,
      lat: rsu.geometry.coordinates[1],
      lon: rsu.geometry.coordinates[0],
      online_status: rsu_online_status,
      scms_status: rsu_scms_status,
    }
  })

  const tableActions: Action<RsuErrorRowType>[] = [
    {
      icon: () => <PlaceOutlined />,
      tooltip: 'View RSU on Map',
      position: 'row',
      onClick: (event, rowData: RsuErrorRowType) => {
        dispatch(setMapViewState({ latitude: rowData.lat, longitude: rowData.lon, zoom: 15 }))
        setSelectedRSU(rsuData.find((rsu) => rsu.properties.ipv4_address === rowData.rsu))
      },
    },
  ]

  const setHidden = () => {
    setEmailHidden(!emailHidden)
  }

  const containerStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    margin: 'auto',
    TextAlign: 'center',
    FlexDirection: 'column',
  }

  const errorPageStyle = {
    backgroundColor: 'rgb(14, 32, 82)',
    borderTop: '1px solid white',
    borderBottom: '1px solid white',
    fontFamily: 'Arial Helvetica Sans-Serif',
    width: '90%',
    padding: '0.5rem 1rem',
  }

  return (
    <div style={containerStyle}>
      {selectedRSU !== null ? (
        <div id="container" className="sideBarOn" style={{ width: '95%' }}>
          <h1 className="h1" style={{ marginBottom: '1rem', width: '100%', textAlign: 'center' }}>
            {selectedRSU.properties.ipv4_address} Errors
          </h1>
          <ArrowBackIos
            style={{
              position: 'absolute',
              top: '1px',
              left: '0.5rem',
              margin: '1rem',
              backgroundColor: 'rgb(14, 32, 82)',
              color: 'white',
              borderRadius: '50%',
              zIndex: 90,
              cursor: 'pointer',
            }}
            onClick={() => {
              setSelectedRSU(null)
            }}
          />
          <div id="sideBarBlock" className="accordion">
            <StyledEngineProvider injectFirst>
              <ThemeProvider theme={theme}>
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography>Online Status</Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <div style={errorPageStyle}>
                      <p>
                        <b>RSU Online Status: </b>
                        {getRSUOnlineStatus(selectedRSU.properties.ipv4_address)}
                      </p>
                      <br />
                      <p>
                        <b>RSU Last Online: </b>
                        {getRSULastOnline(selectedRSU.properties.ipv4_address)}
                      </p>
                    </div>
                  </AccordionDetails>
                </Accordion>
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography>SCMS Status</Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <div style={errorPageStyle}>
                      <p>
                        <b>SCMS Status: </b>
                        {getRSUSCMSStatus(selectedRSU.properties.ipv4_address) === '1' ? 'Healthy' : 'Unhealthy'}
                      </p>
                      <br />
                      <p>
                        <b>SCMS Expiration: </b>
                        {getRSUSCMSExpiration(selectedRSU.properties.ipv4_address)}
                      </p>
                    </div>
                  </AccordionDetails>
                </Accordion>
              </ThemeProvider>
            </StyledEngineProvider>
          </div>
          <div style={{ display: 'flex', alignContent: 'center', justifyContent: 'center', flexDirection: 'row' }}>
            <Button
              variant="contained"
              style={{ margin: '1rem', backgroundColor: '#b55e12', color: 'white' }}
              onClick={() => {
                setEmailHidden(false)
              }}
            >
              Generate Error Summary Email
            </Button>
          </div>
          <RsuErrorSummary
            rsu={selectedRSU.properties.ipv4_address}
            online_status={
              getRSUOnlineStatus(selectedRSU.properties.ipv4_address) +
              (getRSULastOnline(selectedRSU.properties.ipv4_address) === 'No Data'
                ? ' (Never Online)'
                : ' (Last Online ' + getRSULastOnline(selectedRSU.properties.ipv4_address) + ')')
            }
            scms_status={getRSUSCMSDisplay(selectedRSU.properties.ipv4_address)}
            hidden={emailHidden}
            setHidden={setHidden}
          />
        </div>
      ) : (
        <div id="container" className="sideBarOn" style={{ width: '95%' }}>
          <h1 className="h1" style={{ marginBottom: '1rem', width: '100%', textAlign: 'center' }}>
            RSU Errors
          </h1>
          <AdminTable
            actions={tableActions}
            columns={[
              { title: 'RSU', field: 'rsu' },
              { title: 'Online Status', field: 'online_status' },
              { title: 'SCMS Status', field: 'scms_status' },
            ]}
            data={rsuTableData}
            title=""
            selection={false}
            tableLayout="auto"
          />
        </div>
      )}
    </div>
  )
}

export default DisplayRsuErrors
