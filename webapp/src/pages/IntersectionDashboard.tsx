import React, { useEffect } from 'react'
import { useDispatch } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import '../features/adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { NotFound } from './404'
import VerticalTabs from '../components/VerticalTabs'
import DashboardPage from '../components/intersections/DashboardPage'
import NotificationPage from '../components/intersections/NotificationPage'
import AssessmentsPage from '../components/intersections/AssessmentsPage'
import DataSelectorPage from '../components/intersections/DataSelectorPage'
import ConfigurationPage from '../components/intersections/ConfigurationPage'
import ReportsPage from '../components/intersections/ReportsPage'
import DecoderPage from '../components/intersections/DecoderPage'

function IntersectionDashboard() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  return (
    <>
      <div id="admin">
        <h2 className="adminHeader">CV Manager Admin Interface</h2>
        <VerticalTabs
          notFoundRoute={
            <NotFound
              redirectRoute="/dashboard/intersection"
              redirectRouteName="Intersection Dashboard Page"
              description="This page does not exist. Please return to the main admin page."
            />
          }
          defaultTabIndex={0}
          tabs={[
            {
              path: 'dashboard',
              title: 'Dashboard',
              child: <DashboardPage />,
            },
            {
              path: 'notifications',
              title: 'Notifications',
              child: <NotificationPage />,
            },
            {
              path: 'assessments',
              title: 'Assessments',
              child: <AssessmentsPage />,
            },
            {
              path: 'data-selector',
              title: 'Data Selector',
              child: <DataSelectorPage />,
            },
            {
              path: 'decoder',
              title: 'Decoder',
              child: <DecoderPage />,
            },
            {
              path: 'reports',
              title: 'Reports',
              child: <ReportsPage />,
            },
            {
              path: 'configuration',
              title: 'Configuration',
              child: <ConfigurationPage />,
            },
          ]}
        />
      </div>
    </>
  )
}

export default IntersectionDashboard
