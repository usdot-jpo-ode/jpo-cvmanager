import { useEffect, useRef, useState } from 'react'
import { useSelector, useDispatch } from 'react-redux'

import { getRsuLastOnline, selectRsuData } from '../../generalSlices/rsuSlice'

import '../../components/css/SnmpwalkMenu.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import MaterialTable, { Action } from '@material-table/core'
import { RootState } from '../../store'
import { selectRsuOnlineStatus } from '../../generalSlices/rsuSlice'
import { formatScmsExpiration, useGetScmsStatusQuery } from '../api/scmsApiSlice'
import { selectOrganizationName } from '../../generalSlices/userSlice'

import { GpsFixedSharp } from '@mui/icons-material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

import AdminTable from '../../components/AdminTable'
import { setMapViewState } from '../../pages/mapSlice'
import { Accordion, AccordionDetails, AccordionSummary, Box, Button, Stack, Typography, useTheme } from '@mui/material'
import { RsuInfo, RsuOnlineStatus } from '../../models/RsuApi'
import { useReactToPrint } from 'react-to-print'
import { SideBarHeader } from '../../styles/components/SideBarHeader'
import { toggleMapMenuSelection } from './menuSlice'
import toast from 'react-hot-toast'
import { useSendRsuErrorSummaryEmailMutation } from '../api/emailApiSlice'
import { selectEmail } from '../../generalSlices/userSlice'

