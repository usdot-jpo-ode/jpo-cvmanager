import React, { useEffect } from 'react'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { useParams } from 'react-router-dom'
import { setSelectedIntersectionId, setSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { headerTabHeight } from '../../../styles/index'

const IntersectionTsMapPage = () => {
  const { intersectionId, roadRegulatorId, timestamp } = useParams<{
    intersectionId: string
    roadRegulatorId: string
    timestamp: string
  }>()

  const intersectionIdInt = parseInt(intersectionId) ?? -1
  const roadRegulatorIdInt = parseInt(roadRegulatorId) ?? -1
  const timestampInt = parseInt(timestamp) ?? 0

  useEffect(() => {
    setSelectedIntersectionId(intersectionIdInt)
    setSelectedRoadRegulatorId(roadRegulatorIdInt)
  }, [intersectionId])

  return (
    <div className="container">
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          py: 0,
        }}
      >
        <Container
          maxWidth={false}
          style={{
            width: '100%',
            height: `calc(100vh - ${headerTabHeight}px)`,
            display: 'flex',
            position: 'relative',
            padding: 0,
          }}
        >
          <IntersectionMap
            sourceData={timestampInt !== undefined ? { timestamp: timestampInt } : undefined}
            sourceDataType={timestampInt !== undefined ? 'timestamp' : undefined}
            intersectionId={intersectionIdInt}
            roadRegulatorId={roadRegulatorIdInt}
          />
        </Container>
      </Box>
    </div>
  )
}

export default IntersectionTsMapPage
