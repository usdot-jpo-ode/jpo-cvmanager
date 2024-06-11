import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../components/intersection/map/map-component'
import {
  getIntersections,
  selectSelectedIntersectionId,
  selectSelectedRoadRegulatorId,
} from '../generalSlices/intersectionSlice'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'

function IntersectionMapView() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

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

export default IntersectionMapView
