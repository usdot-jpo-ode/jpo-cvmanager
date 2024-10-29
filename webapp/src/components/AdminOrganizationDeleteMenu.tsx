import React from 'react'
import { confirmAlert } from 'react-confirm-alert'
import DeleteIcon from '@mui/icons-material/Delete'
import 'react-confirm-alert/src/react-confirm-alert.css'
import { Options } from './AdminDeletionOptions'

import '../features/adminRsuTab/Admin.css'
import { ContainedIconButton } from '../styles/components/ContainedIconButton'

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
      <ContainedIconButton
        key="delete_button"
        title="Delete Organization"
        onClick={handleDelete}
        sx={{
          float: 'left',
          margin: 2,
          mt: 0.5,
          mr: 0,
          ml: 0.5,
        }}
      >
        <DeleteIcon component={undefined} size={20} />
      </ContainedIconButton>
    </div>
  )
}

export default AdminOrganizationDeleteMenu
