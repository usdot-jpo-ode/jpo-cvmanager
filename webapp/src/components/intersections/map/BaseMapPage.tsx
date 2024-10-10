import React from 'react'
import { useSelector } from 'react-redux'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'

function BaseMapPage() {
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)

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
          style={{ width: '100%', height: 'calc(100vh - 135px)', display: 'flex', position: 'relative', padding: 0 }}
        >
          <IntersectionMap
            sourceData={undefined}
            sourceDataType={undefined}
            intersectionId={intersectionId}
            roadRegulatorId={roadRegulatorId}
            loadOnNull={true}
          />
        </Container>
      </Box>
    </div>
  )
}

export default BaseMapPage
