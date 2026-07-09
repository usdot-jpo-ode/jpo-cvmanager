import React, { useMemo } from 'react'

import { Paper, Box, IconButton, Typography, Fab, AccordionSummary } from '@mui/material'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'

import MuiAccordionDetails from '@mui/material/AccordionDetails'
import { styled, useTheme } from '@mui/material/styles'
import { CustomTable } from './custom-table'
import { format } from 'date-fns'
import { ExpandableTable } from './expandable-table'
import { MAP_PROPS } from './map-slice'
import '../../../components/css/RsuMapView.css'
import { InfoOutlined, Close, ExpandMoreOutlined } from '@mui/icons-material'
import { ConnectionOfTravelNotification } from '../../../models/jpo-conflictmonitor/notifications/ConnectionOfTravelNotification'
import { getSrmInfoList, getSsmInfoList } from './utilities/message-utils'

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  () => ({})
)

const AccordionDetails = styled(MuiAccordionDetails)(() => ({}))

interface SidePanelProps {
  laneInfo: ConnectingLanesFeatureCollection | undefined
  signalGroups: SpatSignalGroup[] | undefined
  bsms: BsmFeatureCollection
  ssmData: ProcessedSsm[]
  srmData: ProcessedSrmFeature[]
  events: MessageMonitor.Event[]
  notifications: MessageMonitor.Notification[]
  sourceData: MAP_PROPS['sourceData']
  sourceDataType: MAP_PROPS['sourceDataType']
  openPanel: string
  setOpenPanel: (panel: string) => void
}

