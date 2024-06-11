import { useEffect } from 'react'
import React from 'react'
import MessageMonitorApi from '../../apis/intersections/mm-api'
import VerticalTabs from '../VerticalTabs'
import { NotFound } from '../../pages/404'
import DashboardPage from './dashboard'
import NotificationsPage from './notifications'
import PerformanceReportsPage from './performance-reports'
import DataSelectorPage from './data-selector'
import { useSelector } from 'react-redux'
import { selectToken } from '../../generalSlices/userSlice'
import { selectSelectedIntersectionId, setSelectedIntersection } from '../../generalSlices/intersectionSlice'

export const DashboardLayout = (props) => {
  const { children } = props

  const authToken = useSelector(selectToken)

  const intersectionId = useSelector(selectSelectedIntersectionId)

  useEffect(() => {
    if (authToken) {
      MessageMonitorApi.getIntersections({ token: authToken }).then((intersections: IntersectionReferenceData[]) => {
        setSelectedIntersection(intersections?.[0]?.intersectionID)
      })
    } else {
      console.error('Did not attempt to update user automatically. Access token:', Boolean(authToken))
    }
  }, [authToken])

  return (
    <VerticalTabs
      notFoundRoute={
        <NotFound
          redirectRoute="/dashboard/admin"
          redirectRouteName="Admin Page"
          description="This page does not exist. Please return to the main admin page."
        />
      }
      defaultTabIndex={0}
      tabs={[
        {
          path: '',
          title: 'Dashboard',
          child: <DashboardPage />,
        },
        {
          path: 'notifications',
          title: 'Notifications',
          child: <NotificationsPage />,
        },
        {
          path: 'reports',
          title: 'Performance Reports',
          child: <PerformanceReportsPage />,
        },
        {
          path: 'data',
          title: 'Data Selector',
          child: <DataSelectorPage />,
        },
      ]}
    />
  )
}
