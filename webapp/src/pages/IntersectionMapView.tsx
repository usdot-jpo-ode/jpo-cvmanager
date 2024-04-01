import React from 'react'
import { useSelector } from 'react-redux'
import { Box, Container } from '@mui/material'
import MapTab from '../components/map/map-component'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../generalSlices/intersectionSlice'

function IntersectionMapView() {
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)

  return (
    <Box
      component="main"
      sx={{
        flexGrow: 1,
        py: 0,
      }}
    >
      <Container
        maxWidth={false}
        style={{ padding: 0, width: '100%', height: '100%', display: 'flex', position: 'relative' }}
      >
        <MapTab
          sourceData={undefined}
          sourceDataType={undefined}
          intersectionId={intersectionId}
          roadRegulatorId={roadRegulatorId}
          sourceApi={'conflictvisualizer'}
        />
      </Container>
    </Box>
  )
}

export default IntersectionMapView
