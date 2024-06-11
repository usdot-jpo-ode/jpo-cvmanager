import React, { useEffect, useState } from 'react'
import { useDispatch } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import '../features/adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { Box, Tab, Tabs, Typography } from '@mui/material'
import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import AdminOrganizationTab from '../features/adminOrganizationTab/AdminOrganizationTab'
import AdminRsuTab from '../features/adminRsuTab/AdminRsuTab'
import AdminUserTab from '../features/adminUserTab/AdminUserTab'
import { NotFound } from './404'
import { SecureStorageManager } from '../managers'
import VerticalTabs from '../components/VerticalTabs'

interface TabPanelProps {
  children?: React.ReactNode
}

function TabPanel(props: TabPanelProps) {
  const { children, ...other } = props

  return (
    <div role="tabpanel" id={`vertical-tabpanel`} aria-labelledby={`vertical-tab`} style={{ width: '100%' }} {...other}>
      <Box sx={{ p: 3 }}>
        <Typography>{children}</Typography>
      </Box>
    </div>
  )
}

function Admin() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const location = useLocation()

  const getSelectedTab = () => location.pathname.split('/')[3] || 'rsus'

  const [value, setValue] = useState<string | number>(getSelectedTab())

  const handleChange = (_e, newValue) => {
    console.log(value, newValue)
    setValue(newValue)
  }

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  return (
    <>
      {SecureStorageManager.getUserRole() !== 'admin' ? (
        <div id="admin">
          <NotFound description="You do not have permission to view this page. Please return to main dashboard: " />
        </div>
      ) : (
        <div id="admin">
          <h2 className="adminHeader">CV Manager Admin Interface</h2>
          <VerticalTabs
            notFoundRoute={
              <NotFound
                redirectRoute="/dashboard/admin"
                redirectRouteName="Admin Page"
                description="This page does not exist. Please return to the main admin page."
              />
            }
            defaultTabIndex={0}
            tabs={[
              {
                path: 'rsus',
                title: 'RSUs',
                child: <AdminRsuTab />,
              },
              {
                path: 'users',
                title: 'Users',
                child: <AdminUserTab />,
              },
              {
                path: 'organizations',
                title: 'Organizations',
                child: <AdminOrganizationTab />,
              },
            ]}
          />
        </div>
      )}
    </>
  )
}

export default Admin
