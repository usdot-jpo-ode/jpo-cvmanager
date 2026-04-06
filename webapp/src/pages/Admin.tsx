import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { selectIsAdminOrAbove, selectOrganizationName } from '../generalSlices/userSlice'
import { updateTableData as updateIntersectionTableData } from '../features/adminIntersectionTab/adminIntersectionTabSlice'
import '../features/adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import AdminOrganizationTab from '../features/adminOrganizationTab/AdminOrganizationTab'
import AdminRsuTab from '../features/adminRsuTab/AdminRsuTab'
import AdminUserTab from '../features/adminUserTab/AdminUserTab'
import { NotFound } from './404'
import { getUserNotifications } from '../features/adminNotificationTab/adminNotificationTabSlice'
import VerticalTabs from '../components/VerticalTabs'
import { headerTabHeight } from '../styles/index'
import AdminIntersectionTab from '../features/adminIntersectionTab/AdminIntersectionTab'
import { evaluateFeatureFlags } from '../feature-flags'

function Admin() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const organization = useSelector(selectOrganizationName)
  const isAdmin = useSelector(selectIsAdminOrAbove)

  useEffect(() => {
    // This preloads data for the admin pages
    // Preload it with changes in dispatch and organization since it needs to be updated every time the organization is switched
    // in order to show only RSUs, Intersections, and Users of selected organization
    if (evaluateFeatureFlags('intersection')) dispatch(updateIntersectionTableData())
    dispatch(getUserNotifications())
  }, [dispatch, organization])

  return (
    <>
      {!isAdmin ? (
        <div id="admin">
          <NotFound description="You do not have permission to view this page. Please return to main dashboard: " />
        </div>
      ) : (
        <div id="admin">
          <VerticalTabs
            height={`calc(100vh - ${headerTabHeight}px)`}
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
