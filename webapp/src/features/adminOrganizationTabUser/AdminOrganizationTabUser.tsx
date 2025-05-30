import React, { useState, useEffect } from 'react'
import AdminTable from '../../components/AdminTable'
import Accordion from '@mui/material/Accordion'
import AccordionSummary from '@mui/material/AccordionSummary'
import AccordionDetails from '@mui/material/AccordionDetails'
import Typography from '@mui/material/Typography'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { DropdownList, Multiselect } from 'react-widgets'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import {
  selectAvailableUserList,
  selectSelectedUserList,
  selectAvailableRoles,

  // actions
  getAvailableRoles,
  getAvailableUsers,
  userDeleteSingle,
  userDeleteMultiple,
  userAddMultiple,
  userBulkEdit as userBulkEditAction,
  setSelectedUserRole,
  setSelectedUserList,
} from './adminOrganizationTabUserSlice'
import {
  selectAuthLoginData,
  selectEmail,
  selectLoadingGlobal,
  setOrganizationList,
} from '../../generalSlices/userSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action, Column } from '@material-table/core'
import { AdminOrgUser } from '../adminOrganizationTab/adminOrganizationTabSlice'
import toast from 'react-hot-toast'

import { useTheme } from '@mui/material'
import { AddCircleOutline, DeleteOutline } from '@mui/icons-material'

interface AdminOrganizationTabUserProps {
  selectedOrg: string
  selectedOrgEmail: string
  tableData: AdminOrgUser[]
  updateTableData: (org: string) => void
}

