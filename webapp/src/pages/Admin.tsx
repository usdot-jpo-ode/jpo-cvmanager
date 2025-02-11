import React, { useEffect } from 'react'
import { useDispatch } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { updateTableData as updateIntersectionTableData } from '../features/adminIntersectionTab/adminIntersectionTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'
import '../features/adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import AdminOrganizationTab from '../features/adminOrganizationTab/AdminOrganizationTab'
import AdminRsuTab from '../features/adminRsuTab/AdminRsuTab'
import AdminUserTab from '../features/adminUserTab/AdminUserTab'
import { NotFound } from './404'
import { SecureStorageManager } from '../managers'
import { getUserNotifications } from '../features/adminNotificationTab/adminNotificationTabSlice'
import VerticalTabs from '../components/VerticalTabs'
import { headerTabHeight } from '../styles/index'
import AdminIntersectionTab from '../features/adminIntersectionTab/AdminIntersectionTab'
import { evaluateFeatureFlags } from '../feature-flags'

function Admin() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  useEffect(() => {
    if (evaluateFeatureFlags('rsu')) dispatch(updateRsuTableData())
    if (evaluateFeatureFlags('intersection')) dispatch(updateIntersectionTableData())
    dispatch(getAvailableUsers())
    dispatch(getUserNotifications())
  }, [dispatch])

  return (
    <>
      {SecureStorageManager.getUserRole() !== 'admin' ? (
        <div id="admin">
          <NotFound description="You do not have permission to view this page. Please return to main dashboard: " />
        </div>
      ) : (
        <div id="admin">
          <VerticalTabs
            height={`calc(100vh - 76px)`}
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
                path: 'rsus',
                title: 'RSUs',
                child: <AdminRsuTab />,
                tag: 'rsu',
              },
              {
                path: 'intersections',
                title: 'Intersections',
                child: <AdminIntersectionTab />,
                tag: 'intersection',
              },
              {
                path: 'users',
                title: 'Users',
                child: <AdminUserTab />,
              },
              {
                path: 'organizations',
                title: 'Organizations',
                child: <AdminOrganizationTab />,
              },
            ]}
          />
        </div>
      )}
    </>
  )
}

export default Admin
