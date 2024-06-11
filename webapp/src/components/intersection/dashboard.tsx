import { Box, Container, Grid } from '@mui/material'
import { NotificationsTable } from './notifications/notifications-table'
import { ConnectionOfTravelAssessmentCard } from './assessments/connection-of-travel-assessment'
import { LaneDirectionOfTravelAssessmentCard } from './assessments/lane-direction-of-travel-assessment'
import { StopLineStopAssessmentCard } from './assessments/stop-line-stop-assessment'
import { SignalStateEventAssessmentCard } from './assessments/signal-state-event-assessment'
import React, { useEffect, useState, useRef } from 'react'
import AssessmentsApi from '../../apis/intersections/assessments-api'
import { selectToken } from '../../generalSlices/userSlice'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { useSelector } from 'react-redux'

const DashboardPage = () => {
  const authToken = useSelector(selectToken)

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

  const getAssessments = async () => {
    if (intersectionId && authToken) {
      setStopLineStopAssessment(
        (await AssessmentsApi.getLatestAssessment(
          authToken,
          'signal_state_assessment',
          intersectionId,
          roadRegulatorId
        )) as StopLineStopAssessment
      )
      setSignalStateEventAssessment(
        (await AssessmentsApi.getLatestAssessment(
          authToken,
          'signal_state_event_assessment',
          intersectionId,
          roadRegulatorId
        )) as SignalStateEventAssessment
      )
      setConnectionOfTravelAssessment(
        (await AssessmentsApi.getLatestAssessment(
          authToken,
          'connection_of_travel',
          intersectionId,
          roadRegulatorId
        )) as ConnectionOfTravelAssessment
      )
      setLaneDirectionOfTravelAssessment(
        (await AssessmentsApi.getLatestAssessment(
          authToken,
          'lane_direction_of_travel',
          intersectionId,
          roadRegulatorId
        )) as LaneDirectionOfTravelAssessment
      )
    } else {
      console.error(
        'Did not attempt to get assessment data. Access token:',
        authToken,
        'Intersection ID:',
        intersectionId
      )
    }
  }

  useEffect(() => {
    getAssessments()
  }, [intersectionId])

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
          <Grid container spacing={3} alignItems="flex-start">
            <ConnectionOfTravelAssessmentCard assessment={connectionOfTravelAssessment} />
            <StopLineStopAssessmentCard assessment={stopLineStopAssessment} />
            <SignalStateEventAssessmentCard assessment={signalStateEventAssessment} />
            <LaneDirectionOfTravelAssessmentCard assessment={laneDirectionOfTravelAssessment} />
            <Grid item xs={12}>
              <NotificationsTable simple={true} />
            </Grid>
          </Grid>
        </Container>
      </Box>
    </>
  )
}

export default DashboardPage
