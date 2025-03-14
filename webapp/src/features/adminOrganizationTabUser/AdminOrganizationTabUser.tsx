import React, { useState, useEffect } from 'react'
import AdminTable from '../../components/AdminTable'
import { AiOutlinePlusCircle } from 'react-icons/ai'
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
import { RootState } from '../../store'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { Action, Column } from '@material-table/core'
import { AdminOrgUser } from '../adminOrganizationTab/adminOrganizationTabSlice'
import toast from 'react-hot-toast'

import { ContainedIconButton } from '../../styles/components/ContainedIconButton'
import { Divider } from '@mui/material'

interface AdminOrganizationTabUserProps {
  selectedOrg: string
  selectedOrgEmail: string
  tableData: AdminOrgUser[]
  updateTableData: (org: string) => void
}

const AdminOrganizationTabUser = (props: AdminOrganizationTabUserProps) => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
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
      width: '23%',
    },
    {
      title: 'Last Name',
      field: 'last_name',
      editable: 'never',
      id: 1,
      width: '23%',
    },
    { title: 'Email', field: 'email', editable: 'never', id: 2, width: '24%' },
    {
      title: 'Role',
      field: 'role',
      id: 3,
      width: '23%',
      lookup: { user: 'User', operator: 'Operator', admin: 'Admin' },
    },
  ])

  let userActions: Action<AdminOrgUser>[] = [
    {
      icon: 'delete',
      tooltip: 'Remove From Organization',
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
      <Accordion>
        <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel1a-content" id="panel1a-header">
          <Typography variant="h6">{props.selectedOrg} Users</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {loadingGlobal === false && [
            <div key="accordion" style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex' }}>
                <Multiselect
                  className="org-multiselect"
                  dataKey="id"
                  textField="email"
                  placeholder="Click to add users"
                  data={availableUserList}
                  value={selectedUserList}
                  onChange={(value) => dispatch(setSelectedUserList(value))}
                />
                <ContainedIconButton
                  key="user_plus_button"
                  onClick={() => userMultiAdd(selectedUserList)}
                  title="Add Users To Organization"
                >
                  <AiOutlinePlusCircle size={20} />
                </ContainedIconButton>
              </div>
              {selectedUserList.length > 0 && (
                <p style={{ marginBottom: 10 }}>
                  <b>Please select a role for:</b>
                </p>
              )}
              {selectedUserList.length > 0 && [
                selectedUserList.map((user) => {
                  return (
                    <div>
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
                  )
                }),
              ]}
            </div>,
            <Divider />,
            <div key="adminTable">
              <AdminTable
                title={'Modify User-Organization Assignment'}
                data={props.tableData}
                columns={userColumns}
                actions={userActions}
                editable={userTableEditable}
              />
            </div>,
          ]}
        </AccordionDetails>
      </Accordion>
    </div>
  )
}

export default AdminOrganizationTabUser
