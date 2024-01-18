import React, { useState, useEffect } from 'react'
import AdminAddUser from '../adminAdduser/AdminAddUser'
import AdminEditUser from '../adminEditUser/AdminEditUser'
import AdminTable from '../../components/AdminTable'
import { IoChevronBackCircleOutline, IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import { selectLoading } from '../../generalSlices/rsuSlice'
import {
  selectActiveDiv,
  selectTableData,
  selectTitle,
  selectEditUserRowData,

  // actions
  getAvailableUsers,
  deleteUsers,
  updateTitle,
  setActiveDiv,
  setEditUserRowData,
} from './adminUserTabSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action } from '@material-table/core'

const AdminUserTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const activeDiv = useSelector(selectActiveDiv)
  const tableData = useSelector(selectTableData)
  const title = useSelector(selectTitle)
  const editUserRowData = useSelector(selectEditUserRowData)
  const [columns] = useState([
    { title: 'First Name', field: 'first_name', id: 0 },
    { title: 'Last Name', field: 'last_name', id: 1 },
    { title: 'Email', field: 'email', id: 2 },
    {
      title: 'Super User',
      field: 'super_user',
      id: 3,
      render: (rowData: AdminUserWithId) => (rowData.super_user ? 'Yes' : 'No'),
    },
    {
      title: 'Rcv Err Emails',
      field: 'receive_error_emails',
      id: 3,
      render: (rowData) => (rowData.receive_error_emails ? 'Yes' : 'No'),
    },
  ])
  const loading = useSelector(selectLoading)

  let tableActions: Action<AdminUserWithId>[] = [
    {
      icon: 'delete',
      tooltip: 'Delete User',
      position: 'row',
      onClick: (event, rowData: AdminUserWithId) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => dispatch(deleteUsers([rowData])),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options('Delete User', 'Are you sure you want to delete "' + rowData.email + '"?', buttons)
        confirmAlert(alertOptions)
      },
    },
    {
      icon: 'edit',
      tooltip: 'Edit User',
      position: 'row',
      onClick: (event, rowData: AdminUserWithId) => onEdit(rowData),
    },
    {
      tooltip: 'Remove All Selected Users',
      icon: 'delete',
      onClick: (event, rowData: AdminUserWithId[]) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => dispatch(deleteUsers(rowData)),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options(
          'Delete Selected Users',
          'Are you sure you want to delete ' + rowData.length + ' users?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
  ]

  const updateTableData = async () => {
    dispatch(getAvailableUsers())
  }

  useEffect(() => {
    dispatch(setActiveDiv('user_table'))
  }, [dispatch])

  useEffect(() => {
    dispatch(updateTitle())
  }, [activeDiv, dispatch])

  const onEdit = (row: AdminUserWithId) => {
    dispatch(setEditUserRowData(row))
    dispatch(setActiveDiv('edit_user'))
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeDiv !== 'user_table' && (
            <button
              key="user_table"
              className="admin_table_button"
              onClick={() => dispatch(setActiveDiv('user_table'))}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeDiv === 'user_table' && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={() => dispatch(setActiveDiv('add_user'))}
              title="Add User"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={() => updateTableData()}
              title="Refresh Users"
            >
              <IoRefresh size={20} />
            </button>,
          ]}
        </h3>
      </div>
      {activeDiv === 'user_table' && loading === false && (
        <div className="scroll-div-tab">
          <AdminTable title={''} data={tableData} columns={columns} actions={tableActions} />
        </div>
      )}

      {activeDiv === 'add_user' && (
        <div className="scroll-div-tab">
          <AdminAddUser />
        </div>
      )}

      {activeDiv === 'edit_user' && (
        <div className="scroll-div-tab">
          <AdminEditUser userData={editUserRowData} updateUserData={updateTableData} />
        </div>
      )}
    </div>
  )
}

export default AdminUserTab