export const SidePanel = (props: SidePanelProps) => {
  const { laneInfo, signalGroups, bsms, ssmData, srmData, events, notifications, sourceData, sourceDataType } = props

  const theme = useTheme()

  const toggleOpen = () => {
    if (props.openPanel === 'map-info') {
      props.setOpenPanel('')
    } else {
      props.setOpenPanel('map-info')
    }
  }

  const [ssmInfo, ssmResponseDict] = useMemo(() => {
    const ssmInfo = ssmData.flatMap(getSsmInfoList)
    const ssmResponseDict: { [key: number]: SsmInfo[] } = {}
    ssmInfo.forEach((ssm) => {
      const key = ssm.requestInfo.vehicleID + '_' + ssm.requestID
      if (key in ssmResponseDict) {
        ssmResponseDict[key] = [...ssmResponseDict[key], ssm]
      } else if (key) {
        ssmResponseDict[key] = [ssm]
      }
    })
    return [ssmInfo, ssmResponseDict]
  }, [ssmData])

  const srmInfo = useMemo(() => {
    return srmData.flatMap(getSrmInfoList)
  }, [srmData])

  const getDataTable = (sourceData: MAP_PROPS['sourceData'], sourceDataType: MAP_PROPS['sourceDataType']) => {
    switch (sourceDataType) {
      case 'notification':
        return getNotificationTable(sourceData as MessageMonitor.Notification)
      case 'event':
        return <Typography>No Data</Typography> //getNotificationTableFromEvent(sourceData as MessageMonitor.Event);
      case 'assessment':
        return <Typography>No Data</Typography> //getNotificationTableFromAssessment(sourceData as Assessment);
      case 'timestamp':
        return <Typography>{format((sourceData as { timestamp: number }).timestamp, 'MM/dd/yyyy HH:mm:ss')}</Typography> //getNotificationTableFromAssessment(sourceData as Assessment);
      default:
        return <Typography>No Data</Typography>
    }
  }

  const getNotificationTable = (notification: MessageMonitor.Notification) => {
    const fields = [['time', format(new Date(notification.notificationGeneratedAt), 'yyyy-MM-dd HH:mm:ss')]]
    switch (notification.notificationType) {
      case 'SpatBroadcastRateNotification':
        break
      case 'SignalStateConflictNotification':
        break
      case 'SignalGroupAlignmentNotification':
        break
      case 'MapBroadcastRateNotification':
        break
      case 'LaneDirectionOfTravelNotification':
        break
      case 'IntersectionReferenceAlignmentNotification':
        break
      case 'ConnectionOfTravelNotification':
        {
          const connectionOfTravelNotification = notification as ConnectionOfTravelNotification
          fields.push([
            'ingress Lane ID',
            // connectionOfTravelNotification?.assessment?.connectionOfTravelAssessmentGroups?.[0]?.ingressLaneID.toString(),
            connectionOfTravelNotification?.ingressLane.toString(),
          ])
          fields.push([
            'egress Lane ID',
            // connectionOfTravelNotification?.assessment?.connectionOfTravelAssessmentGroups?.[0]?.egressLaneID.toString(),
            connectionOfTravelNotification?.egressLane.toString(),
          ])
          fields.push([
            'event count',
            connectionOfTravelNotification?.assessment?.connectionOfTravelAssessmentGroups?.[0]?.eventCount.toString(),
          ])
          break
        }
        return (
          <>
            <Typography variant="h6">{notification?.notificationText}</Typography>
            <Box sx={{ mt: 1 }}>
              <CustomTable headers={['Field', 'Value']} data={notification == undefined ? [] : fields} />
            </Box>
          </>
        )
    }
  }

  const getSrmImportanceLevel = (level: ProcessedRequestImportanceLevel | undefined): string => {
    if (level?.includes('requestImportanceLevel')) {
      return level.replace('requestImportanceLevel', '')
    } else if (level === 'requestImportanceLevelUnKnown') {
      return 'Unknown'
    } else {
      return level
    }
  }

  const getSsmRow = (ssm: SsmInfo) => {
    const rows: any[] = []
    rows.push([`Seq. #`, ssm.sequenceNumber])
    rows.push([`Status`, ssm.status])
    rows.push([`Timestamp`, format(ssm.timeStampEpochMillis, 'yyyy-MM-dd HH:mm:ss.SSS')])
    if (ssm.inboundLaneID || ssm.outboundLaneID) {
      rows.push(['Inbound Lane', ssm.inboundLaneID])
      rows.push(['Outbound Lane', ssm.outboundLaneID])
    }
    if (ssm.inboundLaneConnectionID || ssm.outboundLaneConnectionID) {
      rows.push(['Inbound Lane Connection', ssm.inboundLaneConnectionID])
      rows.push(['Outbound Lane Connection', ssm.outboundLaneConnectionID])
    }
    if (ssm.requestInfo) {
      rows.push(['SRM Veh. ID', ssm.requestInfo.vehicleID])
      rows.push(['SRM Veh. Role', ssm.requestInfo.role])
      rows.push(['SRM Req. Level', getSrmImportanceLevel(ssm.requestInfo.importanceLevel)])
    }
    return (
      <Accordion
        sx={{
          '& .Mui-expanded': {
            backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
          },
        }}
        disableGutters
      >
        <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
          <Typography fontSize="16px">
            Request: {ssm.requestID}, Seq. Num: {ssm.sequenceNumber}, Status: {ssm.status}
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Box sx={{ mt: 1 }}>
            <CustomTable headers={['Field', 'Value']} data={rows} />
          </Box>
        </AccordionDetails>
      </Accordion>
    )
  }

  const getSrmRow = (srm: SrmInfo, ssmResponseDict: { [key: number]: SsmInfo[] }) => {
    const rows: any[] = []
    rows.push([`Request ID`, srm.requestID])
    rows.push(['Time', format(srm.timeStampEpochMillis, 'yyyy-MM-dd HH:mm:ss.SSS')])
    rows.push([`Request Type`, srm.priorityRequestType])
    if (srm.estimatedTimeOfArrival)
      rows.push([`Estimated Arrival`, format(srm.estimatedTimeOfArrival, 'yyyy-MM-dd HH:mm:ss.SSS')])
    if (srm.inboundLaneID || srm.outboundLaneID) {
      rows.push(['Inbound Lane', srm.inboundLaneID])
      rows.push(['Outbound Lane', srm.outboundLaneID])
    }
    if (srm.inboundLaneConnectionID || srm.outboundLaneConnectionID) {
      rows.push(['Inbound Lane Connection', srm.inboundLaneConnectionID])
      rows.push(['Outbound Lane Connection', srm.outboundLaneConnectionID])
    }
    const ssmKey = srm.vehicleInfo.vehicleID + '_' + srm.requestID
    const ssms: SsmInfo[] = ssmResponseDict[ssmKey]
    if (ssms) {
      const sortedSsms = ssms.toSorted((a, b) => (b.sequenceNumber ?? 0) - (a.sequenceNumber ?? 0))
      const matchingSsm = sortedSsms[0]
      rows.push([`  SSM Status (seq ${matchingSsm.requestInfo.requesterSequenceNumber})`, matchingSsm.status])
    }
    return (
      <Accordion
        sx={{
          '& .Mui-expanded': {
            backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
          },
        }}
        disableGutters
      >
        <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
          <Typography fontSize="16px">
            {srm.requestID}: {srm.vehicleInfo.vehicleID}
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Box sx={{ mt: 1 }}>
            <CustomTable headers={['Field', 'Value']} data={rows} />
          </Box>
        </AccordionDetails>
      </Accordion>
    )
  }

  return (
    <>
      <Fab
        sx={{
          position: 'absolute',
          zIndex: 10,
          top: theme.spacing(3),
          right: theme.spacing(3),
          backgroundColor: theme.palette.background.paper,
          '&:hover': {
            backgroundColor: theme.palette.custom.intersectionMapButtonHover,
          },
        }}
        size="small"
        onClick={() => {
          toggleOpen()
        }}
      >
        <InfoOutlined />
      </Fab>
      <div
        style={{
          position: 'absolute',
          zIndex: 10,
          bottom: theme.spacing(3),
          maxHeight: 'calc(100vh - 240px)',
          right: 0,
          width: props.openPanel === 'map-info' ? 600 : 0,
          fontSize: '16px',
          overflow: 'auto',
          scrollBehavior: 'auto',
          borderRadius: '4px',
        }}
      >
        {props.openPanel !== 'map-info' ? null : (
          <Box style={{ position: 'relative', height: '100%', width: '100%' }}>
            <Paper sx={{ height: '100%', width: '100%' }} square>
              <Box>
                <Box
                  sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 16px',
                  }}
                >
                  <Typography fontSize="16px">Information</Typography>
                  <IconButton
                    onClick={() => {
                      toggleOpen()
                    }}
                  >
                    <Close color="info" />
                  </IconButton>
                </Box>
                <Box
                  sx={{
                    maxHeight: '600px',
                    overflow: 'auto',
                    scrollbarColor: `${theme.palette.text.primary} ${theme.palette.background.paper}`,
                  }}
                >
                  <Accordion
                    disableGutters
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Lanes</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Box sx={{ mt: 1 }}>
                        <CustomTable
                          headers={['ingress', 'egress', 'status']}
                          data={
                            laneInfo?.features?.map((lane) => [
                              lane.properties.ingressLaneId,
                              lane.properties.egressLaneId,
                              signalGroups?.find((grp) => grp.signalGroup == lane.properties.signalGroupId)?.state ??
                                'no data',
                            ]) ?? []
                          }
                        />
                      </Box>
                    </AccordionDetails>
                  </Accordion>
                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">BSMs</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Box sx={{ mt: 1 }}>
                        <CustomTable
                          headers={['Time', 'Vehicle ID', 'Speed', 'Heading']}
                          data={
                            bsms?.features.map((bsm) => [
                              bsm.properties.secMark / 1000,
                              bsm.properties.id,
                              bsm.properties.speed,
                              bsm.properties.heading,
                            ]) ?? []
                          }
                        />
                      </Box>
                    </AccordionDetails>
                  </Accordion>
                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Events</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Box sx={{ mt: 1 }}>
                        <ExpandableTable
                          headers={['Time', 'Event Type']}
                          data={
                            events?.map((event) => [
                              format(event.eventGeneratedAt, 'MM/dd/yyyy HH:mm:ss'),
                              event.eventType,
                            ]) ?? []
                          }
                          details={events?.map((event) => JSON.stringify(event, null, 2)) ?? []}
                        />
                      </Box>
                    </AccordionDetails>
                  </Accordion>
                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Notifications</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Box sx={{ mt: 1 }}>
                        <ExpandableTable
                          headers={['Time', 'Type']}
                          data={
                            notifications?.map((notification) => [
                              format(notification.notificationGeneratedAt, 'MM/dd/yyyy HH:mm:ss'),
                              notification.notificationType,
                            ]) ?? []
                          }
                          details={notifications?.map((notification) => JSON.stringify(notification, null, 2)) ?? []}
                        />
                      </Box>
                    </AccordionDetails>
                  </Accordion>
                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Signal Request Messages (SRMs)</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Box>{srmInfo.map((srm) => getSrmRow(srm, ssmResponseDict))}</Box>
                    </AccordionDetails>
                  </Accordion>
                  <Accordion
                    sx={{
                      '& .Mui-expanded': {
                        backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                      },
                    }}
                    disableGutters
                  >
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="16px">Signal Status Messages (SSMs)</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Box sx={{ mt: 1 }}>{ssmInfo.map(getSsmRow)}</Box>
                    </AccordionDetails>
                  </Accordion>
                  {sourceDataType && (
                    <Accordion
                      sx={{
                        '& .Mui-expanded': {
                          backgroundColor: theme.palette.custom.intersectionMapAccordionExpanded,
                        },
                      }}
                      disableGutters
                    >
                      <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                        <Typography fontSize="16px">Source Data: {sourceDataType}</Typography>
                      </AccordionSummary>
                      <AccordionDetails>{getDataTable(sourceData, sourceDataType)}</AccordionDetails>
                    </Accordion>
                  )}
                </Box>
              </Box>
            </Paper>
          </Box>
        )}
      </div>
    </>
  )
}
