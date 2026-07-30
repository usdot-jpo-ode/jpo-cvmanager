import { useSelector } from 'react-redux'
import { selectIsAdminOrAbove } from '../generalSlices/userSlice'
import '../features/adminRsuTab/Admin.css'
import AdminOrganizationTab from '../features/adminOrganizationTab/AdminOrganizationTab'
import AdminRsuTab from '../features/adminRsuTab/AdminRsuTab'
import AdminUserTab from '../features/adminUserTab/AdminUserTab'
import { NotFound } from './404'
import VerticalTabs from '../components/VerticalTabs'
import { headerTabHeight } from '../styles/index'
import AdminIntersectionTab from '../features/adminIntersectionTab/AdminIntersectionTab'

function Admin() {
  const isAdmin = useSelector(selectIsAdminOrAbove)

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