const AdminOrganizationTabUser = (props: AdminOrganizationTabUserProps) => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const { selectedOrg, selectedOrgEmail } = props
  const availableUserList = useSelector(selectAvailableUserList)
  const selectedUserList = useSelector(selectSelectedUserList)
  const availableRoles = useSelector(selectAvailableRoles)
  const loadingGlobal = useSelector(selectLoadingGlobal)
  const authLoginData = useSelector(selectAuthLoginData)
  const userEmail = useSelector(selectEmail)
  const [userColumns] = useState<Column<any>[]>([
    {
      title: 'First Name',
      field: 'first_name',
      editable: 'never',
      id: 0,
    },
    {
      title: 'Last Name',
      field: 'last_name',
      editable: 'never',
      id: 1,
    },
    { title: 'Email', field: 'email', editable: 'never', id: 2 },
    {
      title: 'Role',
      field: 'role',
      id: 3,
      lookup: { user: 'User', operator: 'Operator', admin: 'Admin' },
    },
  ])

  let userActions: Action<AdminOrgUser>[] = [
    {
      icon: () => <DeleteOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      iconProps: {
        itemType: 'rowAction',
      },
      position: 'row',
      onClick: (event, rowData: AdminOrgUser) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => userOnDelete(rowData),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options(
          'Delete User',
          'Are you sure you want to delete "' + rowData.email + '" from ' + props.selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      position: 'toolbarOnSelect',
      iconProps: {
        itemType: 'rowAction',
      },
      onClick: (event, rowData: AdminOrgUser[]) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => userMultiDelete(rowData),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options(
          'Delete Selected Users',
          'Are you sure you want to delete ' + rowData.length + ' users from ' + props.selectedOrg + ' organization?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      position: 'toolbar',
      iconProps: {
        itemType: 'displayIcon',
      },
      icon: () => (
        <Multiselect
          className="org-multiselect"
          dataKey="id"
          textField="email"
          placeholder="Click to add users"
          data={availableUserList}
          value={selectedUserList}
          onChange={(value) => dispatch(setSelectedUserList(value))}
          style={{
            fontSize: '1rem',
          }}
        />
      ),
      onClick: () => {},
    },
    {
      position: 'toolbar',
      iconProps: {
        title: 'Add User',
        color: 'primary',
        itemType: 'contained',
      },
      icon: () => <AddCircleOutline />,
      onClick: () => userMultiAdd(selectedUserList),
    },
  ]

  let userTableEditable = {
    onBulkUpdate: (
      changes: Record<
        number,
        {
          oldData: AdminOrgUser
          newData: AdminOrgUser
        }
      >
    ) =>
      new Promise((resolve, reject) => {
        userBulkEdit(changes)
        setTimeout(() => {
          resolve(null)
        }, 2000)
      }),
  }

  useEffect(() => {
    dispatch(getAvailableRoles())
  }, [dispatch])

  useEffect(() => {
    dispatch(setSelectedUserList([]))
    dispatch(getAvailableUsers(selectedOrg))
  }, [selectedOrg, dispatch])

  const userOnDelete = async (row: AdminOrgUser) => {
    dispatch(
      userDeleteSingle({
        user: row,
        selectedOrg: props.selectedOrg,
        selectedOrgEmail: props.selectedOrgEmail,
        updateTableData: props.updateTableData,
      })
    ).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })

    if (row.email === authLoginData?.data?.email) {
      dispatch(setOrganizationList({ value: { name: props.selectedOrg, role: row.role }, type: 'delete' }))
    }
  }

  const userMultiDelete = async (rows: AdminOrgUser[]) => {
    dispatch(
      userDeleteMultiple({
        users: rows,
        selectedOrg: props.selectedOrg,
        selectedOrgEmail: props.selectedOrgEmail,
        updateTableData: props.updateTableData,
      })
    ).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })

    for (let i = 0; i < rows.length; i++) {
      if (rows[i].email === authLoginData?.data?.email) {
        dispatch(setOrganizationList({ value: { name: props.selectedOrg, role: rows[i].role }, type: 'delete' }))
      }
    }
  }

  const userMultiAdd = async (userList: AdminOrgUser[]) => {
    if (userList.length === 0) {
      toast.error('Please select users to add')
      return
    }
    dispatch(
      userAddMultiple({
        userList,
        selectedOrg: props.selectedOrg,
        selectedOrgEmail: props.selectedOrgEmail,
        updateTableData: props.updateTableData,
      })
    ).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })

    for (let i = 0; i < userList.length; i++) {
      if (userList[i].email === authLoginData?.data?.email) {
        dispatch(setOrganizationList({ value: { name: props.selectedOrg, role: userList[i].role }, type: 'add' }))
      }
    }
  }

  const userBulkEdit = async (
    json: Record<
      number,
      {
        oldData: AdminOrgUser
        newData: AdminOrgUser
      }
    >
  ) => {
    dispatch(
      userBulkEditAction({
        json,
        selectedUser: userEmail,
        selectedOrg: props.selectedOrg,
        selectedOrgEmail: props.selectedOrgEmail,
        updateTableData: props.updateTableData,
      })
    ).then((data) => {
      if (!(data.payload as any).success) {
        toast.error((data.payload as any).message)
      } else {
        toast.success((data.payload as any).message)
      }
    })
  }

  return (
    <div>
      <Accordion elevation={0}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel1a-content" id="panel1a-header">
          <Typography variant="h6">Users</Typography>
        </AccordionSummary>
        <AccordionDetails sx={{ padding: '8px 0px' }}>
          {loadingGlobal === false && (
            <>
              {selectedUserList.length > 0 &&
                selectedUserList.map((user) => (
                  <div key={user.email}>
                    <p>{user.email}</p>
                    <DropdownList
                      className="org-form-dropdown"
                      dataKey="role"
                      textField="role"
                      data={availableRoles}
                      value={user}
                      onChange={(value) => {
                        dispatch(setSelectedUserRole({ email: user.email, role: value.role }))
                      }}
                    />
                  </div>
                ))}
              <AdminTable
                title=""
                data={props.tableData}
                columns={userColumns}
                actions={userActions}
                editable={userTableEditable}
              />
            </>
          )}
        </AccordionDetails>
      </Accordion>
    </div>
  )
}

export default AdminOrganizationTabUser
