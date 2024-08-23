import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../features/intersections/map/map-component'
import './css/NoTableWidth.css'
import {
  getIntersections,
  selectSelectedIntersectionId,
  selectSelectedRoadRegulatorId,
} from '../generalSlices/intersectionSlice'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from './404'
import BaseMapPage from '../components/intersections/map/BaseMapPage'
import NotificationMapPage from '../components/intersections/map/NotificationMapPage'
import IntersectionTsMapPage from '../components/intersections/map/IntersectionTsMapPage'

function IntersectionMapView() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)

  return (
    <Routes>
      <Route path="/" element={<BaseMapPage />} />
      <Route path="notification/:intersectionId/:roadRegulatorId/:notificationId" element={<NotificationMapPage />} />
      <Route path="timestamp/:intersectionId/:roadRegulatorId/:timestamp" element={<IntersectionTsMapPage />} />
      <Route
        path="*"
        element={
          <NotFound
            redirectRoute="/dashboard/intersectionMap"
            redirectRouteName="Intersection Map Page"
            offsetHeight={319}
            description="This page does not exist. Please return to the intersection map page."
          />
        }
      />
    </Routes>
  )
}

export default IntersectionMapView
