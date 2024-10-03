import { Box, Container, Grid } from '@mui/material'
import React from 'react'
import MapTab from '../../features/intersections/map/map-component'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { useSelector } from 'react-redux'

const Map = () => {
  const currentDate = new Date()
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)
  return (
    <>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
        }}
      >
        <Container maxWidth={false} style={{ padding: 0, width: '100%', height: '100%', display: 'flex' }}>
          <MapTab
            sourceData={undefined}
            sourceDataType={undefined}
            intersectionId={intersectionId}
            roadRegulatorId={roadRegulatorId}
          />
        </Container>
      </Box>
    </>
  )
}

export default Map
