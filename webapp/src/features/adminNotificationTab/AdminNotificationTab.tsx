import React, { useState, useEffect } from 'react'
import AdminTable from '../../components/AdminTable'
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
import { AdminEmailNotification } from '../../models/Notifications'
import { headerTabHeight } from '../../styles/index'
import { Button, useTheme } from '@mui/material'
import { AddCircleOutline, DeleteOutline, ModeEditOutline, Refresh } from '@mui/icons-material'

const AdminNotificationTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = useTheme()

  const activeTab = location.pathname.split('/')[3]

  const tableData = useSelector(selectTableData)
  const [columns] = useState([{ title: 'Email Notification Type', field: 'email_type', id: 3 }])
  const loading = useSelector(selectLoading)

  let tableActions: Action<AdminEmailNotification>[] = [
    {
      icon: () => <ModeEditOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      iconProps: {
        itemType: 'rowAction',
      },
      position: 'row',
      onClick: (event, rowData: AdminEmailNotification) => onEdit(rowData),
    },
    {
      icon: () => <DeleteOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      iconProps: {
        itemType: 'rowAction',
      },
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
      tooltip: 'Remove All Selected Notifications',
      icon: 'delete',
      position: 'toolbarOnSelect',
      iconProps: {
        itemType: 'rowAction',
      },
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
    {
      icon: () => null,
      iconProps: {
        title: 'Refresh',
        color: 'info',
        itemType: 'outlined',
      },
      position: 'toolbar',
      onClick: () => {
        updateTableData()
      },
    },
    {
      icon: () => null,
      position: 'toolbar',
      iconProps: {
        title: 'New',
        color: 'primary',
        itemType: 'contained',
      },
      onClick: () => {
        navigate('addNotification')
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
    width: '95%',
    overflow: 'auto',
    height: `calc(100vh - ${headerTabHeight + 76 + 59}px)`, // 76 = page header height, 59 = button div height
    marginTop: '25px',
    backgroundColor: theme.palette.background.default,
  }

  const notificationWrapperStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    width: '100%',
  }

  return (
    <div style={{ height: `calc(100vh - ${headerTabHeight}px)`, backgroundColor: theme.palette.background.default }}>
      <Routes>
        <Route
          path="/"
          element={
            loading === false && (
              <div style={notificationWrapperStyle}>
                <div style={notificationStyle}>
                  <AdminTable title={''} data={tableData} columns={columns} actions={tableActions} />
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
