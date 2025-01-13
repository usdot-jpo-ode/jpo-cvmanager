import React, { useEffect } from 'react'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { updateTableData as updateIntersectionTableData } from '../features/adminIntersectionTab/adminIntersectionTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'
import '../features/adminRsuTab/Admin.css'
import AdminOrganizationTab from '../features/adminOrganizationTab/AdminOrganizationTab'
import AdminRsuTab from '../features/adminRsuTab/AdminRsuTab'
import AdminUserTab from '../features/adminUserTab/AdminUserTab'
import { NotFound } from './404'
import { SecureStorageManager } from '../managers'
import { getUserNotifications } from '../features/adminNotificationTab/adminNotificationTabSlice'
import VerticalTabs from '../components/VerticalTabs'
import { headerTabHeight } from '../styles/index'
import AdminIntersectionTab from '../features/adminIntersectionTab/AdminIntersectionTab'
import { useAppDispatch } from '../hooks'
import { evaluateFeatureFlags } from '../feature-flags'

function Admin() {
  const dispatch = useAppDispatch()

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
          <h2 className="adminHeader">Admin Interface</h2>
          <VerticalTabs
            height={`calc(100vh - ${headerTabHeight + 76}px)`}
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
