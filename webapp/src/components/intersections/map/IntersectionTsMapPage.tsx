import React from 'react'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { useParams } from 'react-router-dom'

const IntersectionTsMapPage = () => {
  const { intersectionId, timestamp } = useParams<{ intersectionId: string; timestamp: string }>()

  let timestampInt: number | undefined = undefined
  try {
    timestampInt = parseInt(timestamp as string)
  } catch (e) {
    timestampInt = undefined
  }

  let intersectionIdInt: number | undefined = undefined
  try {
    intersectionIdInt = parseInt(intersectionId as string)
  } catch (e) {
    intersectionIdInt = undefined
  }

  return (
    <div className="container">
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          py: 0,
        }}
      >
        <Container maxWidth={false} style={{ padding: 0, width: '100%', height: '100%', display: 'flex' }}>
          <IntersectionMap
            sourceData={timestampInt !== undefined ? { timestamp: timestampInt } : undefined}
            sourceDataType={timestampInt !== undefined ? 'timestamp' : undefined}
            intersectionId={intersectionIdInt}
            roadRegulatorId={-1}
          />
        </Container>
      </Box>
    </div>
  )
}

export default IntersectionTsMapPage
