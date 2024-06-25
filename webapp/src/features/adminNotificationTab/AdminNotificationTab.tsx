import React, { useState, useEffect } from 'react'
import AdminTable from '../../components/AdminTable'
import { IoChevronBackCircleOutline, IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import { selectLoading } from '../../generalSlices/rsuSlice'
import {
  selectTableData,
  getUserNotifications,
  deleteNotifications,
  updateTitle,
  setActiveDiv,
  setEditNotificationRowData,
} from './adminNotificationTabSlice'
import { useSelector, useDispatch } from 'react-redux'

import '../adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action } from '@material-table/core'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import AdminEditNotification from '../adminEditNotification/AdminEditNotification'
import AdminAddNotification from '../adminAddNotification/AdminAddNotification'
import { AdminEmailNotification } from '../../types/Notifications'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager Email Notifications'
  } else if (activeTab === 'editNotification') {
    return 'Edit Email Notifications'
  } else if (activeTab === 'addNotification') {
    return 'Add Email Notifications'
  }
  return 'Unknown'
}

const AdminNotificationTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]
  const title = getTitle(activeTab)

  const tableData = useSelector(selectTableData)
  const [columns] = useState([
    { title: 'First Name', field: 'first_name', id: 0 },
    { title: 'Last Name', field: 'last_name', id: 1 },
    { title: 'Email', field: 'email', id: 2 },
    { title: 'Email Type', field: 'email_type', id: 3 },
  ])
  const loading = useSelector(selectLoading)

  let tableActions: Action<AdminEmailNotification>[] = [
    {
      icon: 'delete',
      tooltip: 'Delete Email Notification',
      position: 'row',
      onClick: (event, rowData: AdminEmailNotification) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => dispatch(deleteNotifications([rowData])),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options(
          'Delete Email Notification',
          'Are you sure you want to delete ' + rowData.email_type + ' for "' + rowData.email + '"?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      icon: 'edit',
      tooltip: 'Edit Notification',
      position: 'row',
      onClick: (event, rowData: AdminEmailNotification) => onEdit(rowData),
    },
    {
      tooltip: 'Remove All Selected Notifications',
      icon: 'delete',
      onClick: (event, rowData: AdminEmailNotification[]) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => dispatch(deleteNotifications(rowData)),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options(
          'Delete Selected Email Notifications',
          'Are you sure you want to delete ' + rowData.length + ' email notifications?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
  ]

  // dispatch
  const updateTableData = async () => {
    dispatch(getUserNotifications())
  }

  useEffect(() => {
    dispatch(setActiveDiv('notification_table'))
  }, [dispatch])

  useEffect(() => {
    dispatch(updateTitle())
  }, [activeTab, dispatch])

  const onEdit = (row: AdminEmailNotification) => {
    dispatch(setEditNotificationRowData(row))
    navigate('editNotification/' + row.email_type)
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeTab !== undefined && (
            <button key="notification_table" className="admin_table_button" onClick={() => navigate('.')}>
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeTab === undefined && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={() => navigate('addNotification')}
              title="Add Email Notification"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={() => updateTableData()}
              title="Refresh Email Notifications"
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
          path="addNotification"
          element={
            <div className="scroll-div-tab">
              <AdminAddNotification />
            </div>
          }
        />
        <Route
          path="editNotification/:email"
          element={
            <div className="scroll-div-tab">
              <AdminEditNotification />
            </div>
          }
        />
        <Route
          path="*"
          element={
            <NotFound
              redirectRoute="/dashboard/admin/notifications"
              redirectRouteName="Admin Notification Page"
              offsetHeight={319}
              description="This page does not exist. Please return to the admin Notification page."
            />
          }
        />
      </Routes>
    </div>
  )
}

export default AdminNotificationTab
