import React from 'react'
import './css/NoTableWidth.css'
import { Route, Routes } from 'react-router-dom'
import { NotFound } from './404'
import BaseMapPage from '../components/intersections/map/BaseMapPage'
import NotificationMapPage from '../components/intersections/map/NotificationMapPage'
import IntersectionTsMapPage from '../components/intersections/map/IntersectionTsMapPage'

function IntersectionMapView() {
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
