import React from 'react'
import { Box, Typography } from '@mui/material'
import DistanceFromCenterlineOverTimeGraph from './distance-from-centerline-over-time-graph'
import { LaneDirectionOfTravelReportDataByLaneId } from '../../../../models/ReportData'

interface DistanceFromCenterlineGraphSetProps {
  data: LaneDirectionOfTravelReportDataByLaneId
  distanceTolerance: number
}

const DistanceFromCenterlineGraphSet: React.FC<DistanceFromCenterlineGraphSetProps> = ({ data, distanceTolerance }) => {
  return (
    <Box>
      {Object.keys(data).length === 0 ? (
        <Typography variant="body1" align="center">
          No Data
        </Typography>
      ) : (
        Object.entries(data).map(([laneID, ldotReportData]) => (
          <Box key={laneID} id={`distance-from-centerline-graph-${laneID}`} sx={{ mb: 6 }}>
            <DistanceFromCenterlineOverTimeGraph
              data={ldotReportData}
              laneNumber={laneID.toString()}
              distanceTolerance={distanceTolerance}
            />
          </Box>
        ))
      )}
    </Box>
  )
}

export default DistanceFromCenterlineGraphSet
