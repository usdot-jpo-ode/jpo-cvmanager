import React, { useState } from 'react'

import { Paper, Box, IconButton, Typography, Fab, AccordionSummary } from '@mui/material'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'

import MuiAccordionDetails from '@mui/material/AccordionDetails'
import { styled, useTheme } from '@mui/material/styles'
import { CustomTable } from './custom-table'
import { format } from 'date-fns'
import { ExpandableTable } from './expandable-table'
import { MAP_PROPS, selectSrmCount, selectSrmMsgList, selectSrmSsmCount } from './map-slice'
import SsmSrmItem from '../../../components/SsmSrmItem'
import { setSelectedSrm } from '../../../generalSlices/rsuSlice'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { useDispatch, useSelector } from 'react-redux'
import { RootState } from '../../../store'
import { selectSelectedIntersection } from '../../../generalSlices/intersectionSlice'
import '../../../components/css/RsuMapView.css'
import { InfoOutlined, Close, ExpandMoreOutlined } from '@mui/icons-material'

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  ({ theme }) => ({})
)

const AccordionDetails = styled(MuiAccordionDetails)(({ theme }) => ({}))

interface SidePanelProps {
  laneInfo: ConnectingLanesFeatureCollection | undefined
  signalGroups: SpatSignalGroup[] | undefined
  bsms: BsmFeatureCollection
  events: MessageMonitor.Event[]
  notifications: MessageMonitor.Notification[]
  sourceData: MAP_PROPS['sourceData']
  sourceDataType: MAP_PROPS['sourceDataType']
}

export const SidePanel = (props: SidePanelProps) => {
  const { laneInfo, signalGroups, bsms, events, notifications, sourceData, sourceDataType } = props

  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()

  const srmCount = useSelector(selectSrmCount)
  const srmSsmCount = useSelector(selectSrmSsmCount)
  const srmMsgList = useSelector(selectSrmMsgList)
  const selectedIntersection = useSelector(selectSelectedIntersection)

  const [open, setOpen] = useState(false)

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

  const getSsmSrmTable = (msgList, rsuIpv4: string | undefined, ssmCount: number, srmCount: number) => {
    if (rsuIpv4 == undefined) return <div>No RSU IP Found</div>
    return (
      <div className="ssmSrmContainer">
        <h3 id="ssmsrmDataHeader">SSM / SRM Data For {rsuIpv4}</h3>
        <div id="ssmSrmHeaderContainer" style={{ borderBottom: `1px ${theme.palette.text.primary} solid` }}>
          <p id="ssmTimeHeader">Time</p>
          <p id="requestHeader">Request Id</p>
          <p id="roleHeader">Role</p>
          <p id="ssmSrmHeader">Status</p>
          <p id="ssmSrmHeader">Display</p>
        </div>
        {msgList.map((index) => (
          <SsmSrmItem key={index} elem={msgList[index]} setSelectedSrm={() => dispatch(setSelectedSrm())} />
        ))}
        <h3 id="countsHeader">Total Counts</h3>
        <div id="countsContainer">
          <h4 id="countsData">SSM: {ssmCount}</h4>
          <h4 id="countsData">SRM: {srmCount}</h4>
        </div>
      </div>
    )
  }

  return (
    <>
      <Fab
        style={{
          position: 'absolute',
          zIndex: 10,
          top: theme.spacing(3),
          right: theme.spacing(3),
          backgroundColor: theme.palette.background.paper,
        }}
        size="small"
        onClick={() => {
          setOpen(!open)
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
          width: open ? 600 : 50,
          fontSize: '16px',
          overflow: 'auto',
          scrollBehavior: 'auto',
        }}
      >
        <Box style={{ position: 'relative', height: '100%', width: '100%' }}>
          <Paper sx={{ height: '100%', width: '100%' }} square>
            <Box>
              {!open ? null : (
                <>
                  <Box
                    sx={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      mb: 2,
                      px: 1,
                    }}
                  >
                    <Typography variant="h6">Information</Typography>
                    <IconButton
                      onClick={() => {
                        setOpen(!open)
                      }}
                    >
                      <Close color="info" />
                    </IconButton>
                  </Box>
                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">Lanes</Typography>
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
                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">BSMs</Typography>
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
                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">Events</Typography>
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
                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">Notifications</Typography>
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
                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                      <Typography fontSize="small">Ssm Srm Data</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Box sx={{ mt: 1 }}>
                        {getSsmSrmTable(srmMsgList, selectedIntersection?.rsuIP, srmSsmCount, srmCount)}
                      </Box>
                    </AccordionDetails>
                  </Accordion>
                  {sourceDataType && (
                    <Accordion disableGutters>
                      <AccordionSummary expandIcon={<ExpandMoreOutlined />}>
                        <Typography fontSize="small">Source Data: {sourceDataType}</Typography>
                      </AccordionSummary>
                      <AccordionDetails>{getDataTable(sourceData, sourceDataType)}</AccordionDetails>
                    </Accordion>
                  )}
                </>
              )}
            </Box>
          </Paper>
        </Box>
      </div>
    </>
  )
}
