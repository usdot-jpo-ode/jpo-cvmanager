import React, { useState, useEffect } from 'react'
import AdminAddUser from '../adminAddUser/AdminAddUser'
import AdminEditUser from '../adminEditUser/AdminEditUser'
import AdminTable from '../../components/AdminTable'
import { IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
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

import '../adminRsuTab/Admin.css'
import { Action } from '@material-table/core'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { useAppDispatch, useAppSelector } from '../../hooks'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager Users'
  } else if (activeTab === 'editUser') {
    return ''
  } else if (activeTab === 'addUser') {
    return ''
  }
  return 'Unknown'
}

const AdminUserTab = () => {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]
  const title = getTitle(activeTab)

  const tableData = useAppSelector(selectTableData)
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
  const loading = useAppSelector(selectLoading)

  let tableActions: Action<AdminUserWithId>[] = [
    {
      icon: 'delete',
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
    dispatch(setEditUserRowData(row))
    navigate('editUser/' + row.email)
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {title}
          {activeTab === undefined && [
            <button key="plus_button" className="plus_button" onClick={() => navigate('addUser')} title="Add User">
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
