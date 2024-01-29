import React from 'react'
import AdminRsuTab from '../features/adminRsuTab/AdminRsuTab'
import AdminUserTab from '../features/adminUserTab/AdminUserTab'
import AdminOrganizationTab from '../features/adminOrganizationTab/AdminOrganizationTab'

import '../features/adminRsuTab/Admin.css'

interface AdminFormManagerProps {
  activeForm: 'add_rsu' | 'add_user' | 'add_organization'
}

const AdminFormManager = (props: AdminFormManagerProps) => {
  return (
    <div className="scroll-div">
      {(() => {
        if (props.activeForm === 'add_rsu') {
          return <AdminRsuTab />
        } else if (props.activeForm === 'add_user') {
          return <AdminUserTab />
        } else if (props.activeForm === 'add_organization') {
          return <AdminOrganizationTab />
        }
      })()}
    </div>
  )
}

export default AdminFormManager
