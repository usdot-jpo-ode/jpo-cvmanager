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
import { NotFoundRedirect, AdminNotFoundRedirect } from './404'
import { SecureStorageManager } from '../managers'

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
        <Routes>
          <Route path="*" element={<AdminNotFoundRedirect />} />
        </Routes>
      ) : (
        <div id="admin">
          <h2 className="adminHeader">CV Manager Admin Interface</h2>
          <Box
            sx={{
              flexGrow: 1,
              bgcolor: 'background.default',
              display: 'flex',
              width: '100%',
              height: 'calc(100% - 70px)',
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
                textColor="inherit"
                orientation="vertical"
                sx={{ width: 170 }}
                TabIndicatorProps={{
                  style: {
                    right: 'auto', // remove the default right positioning
                    left: 0, // add left positioning
                    width: 4, // width of the indicator
                  },
                }}
              >
                <Tab
                  label={'RSUs'}
                  value={'rsus'}
                  component={Link}
                  to={'rsus'}
                  sx={{
                    backgroundColor: value === 'rsus' || value === 0 ? '#0e2052' : 'transparent',
                    fontSize: 20,
                    height: '80px',
                    alignItems: 'flex-start', // left-align text
                    textTransform: 'none', // no capitalization
                    '&&': { color: value === 'rsus' || value === 0 ? '#fff' : '#bbb' }, // set color when deselected
                  }}
                />
                <Tab
                  label={'Users'}
                  value={'users'}
                  component={Link}
                  to={'users'}
                  sx={{
                    backgroundColor: value === 'users' || value === 1 ? '#0e2052' : 'transparent',
                    fontSize: 20,
                    height: '80px',
                    alignItems: 'flex-start', // left-align text
                    textTransform: 'none', // no capitalization
                    '&&': { color: value === 'users' || value === 1 ? '#fff' : '#bbb' }, // set color when deselected
                  }}
                />
                <Tab
                  label={'Organizations'}
                  value={'organizations'}
                  component={Link}
                  to={'organizations'}
                  sx={{
                    backgroundColor: value === 'organizations' || value === 2 ? '#0e2052' : 'transparent',
                    fontSize: 20,
                    height: '80px',
                    alignItems: 'flex-start', // left-align text
                    textTransform: 'none', // no capitalization
                    '&&': { color: value === 'organizations' || value === 2 ? '#fff' : '#bbb' }, // set color when deselected
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
      )}
    </>
  )
}

export default Admin