const DisplayRsuErrors = ({ initialSelectedRsu }: { initialSelectedRsu?: RsuInfo }) => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const rsuData = useSelector(selectRsuData)
  const rsuOnlineStatus = useSelector(selectRsuOnlineStatus)
  const organization = useSelector(selectOrganizationName)
  const { data: issScmsStatusData = {} } = useGetScmsStatusQuery(organization, { skip: !organization })
  const [selectedRSU, setSelectedRSU] = useState<RsuInfo | undefined>(initialSelectedRsu)
  const contentRef = useRef(null)
  const errorRef = useRef(null)
  const userEmail = useSelector(selectEmail)
  const handlePrint = useReactToPrint({ contentRef })
  const handleErrorPrint = useReactToPrint({ contentRef: errorRef })
  const [submitRsuErrorSummary] = useSendRsuErrorSummaryEmailMutation()

  const theme = useTheme()

  type RsuErrorRowType = {
    rsu: string
    online_status: string
    lat: number
    lon: number
    scms_status: boolean | null
  }


  // Fetch RSU online status data when an RSU is selected to ensure 'last online' is populated
  useEffect(() => {
    if (selectedRSU) {
      dispatch(getRsuLastOnline(selectedRSU.properties.ipv4_address))
    }
  }, [selectedRSU, dispatch])

  const getRSUOnlineStatus = (rsuIpv4: string) => {
    return rsuIpv4 in rsuOnlineStatus &&
      Object.prototype.hasOwnProperty.call(rsuOnlineStatus[rsuIpv4], 'current_status')
      ? rsuOnlineStatus[rsuIpv4].current_status
      : 'Offline'
  }

  const getRSULastOnline = (rsuIpv4: string): string => {
    return rsuIpv4 in rsuOnlineStatus && Object.prototype.hasOwnProperty.call(rsuOnlineStatus[rsuIpv4], 'last_online')
      ? rsuOnlineStatus[rsuIpv4].last_online
      : 'No Data'
  }

  const getRSUSCMSStatus = (rsuIpv4: string): boolean | null => {
    return Object.prototype.hasOwnProperty.call(issScmsStatusData, rsuIpv4) && issScmsStatusData[rsuIpv4]
      ? issScmsStatusData[rsuIpv4].health
      : null
  }

  const NEVER_DOWNLOADED = 'Never downloaded certificates'

  // Raw ISO-8601 expiration string from the API, or NEVER_DOWNLOADED sentinel if unavailable.
  const getRSUSCMSExpirationRaw = (rsuIpv4: string) => {
    return Object.prototype.hasOwnProperty.call(issScmsStatusData, rsuIpv4) &&
      issScmsStatusData[rsuIpv4] !== null &&
      Object.prototype.hasOwnProperty.call(issScmsStatusData[rsuIpv4], 'expiration')
      ? issScmsStatusData[rsuIpv4].expiration
      : NEVER_DOWNLOADED
  }

  // Human-friendly expiration rendered in the viewer's local timezone, or the NEVER_DOWNLOADED sentinel.
  const getRSUSCMSExpirationDisplay = (rsuIpv4: string) => {
    const raw = getRSUSCMSExpirationRaw(rsuIpv4)
    return raw === NEVER_DOWNLOADED ? raw : formatScmsExpiration(raw)
  }

  const getRSUSCMSDisplay = (rsuIpv4: string) => {
    if (getRSUSCMSStatus(rsuIpv4) !== true) {
      // eslint-disable-next-line no-var
      var rsu_scms_status = 'SCMS Unhealthy'
      const rsu_scms_expiration = getRSUSCMSExpirationRaw(rsuIpv4)
      switch (rsu_scms_expiration) {
        case NEVER_DOWNLOADED:
          rsu_scms_status += ' (RSU Never downloaded certificates)'
          break
        default:
          try {
            const expiration_date = new Date(rsu_scms_expiration)
            const now = new Date()
            const diff = expiration_date.getTime() - now.getTime()
            if (diff < 0) {
              rsu_scms_status += ' (RSU SCMS certificate expired)'
            }
          } catch (e) {
            console.error(`Error parsing SCMS expiration date: ${rsu_scms_expiration}`, e)
          }
          break
      }
    } else {
      rsu_scms_status = 'SCMS Healthy (Expires ' + getRSUSCMSExpirationDisplay(rsuIpv4) + ')'
    }
    return rsu_scms_status
  }

  const sendRsuErrorSummaryEmail = async (
    rsu: string,
    online_status: RsuOnlineStatus | string,
    scms_status: string
  ) => {
    const messageTable = `
        <table>
            <tr>
                <th>Online Status</th>
                <th>SCMS Status</th>
            </tr>
            <tr>
                <td>${'RSU ' + online_status}</td>
                <td>${scms_status}</td>
            </tr>
        </table>
    `

    const message = `
        <p>Below is the error summary for RSU ${rsu} at ${new Date().toISOString()} UTC:</p>
        ${messageTable}
    `

    const data: RsuErrorSummaryEmailContents = {
      subject: `RSU Error Summary for ${rsu}`,
      message: message,
    }

    toast.promise(submitRsuErrorSummary(data).unwrap(), {
      loading: `Sending RSU error summary email to ${userEmail}...`,
      success: `RSU error summary email sent successfully to ${userEmail}!`,
      error: `Failed to send RSU error summary email to ${userEmail}`,
    })
  }

  // Create RSU Errors Table Data
  const rsuTableData = rsuData.map((rsu) => {
    return {
      rsu: rsu.properties.ipv4_address,
      road: rsu.properties.primary_route,
      lat: rsu.geometry.coordinates[1],
      lon: rsu.geometry.coordinates[0],
      online_status: getRSUOnlineStatus(rsu.properties.ipv4_address),
      scms_status: getRSUSCMSStatus(rsu.properties.ipv4_address),
      cert_expiration: getRSUSCMSExpirationDisplay(rsu.properties.ipv4_address),
      milepost: rsu.properties.milepost,
      primary_route: rsu.properties.primary_route,
    }
  })

  const tableActions: Action<RsuErrorRowType>[] = [
    {
      icon: () => null,
      iconProps: {
        title: 'Print Full Report',
        color: 'info',
        itemType: 'outlined',
      },
      position: 'toolbar',
      onClick: () => {
        handlePrint()
      },
    },
    {
      icon: () => null,
      iconProps: {
        title: 'Print Error Report',
        color: 'info',
        itemType: 'outlined',
      },
      position: 'toolbar',
      onClick: () => {
        handleErrorPrint()
      },
    },
  ]

  const containerStyle = {
    display: 'flex',
    justifyContent: 'center',
    margin: 'auto',
    TextAlign: 'center',
    FlexDirection: 'column',
    width: '100%',
    height: '100%',
    backgroundColor: theme.palette.custom.mapLegendBackground,
    borderRadius: '4px',
  }

  return (
    <div style={containerStyle}>
      {selectedRSU !== undefined ? (
        <Stack direction="column" spacing={2} sx={{ pl: 1, pr: 1, width: '100%' }}>
          <SideBarHeader
            onClick={() => {
              setSelectedRSU(undefined)
            }}
            title={selectedRSU.properties.ipv4_address + ' Status'}
          />
          <div className="accordion">
            <Accordion elevation={0}>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography>Online Status</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <div>
                  <Typography fontSize="small" sx={{ color: theme.palette.text.secondary }}>
                    <b>RSU Online Status: </b>
                    {getRSUOnlineStatus(selectedRSU.properties.ipv4_address)}
                  </Typography>
                  <Typography fontSize="small" sx={{ color: theme.palette.text.secondary }}>
                    <b>RSU Last Online: </b>
                    {getRSULastOnline(selectedRSU.properties.ipv4_address)}
                  </Typography>
                </div>
              </AccordionDetails>
            </Accordion>
            <Accordion elevation={0}>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography>SCMS Status</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <div>
                  <Typography fontSize="small" sx={{ color: theme.palette.text.secondary }}>
                    <b>SCMS Status: </b>
                    {getRSUSCMSStatus(selectedRSU.properties.ipv4_address) === true ? 'Healthy' : 'Unhealthy'}
                  </Typography>
                  <Typography fontSize="small" sx={{ color: theme.palette.text.secondary }}>
                    <b>SCMS Expiration: </b>
                    {getRSUSCMSExpirationDisplay(selectedRSU.properties.ipv4_address)}
                  </Typography>
                </div>
              </AccordionDetails>
            </Accordion>
          </div>
          <div style={{ display: 'flex', alignContent: 'center', justifyContent: 'flex-start', flexDirection: 'row' }}>
            <Button
              variant="contained"
              style={{ margin: '1rem' }}
              onClick={() => {
                const lastOnlineStatus =
                  getRSUOnlineStatus(selectedRSU.properties.ipv4_address) +
                  (getRSULastOnline(selectedRSU.properties.ipv4_address) === 'No Data'
                    ? ' (Never Online)'
                    : ' (Last Online ' + getRSULastOnline(selectedRSU.properties.ipv4_address) + ')')

                return sendRsuErrorSummaryEmail(
                  selectedRSU.properties.ipv4_address,
                  lastOnlineStatus,
                  getRSUSCMSDisplay(selectedRSU.properties.ipv4_address)
                )
              }}
              className="museo-slab"
            >
              Send Error Summary Email
            </Button>
          </div>
        </Stack>
      ) : (
        <Stack direction="column" spacing={2} sx={{ pl: 1, pr: 1 }}>
          <SideBarHeader onClick={() => dispatch(toggleMapMenuSelection('Display RSU Status'))} title="RSU Status" />
          <AdminTable
            actions={tableActions}
            columns={[
              {
                title: 'Location',
                field: 'milepost',
                width: '30%',
                render: (rowData) => (
                  <Button
                    onClick={() => {
                      dispatch(setMapViewState({ latitude: rowData.lat, longitude: rowData.lon, zoom: 15 }))
                      setSelectedRSU(rsuData.find((rsu) => rsu.properties.ipv4_address === rowData.rsu))
                    }}
                    variant="text"
                    endIcon={<GpsFixedSharp />}
                    color="info"
                    sx={{
                      '&:hover': {
                        backgroundColor: 'transparent',
                      },
                    }}
                  >
                    <Typography
                      fontSize="small"
                      sx={{
                        '&:hover': {
                          textDecoration: 'underline',
                        },
                      }}
                    >
                      {rowData.primary_route} Mile {rowData.milepost}
                    </Typography>
                  </Button>
                ),
              },
              {
                title: 'Online Status',
                field: 'online_status',
                render: (rowData) => (
                  <Box
                    style={{
                      color: theme.palette.text.primary,
                      backgroundColor: rowData.online_status.toLowerCase().includes('online')
                        ? theme.palette.success.dark
                        : rowData.online_status.toLowerCase().includes('unstable')
                          ? theme.palette.warning.main
                          : theme.palette.error.dark,
                      width: '4rem',
                      height: '1.5rem',
                      display: 'flex',
                      justifyContent: 'center',
                      alignItems: 'center',
                      borderRadius: '1rem',
                    }}
                  >
                    <Typography fontSize="medium">{rowData.online_status}</Typography>
                  </Box>
                ),
              },
              {
                title: 'SCMS Status',
                field: 'scms_status',
                render: (rowData) => (
                  <>
                    <Typography
                      fontSize="medium"
                      sx={{
                        color: rowData.scms_status === true ? theme.palette.success.light : theme.palette.error.light,
                      }}
                    >
                      {rowData.scms_status === true ? 'Healthy' : 'Unhealthy'}
                    </Typography>
                    <Typography fontSize="small" sx={{ color: theme.palette.text.primary }}>
                      {rowData.cert_expiration}
                    </Typography>
                  </>
                ),
              },
              { title: 'RSU IP', field: 'rsu' },
            ]}
            data={rsuTableData}
            title=""
            selection={false}
            tableLayout="auto"
            pageSizeOptions={[]}
          />
        </Stack>
      )}
      <div style={{ display: 'none' }}>
        <div
          ref={contentRef}
          style={{
            margin: '50px',
            fontFamily: 'Arial Helvetica Sans-Serif',
            height: '100vh',
          }}
        >
          <h1 style={{ textAlign: 'center', marginBottom: '10px', color: 'black' }}>RSU Summary</h1>
          <br />
          <p style={{ color: 'black' }}>
            Below is the generated RSU summary report for all RSUs at {new Date().toISOString()} UTC:
          </p>
          <div style={{ marginTop: '25px' }}>
            <MaterialTable
              columns={[
                {
                  title: 'RSU',
                  field: 'rsu',
                  render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.rsu}</p>,
                },
                {
                  title: 'Road',
                  field: 'road',
                  render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.road}</p>,
                },
                {
                  title: 'Online Status',
                  field: 'online_status',
                  render: (rowData) => (
                    <Typography
                      fontSize="medium"
                      sx={
                        rowData.online_status.toLowerCase().includes('online')
                          ? {
                              color: theme.palette.success.dark,
                              fontWeight: 'bold',
                            }
                          : {
                              color: theme.palette.error.dark,
                              fontWeight: 'bold',
                            }
                      }
                    >
                      {rowData.online_status}
                    </Typography>
                  ),
                },
                {
                  title: 'SCMS Status',
                  field: 'scms_status',
                  render: (rowData) => (
                    <>
                      <Typography
                        fontSize="medium"
                        sx={{
                          color: rowData.scms_status === true ? theme.palette.success.dark : theme.palette.error.dark,
                          fontWeight: 'bold',
                        }}
                      >
                        {rowData.scms_status === true ? 'Healthy' : 'Unhealthy'}
                      </Typography>
                      <Typography fontSize="small" sx={{ color: 'black' }}>
                        {rowData.cert_expiration}
                      </Typography>
                    </>
                  ),
                },
              ].map((column) => ({
                ...column,
                cellStyle: {
                  borderRight: '1px solid black', // Add column lines
                },
              }))}
              actions={[]}
              data={rsuTableData}
              title=""
              options={{
                toolbar: false,
                search: false,
                paging: false,
                rowStyle: {
                  overflowWrap: 'break-word',
                  border: `1px solid black`, // Add cell borders
                },
              }}
              style={{
                backgroundColor: 'white',
                color: 'black',
                fontFamily: 'Arial Helvetica Sans-Serif',
                border: 'none',
              }}
            />
          </div>
        </div>
        <div
          ref={errorRef}
          style={{
            margin: '50px',
            fontFamily: 'Arial Helvetica Sans-Serif',
            height: '100vh',
          }}
        >
          <h1 style={{ textAlign: 'center', marginBottom: '10px', color: 'black' }}>RSU Error Summary</h1>
          <br />
          <p style={{ color: 'black' }}>
            Below is the generated RSU Error summary report for all RSUs at {new Date().toISOString()} UTC:
          </p>
          <div style={{ marginTop: '25px' }}>
            <MaterialTable
              columns={[
                {
                  title: 'RSU',
                  field: 'rsu',
                  render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.rsu}</p>,
                },
                {
                  title: 'Road',
                  field: 'road',
                  render: (rowData) => <p style={{ color: 'black', fontWeight: 'bold' }}>{rowData.road}</p>,
                },
                {
                  title: 'Online Status',
                  field: 'online_status',
                  render: (rowData) => (
                    <Typography
                      fontSize="medium"
                      sx={
                        rowData.online_status.toLowerCase().includes('online')
                          ? {
                              color: theme.palette.success.dark,
                              fontWeight: 'bold',
                            }
                          : {
                              color: theme.palette.error.dark,
                              fontWeight: 'bold',
                            }
                      }
                    >
                      {rowData.online_status}
                    </Typography>
                  ),
                },
                {
                  title: 'SCMS Status',
                  field: 'scms_status',
                  render: (rowData) => (
                    <>
                      <Typography
                        fontSize="medium"
                        sx={{
                          color: rowData.scms_status === true ? theme.palette.success.dark : theme.palette.error.dark,
                          fontWeight: 'bold',
                        }}
                      >
                        {rowData.scms_status === true ? 'Healthy' : 'Unhealthy'}
                      </Typography>
                      <Typography fontSize="small" sx={{ color: 'black' }}>
                        {rowData.cert_expiration}
                      </Typography>
                    </>
                  ),
                },
              ].map((column) => ({
                ...column,
                cellStyle: {
                  borderRight: '1px solid black', // Add column lines
                },
              }))}
              actions={[]}
              data={
                rsuTableData !== undefined
                  ? rsuTableData.filter((row) => row.online_status.includes('Offline') || row.scms_status !== true)
                  : []
              }
              title=""
              options={{
                toolbar: false,
                search: false,
                paging: false,
                rowStyle: {
                  overflowWrap: 'break-word',
                  border: `1px solid black`, // Add cell borders
                },
              }}
              style={{
                backgroundColor: 'white',
                color: 'black',
                fontFamily: 'Arial Helvetica Sans-Serif',
                border: 'none',
              }}
            />
          </div>
        </div>
      </div>
    </div>
  )
}

export default DisplayRsuErrors
