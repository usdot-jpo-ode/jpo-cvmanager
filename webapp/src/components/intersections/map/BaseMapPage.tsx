import React from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { headerTabHeight } from '../../../styles/index'

function BaseMapPage() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const intersectionId = useSelector(selectSelectedIntersectionId)

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
            sourceData={undefined}
            sourceDataType={undefined}
            intersectionId={intersectionId}
            loadOnNull={true}
          />
        </Container>
      </Box>
    </div>
  )
}

export default BaseMapPage
