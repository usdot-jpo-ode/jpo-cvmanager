import React, { useEffect, useState } from 'react'
import { useDispatch } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import '../features/adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { alpha, Box, Tab, Tabs, useTheme } from '@mui/material'
import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { evaluateFeatureFlags } from '../feature-flags'
import { CellTowerOutlined, GroupOutlined, TrafficOutlined, WorkspacesOutlined } from '@mui/icons-material'

interface TabPanelProps {
  children?: React.ReactNode
}

function TabPanel(props: TabPanelProps) {
  const { children, ...other } = props

  return (
    <div
      role="tabpanel"
      id={`vertical-tabpanel`}
      aria-labelledby={`vertical-tab`}
      style={{ width: '100%', overflowY: 'auto' }}
      {...other}
    >
      <Box sx={{ p: 3 }}>{children}</Box>
    </div>
  )
}

interface VerticalTabItem {
  path: string
  title: string
  adminRequired?: boolean
  child: React.ReactNode
  tag?: FEATURE_KEY
  icon?: React.ReactNode
}

interface VerticalTabProps {
  notFoundRoute: React.ReactNode
  defaultTabIndex?: number
  tabs: VerticalTabItem[]
  height?: string
}

function VerticalTabs(props: VerticalTabProps) {
  const { notFoundRoute, defaultTabIndex, tabs } = props
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()
  const location = useLocation()
  const filteredTabs = tabs.filter((tab) => evaluateFeatureFlags(tab.tag))
  const defaultTabKey = filteredTabs[defaultTabIndex ?? 0]?.path

  const getIcon = (tabName: string) => {
    switch (tabName) {
      case 'RSUs':
        return <CellTowerOutlined />
      case 'Intersections':
        return <TrafficOutlined />
      case 'Users':
        return <GroupOutlined />
      case 'Organizations':
        return <WorkspacesOutlined />
    }
  }

  const getSelectedTab = () => location.pathname.split('/').at(-1) || defaultTabKey

  const [value, setValue] = useState<string | number>(getSelectedTab())

  useEffect(() => {
    setValue(getSelectedTab())
  }, [location.pathname])

  const handleChange = (_e, newValue) => {
    setValue(newValue)
  }

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  return (
    <Box
      sx={{
        flexGrow: 1,
        bgcolor: theme.palette.background.default,
        display: 'flex',
        width: '100%',
        ...(props.height !== undefined && { height: props.height }),
      }}
    >
      <Box
        sx={{
          bgcolor: theme.palette.background.paper,
        }}
      >
        <Tabs
          value={value}
          onChange={handleChange}
          aria-label="Navigation"
          indicatorColor="secondary"
          textColor="inherit"
          orientation="vertical"
          sx={{ width: 250, '& .MuiTabs-indicator': { display: 'none' } }}
        >
          {filteredTabs.map((tab) => {
            const index = filteredTabs.indexOf(tab)
            return (
              <Tab
                label={tab.title}
                key={tab.path}
                value={tab.path}
                component={Link}
                to={tab.path}
                icon={getIcon(tab.title)}
                iconPosition="start"
                className="capital-case"
                sx={{
                  display: 'flex',
                  flexDirection: 'row',
                  justifyContent: 'flex-start',
                  alignItems: 'center',
                  fontSize: '16px',
                  minHeight: '48px',
                  fontWeight: value === tab.path || value === index ? 'bold' : 'normal',
                  color:
                    value === tab.path || value === index ? theme.palette.text.primary : theme.palette.text.secondary,
                  padding: '8px 16px',
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.text.primary, 0.1),
                  },
                }}
              />
            )
          })}
        </Tabs>
      </Box>
      <TabPanel>
        <Routes>
          <Route index element={<Navigate to={filteredTabs[defaultTabIndex ?? 0]?.path} replace />} />
          {filteredTabs.map((tab) => (
            <Route key={tab.path} path={`${tab.path}/*`} element={tab.child} />
          ))}
          <Route path="*" element={notFoundRoute} />
        </Routes>
      </TabPanel>
    </Box>
  )
}

export default VerticalTabs
