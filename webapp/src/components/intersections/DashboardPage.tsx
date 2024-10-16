import { Box, Container, Grid2 } from '@mui/material'
import { NotificationsTable } from '../../features/intersections/notifications/notifications-table'
import { ConnectionOfTravelAssessmentCard } from '../../features/intersections/assessments/connection-of-travel-assessment'
import { LaneDirectionOfTravelAssessmentCard } from '../../features/intersections/assessments/lane-direction-of-travel-assessment'
import { StopLineStopAssessmentCard } from '../../features/intersections/assessments/stop-line-stop-assessment'
import { SignalStateEventAssessmentCard } from '../../features/intersections/assessments/signal-state-event-assessment'
import React, { useEffect, useState } from 'react'
import AssessmentsApi from '../../apis/intersections/assessments-api'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useAppSelector } from '../../hooks'

const Page = () => {
  const intersectionId = useAppSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useAppSelector(selectSelectedRoadRegulatorId)
  const token = useAppSelector(selectToken)

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
          <Grid2 container spacing={3} alignItems="flex-start">
            <ConnectionOfTravelAssessmentCard assessment={connectionOfTravelAssessment} />
            <StopLineStopAssessmentCard assessment={stopLineStopAssessment} />
            <SignalStateEventAssessmentCard assessment={signalStateEventAssessment} />
            <LaneDirectionOfTravelAssessmentCard assessment={laneDirectionOfTravelAssessment} />
            <Grid2 size={12}>
              <NotificationsTable simple={true} />
            </Grid2>
          </Grid2>
        </Container>
      </Box>
    </>
  )
}

export default Page
