import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import NotificationApi from '../../../apis/intersections/notification-api'
import { useParams } from 'react-router-dom'
import { selectToken } from '../../../generalSlices/userSlice'

function NotificationMapPage() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const { intersectionId, roadRegulatorId, notificationId } = useParams<{
    intersectionId: string
    roadRegulatorId: string
    notificationId: string
  }>()
  const [notification, setNotification] = useState<MessageMonitor.Notification | undefined>()
  const token = useSelector(selectToken)

  const intersectionIdInt = parseInt(intersectionId) ?? -1
  const roadRegulatorIdInt = parseInt(roadRegulatorId) ?? -1

  const updateNotifications = () => {
    if (intersectionId) {
      NotificationApi.getActiveNotifications({
        token: token,
        intersectionId: intersectionIdInt,
        roadRegulatorId: roadRegulatorIdInt,
        key: notificationId,
      }).then((notifications) => {
        const notif = notifications?.pop()
        setNotification(notif)
      })
    } else {
      console.error('Did not attempt to get notification data in map. Intersection ID:', intersectionId)
    }
  }

  useEffect(() => {
    updateNotifications()
  }, [intersectionId])

  return (
    <IntersectionMap
      sourceData={notification}
      sourceDataType={notification !== undefined ? 'notification' : undefined}
      intersectionId={intersectionIdInt}
      roadRegulatorId={roadRegulatorIdInt}
      loadOnNull={false}
    />
  )
}

export default NotificationMapPage
