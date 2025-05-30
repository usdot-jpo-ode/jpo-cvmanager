import { Box, Container, Grid2 } from '@mui/material'
import { NotificationsTable } from '../../features/intersections/notifications/notifications-table'
import { ConnectionOfTravelAssessmentCard } from '../../features/intersections/assessments/connection-of-travel-assessment'
import { LaneDirectionOfTravelAssessmentCard } from '../../features/intersections/assessments/lane-direction-of-travel-assessment'
import { StopLineStopAssessmentCard } from '../../features/intersections/assessments/stop-line-stop-assessment'
import { StopLinePassageAssessmentCard } from '../../features/intersections/assessments/stop-line-passage-assessment'
import React, { useEffect, useMemo, useState } from 'react'
import AssessmentsApi from '../../apis/intersections/assessments-api'
import { selectSelectedIntersectionId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'

const Page = () => {
  const intersectionId = useSelector(selectSelectedIntersectionId)
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
          intersectionId
        )) as StopLineStopAssessment
      )
      setStopLinePassageAssessment(
        (await AssessmentsApi.getLatestAssessment(
          token,
          'stop_line_passage_assessment',
          intersectionId
        )) as StopLinePassageAssessment
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

  // The grid breakpoints are based on the full screen width of 1200px, and the padding offset is the difference
  // between the full screen width and rendered width of the grid area, measured 2025/05/09 as 838px.
  const paddingOffset = 362 // 1200 - 838
  const chartCardPadding = 10 // extra padding around plots, make sure breakpoint triggers before scrolling cards
  const muiBreakpoints = {
    xs: 0,
    sm: 600 - paddingOffset,
    md: 900 - paddingOffset,
    lg: 1200 - paddingOffset,
    xl: 1536 - paddingOffset,
  }

  const computeWidth = (minWidth: number, totalWidth: number, gridWidth: number) => {
    const ratio = minWidth / totalWidth
    if (ratio < 0.25) {
      return gridWidth / 4
    } else if (ratio < 0.5) {
      return gridWidth / 2
    }
    return gridWidth
  }

  const generateBreakpoints = (minWidth: number) => {
    const gridWidth = 12
    return {
      xs: gridWidth,
      sm: gridWidth,
      md: computeWidth(minWidth, muiBreakpoints.md, gridWidth),
      lg: computeWidth(minWidth, muiBreakpoints.lg, gridWidth),
      xl: computeWidth(minWidth, muiBreakpoints.xl, gridWidth),
    }
  }

  const widthPerBar = 22
  const legendPadding = 100
  const minChartWidth = 200

  const getChartWidth = (numBars: number) => Math.max((numBars ?? 0) * widthPerBar + legendPadding, minChartWidth)
  const connectionOfTravelMinWidth = useMemo(
    () => getChartWidth(connectionOfTravelAssessment?.connectionOfTravelAssessmentGroups?.length),
    [connectionOfTravelAssessment]
  )
  const stopLineStopMinWidth = useMemo(
    () => getChartWidth(stopLineStopAssessment?.stopLineStopAssessmentGroup?.length),
    [stopLineStopAssessment]
  )
  const stopLinePassageMinWidth = useMemo(
    () => getChartWidth(stopLinePassageAssessment?.stopLinePassageAssessmentGroup?.length),
    [stopLinePassageAssessment]
  )

  // Lane direction of travel shows 2 bars per unique lane ID
  const laneDirectionOfTravelMinWidth = useMemo(() => {
    const laneDirectionOfTravelNumUniqueLanes =
      new Set(laneDirectionOfTravelAssessment?.laneDirectionOfTravelAssessmentGroup?.map((group) => group.laneID) ?? [])
        .size * 2
    return getChartWidth(laneDirectionOfTravelNumUniqueLanes)
  }, [laneDirectionOfTravelAssessment])

  const connectionOfTravelBreakpoints = useMemo(
    () => generateBreakpoints(connectionOfTravelMinWidth + chartCardPadding),
    [connectionOfTravelMinWidth]
  )
  const stopLineStopBreakpoints = useMemo(
    () => generateBreakpoints(stopLineStopMinWidth + chartCardPadding),
    [stopLineStopMinWidth]
  )
  const stopLinePassageBreakpoints = useMemo(
    () => generateBreakpoints(stopLinePassageMinWidth + chartCardPadding),
    [stopLinePassageMinWidth]
  )
  const laneDirectionOfTravelBreakpoints = useMemo(
    () => generateBreakpoints(laneDirectionOfTravelMinWidth + chartCardPadding),
    [laneDirectionOfTravelMinWidth]
  )

  return (
    <>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          py: 8,
        }}
      >
        <Grid2 container spacing={2} justifyContent="flex-start">
          <Grid2 size={connectionOfTravelBreakpoints}>
            <ConnectionOfTravelAssessmentCard
              assessment={connectionOfTravelAssessment}
              minWidth={connectionOfTravelMinWidth}
            />
          </Grid2>
          <Grid2 size={stopLineStopBreakpoints}>
            <StopLineStopAssessmentCard assessment={stopLineStopAssessment} minWidth={stopLineStopMinWidth} />
          </Grid2>
          <Grid2 size={stopLinePassageBreakpoints}>
            <StopLinePassageAssessmentCard assessment={stopLinePassageAssessment} minWidth={stopLinePassageMinWidth} />
          </Grid2>
          <Grid2 size={laneDirectionOfTravelBreakpoints}>
            <LaneDirectionOfTravelAssessmentCard
              assessment={laneDirectionOfTravelAssessment}
              minWidth={laneDirectionOfTravelMinWidth}
            />
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
