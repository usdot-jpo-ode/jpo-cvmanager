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

import '../adminRsuTab/Admin.css'
import { Action } from '@material-table/core'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import AdminEditNotification from '../adminEditNotification/AdminEditNotification'
import AdminAddNotification from '../adminAddNotification/AdminAddNotification'
import { AdminEmailNotification } from '../../models/Notifications'
import { selectEmail } from '../../generalSlices/userSlice'
import { useAppDispatch, useAppSelector } from '../../hooks'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager Email Notifications'
  } else if (activeTab === 'editNotification') {
    return 'Edit Email Notification'
  } else if (activeTab === 'addNotification') {
    return 'Add Email Notification'
  }
  return 'Unknown'
}

const AdminNotificationTab = () => {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[3]
  const title = getTitle(activeTab)

  const userEmail = useAppSelector(selectEmail)

  const tableData = useAppSelector(selectTableData)
  const [columns] = useState([{ title: 'Email Notification Type', field: 'email_type', id: 3 }])
  const loading = useAppSelector(selectLoading)

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
          'Are you sure you want to unsubscribe from ' + rowData.email_type + ' notifications?"',
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

  // load data on first render
  useEffect(() => {
    dispatch(getUserNotifications())
  }, [])

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

  const notificationStyle = {
    width: '80%',
    height: 'calc(100vh - 100px)',
    fontFamily: 'Arial, Helvetica, sans-serif',
    overflow: 'auto',
  }

  const notificationWrapperStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgb(28, 29, 31)',
    width: '100%',
  }

  const panelHeaderNotificationStyle = {
    marginTop: '10px',
    padding: '5px',
    fontFamily: 'sans-serif',
    fontSize: '25px',
    backgroundColor: 'rgb(28, 29, 31)',
  }

  console.log('active tab: ', activeTab)

  return (
    <div style={{ backgroundColor: 'rgb(28, 29, 31)' }}>
      <div>
        <div>
          <h2 className="adminHeader" style={{ backgroundColor: 'rgb(51, 51, 51)' }}>
            {title}
          </h2>
        </div>
        <div style={panelHeaderNotificationStyle}>
          {activeTab !== undefined && (
            <button
              key="notification_table"
              className="admin_table_button"
              style={{ marginLeft: '10px' }}
              onClick={() => navigate('.')}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          <div />
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
        </div>
      </div>
      <Routes>
        <Route
          path="/"
          element={
            loading === false && (
              <div style={notificationWrapperStyle}>
                <div style={notificationStyle}>
                  <AdminTable
                    title={userEmail + ' Email Notifications'}
                    data={tableData}
                    columns={columns}
                    actions={tableActions}
                  />
                </div>
              </div>
            )
          }
        />
        <Route
          path="addNotification"
          element={
            <div style={notificationWrapperStyle}>
              <div style={notificationStyle}>
                <AdminAddNotification />
              </div>
            </div>
          }
        />
        <Route
          path="editNotification/:email"
          element={
            <div style={notificationWrapperStyle}>
              <div style={notificationStyle}>
                <AdminEditNotification />
              </div>
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
