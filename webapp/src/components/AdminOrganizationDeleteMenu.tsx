import React from 'react'
import { confirmAlert } from 'react-confirm-alert'
import DeleteIcon from '@mui/icons-material/Delete'
import 'react-confirm-alert/src/react-confirm-alert.css'
import { Options } from './AdminDeletionOptions'

import '../features/adminRsuTab/Admin.css'
import { ContainedIconButton } from '../styles/components/ContainedIconButton'
import { alpha, useTheme } from '@mui/material/styles'
import { DeleteOutline } from '@mui/icons-material'

interface AdminOrganizationDeleteMenuProps {
  selectedOrganization: string
  deleteOrganization: () => void
}

const AdminOrganizationDeleteMenu = (props: AdminOrganizationDeleteMenuProps) => {
  const theme = useTheme()
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
          backgroundColor: 'transparent',
          borderRadius: '2px',
          '&:hover': {
            backgroundColor: alpha(theme.palette.text.primary, 0.1),
          },
        }}
      >
        <DeleteOutline sx={{ color: theme.palette.custom.rowActionIcon, fontSize: '1.5rem' }} component={undefined} />
      </ContainedIconButton>
    </div>
  )
}

export default AdminOrganizationDeleteMenu
