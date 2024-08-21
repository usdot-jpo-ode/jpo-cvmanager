import React from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'

function BaseMapPage() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)

  return (
    <IntersectionMap
      sourceData={undefined}
      sourceDataType={undefined}
      intersectionId={intersectionId}
      roadRegulatorId={roadRegulatorId}
      loadOnNull={true}
    />
  )
}

export default BaseMapPage
