import React from 'react'
import { Box, Typography } from '@mui/material'
import HeadingErrorOverTimeGraph from './heading-error-over-time-graph'
import { extractLaneIds, LaneDirectionOfTravelReportData } from '../report-utils'

interface HeadingErrorGraphSetProps {
  data: LaneDirectionOfTravelReportData[]
  headingTolerance: number // New prop
}

const HeadingErrorGraphSet: React.FC<HeadingErrorGraphSetProps> = ({ data, headingTolerance }) => {
  // Extract lane IDs using the helper function
  const laneIds = extractLaneIds(data)

  // Group data by LaneID
  const groupedData = data.reduce((acc, item) => {
    if (!acc[item.laneID]) {
      acc[item.laneID] = []
    }
    acc[item.laneID].push(item)
    return acc
  }, {} as { [laneID: number]: LaneDirectionOfTravelReportData[] })

  return (
    <Box>
      {laneIds.length === 0 ? (
        <Typography variant="body1" align="center">
          No Data
        </Typography>
      ) : (
        laneIds.map((laneID) => (
          <Box key={laneID} id={`heading-error-graph-${laneID}`} sx={{ mb: 6 }}>
            <HeadingErrorOverTimeGraph
              data={groupedData[laneID]}
              laneNumber={laneID.toString()}
              headingTolerance={headingTolerance}
            />
          </Box>
        ))
      )}
    </Box>
  )
}

export default HeadingErrorGraphSet
