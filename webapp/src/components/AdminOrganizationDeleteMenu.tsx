import React from 'react'
import { confirmAlert } from 'react-confirm-alert'
import DeleteIcon from '@mui/icons-material/Delete'
import 'react-confirm-alert/src/react-confirm-alert.css'
import { Options } from './AdminDeletionOptions'

import '../features/adminRsuTab/Admin.css'

interface AdminOrganizationDeleteMenuProps {
  selectedOrganization: string
  deleteOrganization: () => void
}

const AdminOrganizationDeleteMenu = (props: AdminOrganizationDeleteMenuProps) => {
  const handleDelete = () => {
    const buttons = [
      {
        label: 'Yes',
        onClick: () => props.deleteOrganization(),
      },
      { label: 'No', onClick: () => {} },
    ]
    const alertOptions = Options(
      'Delete Organization',
      'Are you sure you want to delete the "' + props.selectedOrganization + '" organization?',
      buttons
    )
    confirmAlert(alertOptions)
  }

  return (
    <div>
      <button className="delete_button" onClick={handleDelete} title="Delete Organization">
        <DeleteIcon size={20} component={undefined} />
      </button>
    </div>
  )
}

export default AdminOrganizationDeleteMenu
