import React, { useEffect, useState } from 'react'
import { Box, Container } from '@mui/material'
import IntersectionMap from '../../../features/intersections/map/map-component'
import { setSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import NotificationApi from '../../../apis/intersections/notification-api'
import { useParams } from 'react-router-dom'
import { selectToken } from '../../../generalSlices/userSlice'
import { useSelector } from 'react-redux'
import { headerTabHeight } from '../../../styles/index'

function NotificationMapPage() {
  const { intersectionId, notificationId } = useParams<{
    intersectionId: string
    notificationId: string
  }>()
  const [notification, setNotification] = useState<MessageMonitor.Notification | undefined>()
  const token = useSelector(selectToken)

  const intersectionIdInt = parseInt(intersectionId) ?? -1

  const updateNotifications = () => {
    if (intersectionId) {
      NotificationApi.getActiveNotifications({
        token: token,
        intersectionId: intersectionIdInt,
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
          style={{
            width: '100%',
            height: `calc(100vh - ${headerTabHeight}px)`,
            display: 'flex',
            position: 'relative',
            padding: 0,
          }}
        >
          <IntersectionMap
            sourceData={notification}
            sourceDataType={notification !== undefined ? 'notification' : undefined}
            intersectionId={intersectionIdInt}
            loadOnNull={false}
          />
        </Container>
      </Box>
    </div>
  )
}

export default NotificationMapPage
