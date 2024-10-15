import React, { useEffect, useState } from 'react'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { setSelectedIntersectionId, setSelectedRoadRegulatorId } from '../../../generalSlices/intersectionSlice'
import NotificationApi from '../../../apis/intersections/notification-api'
import { useParams } from 'react-router-dom'
import { selectToken } from '../../../generalSlices/userSlice'
import { useAppSelector } from '../../../hooks'

function NotificationMapPage() {
  const { intersectionId, roadRegulatorId, notificationId } = useParams<{
    intersectionId: string
    roadRegulatorId: string
    notificationId: string
  }>()
  const [notification, setNotification] = useState<MessageMonitor.Notification | undefined>()
  const token = useAppSelector(selectToken)

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
          style={{ width: '100%', height: 'calc(100vh - 135px)', display: 'flex', position: 'relative', padding: 0 }}
        >
          <IntersectionMap
            sourceData={notification}
            sourceDataType={notification !== undefined ? 'notification' : undefined}
            intersectionId={intersectionIdInt}
            roadRegulatorId={roadRegulatorIdInt}
            loadOnNull={false}
          />
        </Container>
      </Box>
    </div>
  )
}

export default NotificationMapPage
