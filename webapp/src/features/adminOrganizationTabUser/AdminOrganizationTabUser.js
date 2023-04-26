import React, { useState, useEffect } from 'react'
import AdminTable from '../../components/AdminTable'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import { ThemeProvider, createTheme } from '@mui/material'
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
  userBulkEdit,
  setSelectedUserRole,
  setSelectedUserList,
} from './adminOrganizationTabUserSlice'
import { selectLoadingGlobal } from '../../generalSlices/userSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'

const AdminOrganizationTabUser = (props) => {
  const dispatch = useDispatch()
  const { selectedOrg } = props
  const availableUserList = useSelector(selectAvailableUserList)
  const selectedUserList = useSelector(selectSelectedUserList)
  const availableRoles = useSelector(selectAvailableRoles)
  const loadingGlobal = useSelector(selectLoadingGlobal)
  const [userColumns] = useState([
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

  let userActions = [
    {
      icon: 'delete',
      tooltip: 'Remove From Organization',
      position: 'row',
      onClick: (event, rowData) => {
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
      onClick: (event, rowData) => {
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
    onBulkUpdate: (changes) =>
      new Promise((resolve, reject) => {
        userBulkEdit(changes)
        setTimeout(() => {
          resolve()
        }, 2000)
      }),
  }

  useEffect(() => {
    dispatch(getAvailableRoles())
  }, [])

  useEffect(() => {
    dispatch(setSelectedUserList([]))
    dispatch(getAvailableUsers(selectedOrg))
  }, [selectedOrg])

  const userOnDelete = async (row) => {
    dispatch(
      userDeleteSingle({
        user: row,
        orgPatchJson: props.orgPatchJson,
        selectedOrg: props.selectedOrg,
        fetchPatchOrganization: props.fetchPatchOrganization,
        updateTableData: props.updateTableData,
      })
    )
  }

  const userMultiDelete = async (rows) => {
    dispatch(
      userDeleteMultiple({
        users: rows,
        orgPatchJson: props.orgPatchJson,
        selectedOrg: props.selectedOrg,
        fetchPatchOrganization: props.fetchPatchOrganization,
        updateTableData: props.updateTableData,
      })
    )
  }

  const userMultiAdd = async (userList) => {
    dispatch(
      userAddMultiple({
        userList,
        orgPatchJson: props.orgPatchJson,
        selectedOrg: props.selectedOrg,
        fetchPatchOrganization: props.fetchPatchOrganization,
        updateTableData: props.updateTableData,
      })
    )
  }

  const userBulkEdit = async (json) => {
    dispatch(
      userBulkEdit({
        json,
        orgPatchJson: props.orgPatchJson,
        selectedOrg: props.selectedOrg,
        fetchPatchOrganization: props.fetchPatchOrganization,
        updateTableData: props.updateTableData,
      })
    )
  }

  const accordionTheme = createTheme({
    palette: {
      text: {
        primary: '#ffffff',
        secondary: '#ffffff',
        disabled: '#ffffff',
        hint: '#ffffff',
      },
      divider: '#333',
      background: {
        paper: '#0e2052',
      },
    },
  })

  const innerAccordionTheme = createTheme({
    palette: {
      text: {
        primary: '#fff',
        secondary: '#fff',
        disabled: '#fff',
        hint: '#fff',
      },
      divider: '#333',
      background: {
        paper: '#333',
      },
    },
  })

  return (
    <div>
      <ThemeProvider theme={accordionTheme}>
        <Accordion>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon className="expand" />}
            aria-controls="panel1a-content"
            id="panel1a-header"
          >
            <Typography style={{ fontSize: '18px' }}>{props.selectedOrg} Users</Typography>
          </AccordionSummary>
          <AccordionDetails>
            {loadingGlobal === false && [
              <div className="accordion" key="accordion">
                <ThemeProvider theme={innerAccordionTheme}>
                  <Accordion>
                    <AccordionSummary
                      expandIcon={<ExpandMoreIcon className="expand" />}
                      aria-controls="panel1a-content"
                      id="panel1a-header"
                    >
                      <Typography>Add Users to {props.selectedOrg}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <div className="spacer-large-user">
                        <Multiselect
                          className="org-multiselect"
                          dataKey="id"
                          textField="email"
                          placeholder="Click to add users"
                          data={availableUserList}
                          value={selectedUserList}
                          onChange={(value) => dispatch(setSelectedUserList(value))}
                        />
                        <button
                          key="user_plus_button"
                          className="admin-button"
                          onClick={() => userMultiAdd(selectedUserList)}
                          title="Add Users To Organization"
                        >
                          <AiOutlinePlusCircle size={20} />
                        </button>
                      </div>
                      {selectedUserList.length > 0 && (
                        <p className="org-form-test">
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
                    </AccordionDetails>
                  </Accordion>
                </ThemeProvider>
              </div>,
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
      </ThemeProvider>
    </div>
  )
}

export default AdminOrganizationTabUser
