import { Box, Container, Grid, TextFieldProps } from '@mui/material'
import NotificationApi from '../../apis/intersections/notification-api'
import { ConnectionOfTravelAssessmentCard } from '../../features/intersections/assessments/connection-of-travel-assessment'
import { LaneDirectionOfTravelAssessmentCard } from '../../features/intersections/assessments/lane-direction-of-travel-assessment'
import { SignalStateEventAssessmentCard } from '../../features/intersections/assessments/signal-state-event-assessment'
import { StopLineStopAssessmentCard } from '../../features/intersections/assessments/stop-line-stop-assessment'
import { NotificationsTable } from '../../features/intersections/notifications/notifications-table'
import React, { useEffect, useState, useRef } from 'react'
import AssessmentsApi from '../../apis/intersections/assessments-api'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'

const tabs = [
  {
    label: 'All',
    value: 'all',
    description: 'All Assessments',
  },
  {
    label: 'Stop Line Stop Assessment',
    value: 'SignalStateAssessment',
    description: 'Signal State Assessment',
  },
  {
    label: 'Lane Direction of Travel Assessment',
    value: 'LaneDirectionOfTravelAssessment',
    description: 'Lane Direction of Travel Assessment',
  },
  {
    label: 'Connection of Travel Assessment',
    value: 'ConnectionOfTravelAssessment',
    description: 'Connection of Travel Assessment',
  },
  {
    label: 'Vehicle Stop Assessment',
    value: 'VehicleStopAssessment',
    description: 'Vehicle Stop Assessment',
  },
]

const Page = () => {
  const queryRef = useRef<TextFieldProps>(null)
  const [notifications, setNotifications] = useState<SpatBroadcastRateNotification>([])
  const [currentTab, setCurrentTab] = useState('all')
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [currentDescription, setCurrentDescription] = useState('')
  const [filter, setFilter] = useState({
    query: '',
    tab: currentTab,
  })
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)
  // create hooks, and methods for each assessment type:
  const [stopLineStopAssessment, setStopLineStopAssessment] = useState<StopLineStopAssessment | undefined>(undefined)
  // create hooks, and methods for each assessment type:
  const [signalStateEventAssessment, setSignalStateEventAssessment] = useState<SignalStateEventAssessment | undefined>(
    undefined
  )
  const [connectionOfTravelAssessment, setConnectionOfTravelAssessment] = useState<
    ConnectionOfTravelAssessment | undefined
  >(undefined)
  const [laneDirectionOfTravelAssessment, setLaneDirectionOfTravelAssessment] = useState<
    LaneDirectionOfTravelAssessment | undefined
  >(undefined)
  const token = useSelector(selectToken)

  const getAssessments = async () => {
    if (intersectionId) {
      setStopLineStopAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'signal_state_assessment',
          intersectionId,
          roadRegulatorId
        )) as StopLineStopAssessment
      )
      setSignalStateEventAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'signal_state_event_assessment',
          intersectionId,
          roadRegulatorId
        )) as SignalStateEventAssessment
      )
      setConnectionOfTravelAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'connection_of_travel',
          intersectionId,
          roadRegulatorId
        )) as ConnectionOfTravelAssessment
      )
      setLaneDirectionOfTravelAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'lane_direction_of_travel',
          intersectionId,
          roadRegulatorId
        )) as LaneDirectionOfTravelAssessment
      )
    } else {
      console.error('Did not attempt to get assessment data. Intersection ID:', intersectionId)
    }
  }

  const updateNotifications = () => {
    if (intersectionId) {
      setNotifications(
        NotificationApi.getActiveNotifications({
          token: token,
          intersectionId,
          roadRegulatorId,
        })
      )
    } else {
      console.error('Did not attempt to update notifications. Intersection ID:', intersectionId)
    }
  }

  useEffect(() => {
    updateNotifications()
    getAssessments()
  }, [intersectionId])

  useEffect(() => {
    updateDescription()
  }, [currentTab])

  const updateDescription = () => {
    for (let i = 0; i < tabs.length; i++) {
      if (tabs[i].value === currentTab) {
        setCurrentDescription(tabs[i].description)
      }
    }
  }

  return (
    <>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          py: 8,
        }}
      >
        <Container maxWidth={false}>
          <Grid container spacing={3}>
            <Grid item>
              <ConnectionOfTravelAssessmentCard assessment={connectionOfTravelAssessment} />
            </Grid>
            <Grid item>
              <LaneDirectionOfTravelAssessmentCard assessment={laneDirectionOfTravelAssessment} />
            </Grid>
            <Grid item>
              <StopLineStopAssessmentCard assessment={stopLineStopAssessment} />
            </Grid>
            <Grid item>
              <SignalStateEventAssessmentCard assessment={signalStateEventAssessment} />
            </Grid>
            <Grid item>
              <NotificationsTable simple={true} />
            </Grid>
          </Grid>
        </Container>
      </Box>
    </>
  )
}

export default Page
