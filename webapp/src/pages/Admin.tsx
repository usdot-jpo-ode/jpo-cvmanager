import React, { useEffect, useState } from 'react'
import AdminFormManager from '../components/AdminFormManager'
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
import { setRouteNotFound } from '../generalSlices/userSlice'

interface TabPanelProps {
  children?: React.ReactNode
}

function TabPanel(props: TabPanelProps) {
  const { children, ...other } = props

  return (
    <div role="tabpanel" id={`vertical-tabpanel`} aria-labelledby={`vertical-tab`} {...other}>
      <Box sx={{ p: 3 }}>
        <Typography>{children}</Typography>
      </Box>
    </div>
  )
}

function Admin() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const location = useLocation()
  const [value, setValue] = useState(location.pathname.split('/')[location.pathname.split('/').length - 1] || 'rsus')

  const handleChange = (_e, newValue) => {
    setValue(newValue)
  }

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  return (
    <div id="admin">
      <h2 className="adminHeader">CV Manager Admin Interface</h2>
      <Box
        sx={{
          flexGrow: 1,
          bgcolor: 'background',
          display: 'flex',
          width: '100%',
          height: '100%',
        }}
      >
        <Tabs
          value={value}
          onChange={handleChange}
          aria-label="Navigation"
          indicatorColor="primary"
          textColor="primary"
          orientation="vertical"
          sx={{ minWidth: 200 }}
        >
          <Tab label="RSUs" value={'rsus'} component={Link} to={'rsus'} />
          <Tab label="Users" value={'users'} component={Link} to={'users'} />
          <Tab label="Organizations" value={'organizations'} component={Link} to={'organizations'} />
        </Tabs>
        <TabPanel>
          <Routes>
            <Route index element={<Navigate to="rsus" replace />} />
            <Route path="rsus" element={<AdminRsuTab />} />
            <Route path="users" element={<AdminUserTab />} />
            <Route path="organizations" element={<AdminOrganizationTab />} />
            <Route path="*" element={() => dispatch(setRouteNotFound(true))} />
          </Routes>
        </TabPanel>
      </Box>
    </div>
  )
}

export default Admin
