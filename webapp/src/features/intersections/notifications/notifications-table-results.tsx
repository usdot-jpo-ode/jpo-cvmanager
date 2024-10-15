import PerfectScrollbar from 'react-perfect-scrollbar'
import PropTypes from 'prop-types'
import { format } from 'date-fns'
import {
  Box,
  Card,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
  IconButton,
  TableContainer,
  Collapse,
} from '@mui/material'
import React, { ReactElement } from 'react'
import MapRoundedIcon from '@mui/icons-material/MapRounded'
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp'
import { useNavigate } from 'react-router-dom'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { useAppSelector } from '../../../hooks'

export const NotificationsTableResults = ({
  customers,
  allTabNotifications,
  notificationsCount,
  selectedNotifications,
  onSelectedItemsChanged,
  expandedNotifications,
  onExpandedItemsChanged,
  onPageChange,
  onRowsPerPageChange,
  page,
  rowsPerPage,
}) => {
  const intersectionId = useAppSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useAppSelector(selectSelectedRoadRegulatorId)

  const navigate = useNavigate()
  const handleSelectAll = (event) => {
    let newSelectedCustomerIds: string[] = []
    if (notificationsCount === 0) return
    if (event.target.checked) {
      newSelectedCustomerIds = allTabNotifications.map((notification) => notification.key)
    } else {
      newSelectedCustomerIds = []
    }

    onSelectedItemsChanged(newSelectedCustomerIds)
  }

  const handleSelectOne = (event, notificationId: string) => {
    if (!selectedNotifications.includes(notificationId)) {
      onSelectedItemsChanged((prevSelected: string[]) => [...prevSelected, notificationId])
    } else {
      onSelectedItemsChanged((prevSelected: string[]) => prevSelected.filter((key: string) => key !== notificationId))
    }
  }

  const handleExpandOne = (notificationId: string) => {
    if (!expandedNotifications.includes(notificationId)) {
      onExpandedItemsChanged((prevExpanded: string[]) => [...prevExpanded, notificationId])
    } else {
      onExpandedItemsChanged((prevExpanded: string[]) => prevExpanded.filter((key: string) => key !== notificationId))
    }
  }

  const getDescriptionTextForNotification = (notification: MessageMonitor.Notification): ReactElement => {
    switch (notification.notificationType) {
      case 'ConnectionOfTravelNotification':
        const connectionOfTravelNotification = notification as ConnectionOfTravelNotification
        return (
          <Typography>
            {'Associated Assessment Data'}
            <br />
            {connectionOfTravelNotification.assessment.connectionOfTravelAssessmentGroups.map((assessmentGroup) => (
              <>
                {`- Ingress lane: ${assessmentGroup.ingressLaneID}, egress lane: ${assessmentGroup.egressLaneID}, connectionId: ${assessmentGroup.connectionID}, eventCount: ${assessmentGroup.eventCount}`}
                <br />
              </>
            ))}
          </Typography>
        )
      case 'IntersectionReferenceAlignmentNotification':
        const intersectionReferenceAlignmentNotification = notification as IntersectionReferenceAlignmentNotification
        const intersectionReferenceAlignmentEvent = intersectionReferenceAlignmentNotification.event
        const mapArr = Array.from(intersectionReferenceAlignmentEvent.mapRegulatorIntersectionIds) ?? []
        const spatArr = Array.from(intersectionReferenceAlignmentEvent.spatRegulatorIntersectionIds) ?? []
        return (
          <Typography>
            {`- Intersection IDs, MAP: ${mapArr.map((v) => v.intersectionId)}, SPAT: ${spatArr.map(
              (v) => v.intersectionId
            )}`}
            <br />
            {`- Road Regulator IDs, MAP: ${mapArr.map((v) => v.roadRegulatorId)}, SPAT: ${spatArr.map(
              (v) => v.roadRegulatorId
            )}`}
          </Typography>
        )
      case 'LaneDirectionOfTravelAssessmentNotification':
        const laneDirTravelNotification = notification as LaneDirectionOfTravelNotification
        const laneDirTravelAssessmentGroups = laneDirTravelNotification.assessment.laneDirectionOfTravelAssessmentGroup
        return (
          <Typography>
            {'Associated Assessment Data'}
            <br />
            {laneDirTravelAssessmentGroups.map((assessmentGroup) => {
              const numEvents = assessmentGroup.inToleranceEvents + assessmentGroup.outOfToleranceEvents
              const eventsRatio = assessmentGroup.inToleranceEvents / numEvents
              return (
                <>
                  {`- lane ID ${assessmentGroup.laneID}, in tolerance events ${eventsRatio} (${assessmentGroup.inToleranceEvents}/${numEvents})`}
                  <br />
                  {`  - Centerline Distance. Expected: ${assessmentGroup.distanceFromCenterlineTolerance}, Median: ${assessmentGroup.medianCenterlineDistance}, Median in tolerance: ${assessmentGroup.medianInToleranceCenterlineDistance}`}
                  <br />
                  {`  - Heading. Expected: ${assessmentGroup.expectedHeading}, Median: ${assessmentGroup.medianHeading}, Median in tolerance: ${assessmentGroup.medianInToleranceHeading}`}
                  <br />
                </>
              )
            })}
          </Typography>
        )
      case 'SignalGroupAlignmentNotification':
        const sigGroupAlignmentNotification = notification as SignalGroupAlignmentNotification
        const sigGroupAlignmentEvent = sigGroupAlignmentNotification.event as SignalGroupAlignmentEvent & {
          sourceID: string
          spatSignalGroupIds: number[]
          mapSignalGroupIds: number[]
        }
        return (
          <Typography>
            {`Source ID: ${sigGroupAlignmentEvent.sourceID}`}
            <br />
            {`- SPAT Signal Group IDs: ${sigGroupAlignmentEvent.spatSignalGroupIds}`}
            <br />
            {`- MAP Signal Group IDs: ${sigGroupAlignmentEvent.mapSignalGroupIds}`}
          </Typography>
        )
      case 'SignalStateConflictNotification':
        const sigStateConflictNotification = notification as SignalStateConflictNotification
        const sigStateConflictEvent = sigStateConflictNotification.event
        return (
          <Typography>
            {`Conflict type: ${sigStateConflictEvent.conflictType}`}
            <br />
            {`- First conflicting signal state: ${sigStateConflictEvent.firstConflictingSignalState} of group: ${sigStateConflictEvent.firstConflictingSignalGroup}`}
            <br />
            {`- Second conflicting signal state: ${sigStateConflictEvent.secondConflictingSignalState} of group: ${sigStateConflictEvent.secondConflictingSignalGroup}`}
          </Typography>
        )
      case 'TimeChangeDetailsNotification':
        const timeChangeDetailsNotification = notification as TimeChangeDetailsNotification
        const timeChangeDetailsEvent = timeChangeDetailsNotification.event
        return (
          <Typography>
            {`Signal group: ${timeChangeDetailsEvent.signalGroup}`}
            <br />
            {`- First conflicting timemark: ${timeChangeDetailsEvent.firstConflictingTimemark}, spat timestamp: ${timeChangeDetailsEvent.firstSpatTimestamp}, type: ${timeChangeDetailsEvent.firstTimeMarkType}`}
            <br />
            {`- Second conflicting timemark: ${timeChangeDetailsEvent.secondConflictingTimemark} spat timestamp: ${timeChangeDetailsEvent.secondSpatTimestamp}, type: ${timeChangeDetailsEvent.secondTimeMarkType}`}
          </Typography>
        )
      case 'KafkaStreamsAnomalyNotification':
        // No markers for this notification
        return <Typography>No Data</Typography>
      case 'BroadcastRateNotification':
        // No markers for this notification
        return <Typography>No Data</Typography>
      default:
        return <Typography>No Data</Typography>
    }
  }

  return (
    <Card sx={{ overflowX: 'auto' }}>
      <PerfectScrollbar>
        <Box sx={{ minWidth: 1050 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox
                    checked={selectedNotifications.length === notificationsCount && selectedNotifications.length}
                    color="primary"
                    indeterminate={
                      selectedNotifications.length > 0 && selectedNotifications.length < notificationsCount
                    }
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell>Notification Type</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Message</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {customers.map((notification: MessageMonitor.Notification) => {
                const isNotificationSelected = [...selectedNotifications].indexOf(notification.key) !== -1
                const isNotificationExpanded = [...expandedNotifications].indexOf(notification.key) !== -1
                return (
                  <>
                    <TableRow
                      hover
                      key={notification.key}
                      selected={[...selectedNotifications].indexOf(notification.key) !== -1}
                    >
                      <TableCell padding="checkbox">
                        <IconButton
                          aria-label="expand row"
                          size="small"
                          onClick={() => handleExpandOne(notification.key)}
                        >
                          {isNotificationExpanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                        </IconButton>
                      </TableCell>
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={isNotificationSelected}
                          onChange={(event) => handleSelectOne(event, notification.key)}
                          value="true"
                        />
                      </TableCell>
                      <TableCell>
                        <Box
                          sx={{
                            alignItems: 'center',
                            display: 'flex',
                          }}
                        >
                          <Typography color="textPrimary" variant="body1">
                            {notification.notificationType}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>{format(notification.notificationGeneratedAt, 'MM/dd/yyyy HH:mm:ss')}</TableCell>
                      <TableCell>{notification.notificationText}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          component="a"
                          onClick={() =>
                            navigate(
                              `/dashboard/intersectionMap/notification/${intersectionId}/${roadRegulatorId}/${notification.key}`
                            )
                          }
                        >
                          <MapRoundedIcon fontSize="medium" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
                        <Collapse in={isNotificationExpanded} timeout="auto" unmountOnExit>
                          <Box sx={{ margin: 1 }}>
                            <Typography color="textPrimary" variant="body1">
                              {getDescriptionTextForNotification(notification)}
                            </Typography>
                          </Box>
                        </Collapse>
                      </TableCell>
                    </TableRow>
                  </>
                )
              })}
            </TableBody>
          </Table>
        </Box>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={notificationsCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Card>
  )
}

NotificationsTableResults.propTypes = {
  customers: PropTypes.array.isRequired,
  onSelectedItemsChanged: PropTypes.func,
}
