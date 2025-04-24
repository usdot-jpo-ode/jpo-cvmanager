import { Box, Container, Grid2 } from '@mui/material'
import { NotificationsTable } from '../../features/intersections/notifications/notifications-table'
import { ConnectionOfTravelAssessmentCard } from '../../features/intersections/assessments/connection-of-travel-assessment'
import { LaneDirectionOfTravelAssessmentCard } from '../../features/intersections/assessments/lane-direction-of-travel-assessment'
import { StopLineStopAssessmentCard } from '../../features/intersections/assessments/stop-line-stop-assessment'
import { StopLinePassageAssessmentCard } from '../../features/intersections/assessments/stop-line-passage-assessment'
import React, { useEffect, useState } from 'react'
import AssessmentsApi from '../../apis/intersections/assessments-api'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'

const Page = () => {
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)
  const token = useSelector(selectToken)

  // create hooks, and methods for each assessment type:
  const [stopLineStopAssessment, setStopLineStopAssessment] = useState<StopLineStopAssessment | undefined>(undefined)
  // create hooks, and methods for each assessment type:
  const [stopLinePassageAssessment, setStopLinePassageAssessment] = useState<StopLinePassageAssessment | undefined>(
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
          'stop_line_stop_assessment',
          intersectionId,
          roadRegulatorId
        )) as StopLineStopAssessment
      )
      setStopLinePassageAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'stop_line_passage_assessment',
          intersectionId,
          roadRegulatorId
        )) as StopLinePassageAssessment
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
          <StopLinePassageAssessmentCard assessment={stopLinePassageAssessment} />
          <LaneDirectionOfTravelAssessmentCard assessment={laneDirectionOfTravelAssessment} />
          <Grid2 size={12}>
            <NotificationsTable simple={true} />
          </Grid2>
        </Grid2>
      </Container>
    </Box>
  )
}

export default Page
