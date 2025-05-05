import { Box, Grid2, useTheme } from '@mui/material'
import { NotificationsTable } from '../../features/intersections/notifications/notifications-table'
import { ConnectionOfTravelAssessmentCard } from '../../features/intersections/assessments/connection-of-travel-assessment'
import { LaneDirectionOfTravelAssessmentCard } from '../../features/intersections/assessments/lane-direction-of-travel-assessment'
import { StopLineStopAssessmentCard } from '../../features/intersections/assessments/stop-line-stop-assessment'
import { SignalStateEventAssessmentCard } from '../../features/intersections/assessments/signal-state-event-assessment'
import React, { useEffect, useState } from 'react'
import AssessmentsApi from '../../apis/intersections/assessments-api'
import { selectSelectedIntersectionId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'

const Page = () => {
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const token = useSelector(selectToken)
  const theme = useTheme()

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
          intersectionId
        )) as StopLineStopAssessment
      )
      setSignalStateEventAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'signal_state_event_assessment',
          intersectionId
        )) as SignalStateEventAssessment
      )
      setConnectionOfTravelAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'connection_of_travel',
          intersectionId
        )) as ConnectionOfTravelAssessment
      )
      setLaneDirectionOfTravelAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'lane_direction_of_travel',
          intersectionId
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
        <Grid2
          container
          spacing={0.5}
          sx={{
            marginTop: theme.spacing(3),
          }}
          justifyContent="center"
        >
          <Grid2 size={{ xs: 12, md: 6, lg: 3 }}>
            <ConnectionOfTravelAssessmentCard assessment={connectionOfTravelAssessment} />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 6, lg: 3 }}>
            <StopLineStopAssessmentCard assessment={stopLineStopAssessment} />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 6, lg: 3 }}>
            <SignalStateEventAssessmentCard assessment={signalStateEventAssessment} />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 6, lg: 3 }}>
            <LaneDirectionOfTravelAssessmentCard assessment={laneDirectionOfTravelAssessment} />
          </Grid2>
          <Grid2 size={12}>
            <NotificationsTable simple={true} />
          </Grid2>
        </Grid2>
      </Box>
    </>
  )
}

export default Page
