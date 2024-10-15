import React, { useState } from 'react'

import { Paper, Box, IconButton, Typography } from '@mui/material'
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import MuiAccordion, { AccordionProps } from '@mui/material/Accordion'
import MuiAccordionSummary, { AccordionSummaryProps } from '@mui/material/AccordionSummary'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import ArrowForwardIosSharpIcon from '@mui/icons-material/ArrowForwardIosSharp'
import { styled } from '@mui/material/styles'
import { CustomTable } from './custom-table'
import { format } from 'date-fns'
import { ExpandableTable } from './expandable-table'
import { MAP_PROPS, selectSrmCount, selectSrmMsgList, selectSrmSsmCount } from './map-slice'
import { RsuInfo } from '../../../apis/rsu-api-types'
import SsmSrmItem from '../../../components/SsmSrmItem'
import { setSelectedSrm } from '../../../generalSlices/rsuSlice'
import { selectSelectedIntersection } from '../../../generalSlices/intersectionSlice'
import '../../../components/css/RsuMapView.css'
import { useAppDispatch, useAppSelector } from '../../../hooks'

const Accordion = styled((props: AccordionProps) => <MuiAccordion disableGutters elevation={0} square {...props} />)(
  ({ theme }) => ({})
)

const AccordionSummary = styled((props: AccordionSummaryProps) => (
  <MuiAccordionSummary expandIcon={<ArrowForwardIosSharpIcon sx={{ fontSize: '0.8rem' }} />} {...props} />
))(({ theme }) => ({
  minHeight: 0,
  paddingLeft: 10,
  flexDirection: 'row-reverse',
  '& .MuiAccordionSummary-expandIconWrapper.Mui-expanded': {
    transform: 'rotate(90deg)',
  },
  '& .MuiAccordionSummary-content': {
    marginLeft: theme.spacing(1),
    marginTop: 0,
    marginBottom: 0,
  },
}))

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

  const dispatch = useAppDispatch()

  const srmCount = useAppSelector(selectSrmCount)
  const srmSsmCount = useAppSelector(selectSrmSsmCount)
  const srmMsgList = useAppSelector(selectSrmMsgList)
  const selectedIntersection = useAppSelector(selectSelectedIntersection)

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

  const getRsuInfoTable = (rsuInfo: RsuInfo['rsuList'][0]) => {
    const fields = [
      ['id', rsuInfo?.properties?.rsu_id],
      ['milepost', rsuInfo?.properties?.milepost],
      ['geography', rsuInfo?.properties?.geography],
      ['model_name', rsuInfo?.properties?.model_name],
      ['ipv4_address', rsuInfo?.properties?.ipv4_address],
      ['primary_route', rsuInfo?.properties?.primary_route],
      ['serial_number', rsuInfo?.properties?.serial_number],
      ['manufacturer_name', rsuInfo?.properties?.manufacturer_name],
    ]
    return (
      <>
        <Typography variant="h6">{rsuInfo?.properties?.ipv4_address}</Typography>
        <Box sx={{ mt: 1 }}>
          <CustomTable headers={['Field', 'Value']} data={rsuInfo == undefined ? [] : fields} />
        </Box>
      </>
    )
  }

  const getSsmSrmTable = (msgList, rsuIpv4: string | undefined, ssmCount: number, srmCount: number) => {
    if (rsuIpv4 == undefined) return <div>No RSU IP Found</div>
    return (
      <div className="ssmSrmContainer">
        <h3 id="ssmsrmDataHeader">SSM / SRM Data For {rsuIpv4}</h3>
        <div id="ssmSrmHeaderContainer">
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
    <div
      style={{
        position: 'absolute',
        zIndex: 10,
        top: 0,
        bottom: 0,
        right: 0,
        width: open ? 450 : 50,
        fontSize: '16px',
        overflow: 'auto',
        scrollBehavior: 'auto',
      }}
    >
      <Box style={{ position: 'relative', height: '100%', width: '100%' }}>
        <Paper sx={{ height: '100%', width: '100%' }}>
          <Box>
            <IconButton
              onClick={() => {
                setOpen((prev) => !prev)
              }}
            >
              {open ? <ChevronRightIcon /> : <ChevronLeftIcon />}
            </IconButton>
            {!open ? (
              <Box></Box>
            ) : (
              <>
                <Typography variant="h5" sx={{ px: 2 }}>
                  Information Panel
                </Typography>
                <Accordion disableGutters>
                  <AccordionSummary>
                    <Typography variant="h5">Lanes</Typography>
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
                  <AccordionSummary>
                    <Typography variant="h5">BSMs</Typography>
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
                  <AccordionSummary>
                    <Typography variant="h5">Events</Typography>
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
                  <AccordionSummary>
                    <Typography variant="h5">Notifications</Typography>
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
                  <AccordionSummary>
                    <Typography variant="h5">Ssm Srm Data</Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Box sx={{ mt: 1 }}>
                      {getSsmSrmTable(srmMsgList, selectedIntersection?.rsuIP, srmSsmCount, srmCount)}
                    </Box>
                  </AccordionDetails>
                </Accordion>
                {sourceDataType && (
                  <Accordion disableGutters>
                    <AccordionSummary>
                      <Typography variant="h5">Source Data: {sourceDataType}</Typography>
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
  )
}
