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
import { NotFoundRedirect } from './404'

interface TabLinkProps {
  label: string
  val: string
  link: string
}

function TabLink(props: TabLinkProps) {
  const { label, val, link, ...other } = props

  return (
    <Tab
      label={label}
      value={link}
      component={Link}
      to={link}
      sx={{ backgroundColor: val === link ? '#0e2052' : 'transparent', marginTop: 2 }}
      {...other}
    />
  )
}

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
    <div id="admin">
      <h2 className="adminHeader">CV Manager Admin Interface</h2>
      <Box
        sx={{
          flexGrow: 1,
          bgcolor: 'background.default',
          display: 'flex',
          width: '100%',
          height: '100%',
        }}
      >
        <Box
          sx={{
            // flexGrow: 1,
            bgcolor: 'background.paper',
            // flexDirection: 'row',
          }}
        >
          <Tabs
            value={value}
            onChange={handleChange}
            aria-label="Navigation"
            indicatorColor="primary"
            textColor="primary"
            orientation="vertical"
            sx={{ width: 200 }}
          >
            <Tab
              label={'RSUs'}
              value={'rsus'}
              component={Link}
              to={'rsus'}
              sx={{
                backgroundColor: value === 'rsus' || value === 0 ? '#0e2052' : 'transparent',
                fontSize: 17,
                height: '100px',
              }}
            />
            <Tab
              label={'Users'}
              value={'users'}
              component={Link}
              to={'users'}
              sx={{
                backgroundColor: value === 'users' || value === 1 ? '#0e2052' : 'transparent',
                fontSize: 17,
                height: '100px',
              }}
            />
            <Tab
              label={'Organizations'}
              value={'organizations'}
              component={Link}
              to={'organizations'}
              sx={{
                backgroundColor: value === 'organizations' || value === 2 ? '#0e2052' : 'transparent',
                fontSize: 17,
                height: '100px',
              }}
            />
          </Tabs>
        </Box>
        <TabPanel>
          <Routes>
            <Route index element={<Navigate to="rsus" replace />} />
            <Route path="rsus/*" element={<AdminRsuTab />} />
            <Route path="users/*" element={<AdminUserTab />} />
            <Route path="organizations/*" element={<AdminOrganizationTab />} />
            <Route path="*" element={<NotFoundRedirect />} />
          </Routes>
        </TabPanel>
      </Box>
    </div>
  )
}

export default Admin
