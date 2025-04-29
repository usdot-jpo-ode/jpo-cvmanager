import React, { useState, useEffect } from 'react'
import AdminAddUser from '../adminAddUser/AdminAddUser'
import AdminEditUser from '../adminEditUser/AdminEditUser'
import AdminTable from '../../components/AdminTable'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import { selectLoading } from '../../generalSlices/rsuSlice'
import {
  selectTableData,

  // actions
  getAvailableUsers,
  deleteUsers,
  updateTitle,
  setActiveDiv,
  setEditUserRowData,
} from './adminUserTabSlice'
import { clear, getUserData } from './../adminEditUser/adminEditUserSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action } from '@material-table/core'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { AddCircleOutline, DeleteOutline, ModeEditOutline, Refresh } from '@mui/icons-material'
import { Button } from '@mui/material'

const AdminUserTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]

  const tableData = useSelector(selectTableData)
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
  ])
  const loading = useSelector(selectLoading)

  let tableActions: Action<AdminUserWithId>[] = [
    {
      icon: () => <ModeEditOutline />,
      tooltip: 'Edit User',
      position: 'row',
      onClick: (event, rowData: AdminUserWithId) => onEdit(rowData),
    },
    {
      icon: () => <DeleteOutline />,
      tooltip: 'Delete User',
      position: 'row',
      onClick: (event, rowData: AdminUserWithId) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => handleDelete([rowData]),
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
      tooltip: 'Remove All Selected Users',
      icon: 'delete',
      position: 'toolbarOnSelect',
      onClick: (event, rowData: AdminUserWithId[]) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => handleDelete(rowData),
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
    {
      tooltip: 'Refresh Data',
      icon: () => (
        <Button variant="outlined" color="info" startIcon={<Refresh />}>
          Refresh
        </Button>
      ),
      position: 'toolbar',
      onClick: () => {
        updateTableData()
      },
    },
    {
      tooltip: 'Add New User',
      icon: () => (
        <Button variant="contained" startIcon={<AddCircleOutline />}>
          New
        </Button>
      ),
      position: 'toolbar',
      onClick: () => {
        navigate('addUser')
      },
    },
  ]

  const handleDelete = (rowData: AdminUserWithId[]) => {
    dispatch(deleteUsers(rowData)).then((data: any) => {
      data.payload.success ? toast.success('User(s) Deleted Successfully') : toast.error(data.message.payload)
    })
  }

  const updateTableData = async () => {
    dispatch(getAvailableUsers())
  }

  useEffect(() => {
    dispatch(setActiveDiv('user_table'))
  }, [dispatch])

  useEffect(() => {
    dispatch(updateTitle())
  }, [activeTab, dispatch])

  const onEdit = (row: AdminUserWithId) => {
    // Fetch user info before navigating to ensure updated menu state
    dispatch(clear())
    dispatch(getUserData(row.email))

    dispatch(setEditUserRowData(row))
    navigate('editUser/' + row.email)
  }

  return (
    <div>
      <Routes>
        <Route
          path="/"
          element={
            loading === false && (
              <div className="scroll-div-tab">
                <AdminTable title={''} data={tableData} columns={columns} actions={tableActions} />
              </div>
            )
          }
        />
        <Route
          path="addUser"
          element={
            <div className="scroll-div-tab">
              <AdminAddUser />
            </div>
          }
        />
        <Route
          path="editUser/:email"
          element={
            <div className="scroll-div-tab">
              <AdminEditUser />
            </div>
          }
        />
        <Route
          path="*"
          element={
            <NotFound
              redirectRoute="/dashboard/admin/users"
              redirectRouteName="Admin User Page"
              offsetHeight={319}
              description="This page does not exist. Please return to the admin User page."
            />
          }
        />
      </Routes>
    </div>
  )
}

export default AdminUserTab
