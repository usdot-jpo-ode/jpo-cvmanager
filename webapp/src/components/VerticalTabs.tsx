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
        bgcolor: 'background.default',
        display: 'flex',
        width: '100%',
        ...(props.height !== undefined && { height: props.height }),
      }}
    >
      <Box
        sx={{
          bgcolor: 'background.paper',
        }}
      >
        <Tabs
          value={value}
          onChange={handleChange}
          aria-label="Navigation"
          indicatorColor="secondary"
          textColor="inherit"
          orientation="vertical"
          sx={{ width: 170 }}
          TabIndicatorProps={{
            style: {
              right: 'auto', // remove the default right positioning
              left: 0, // add left positioning
              width: 5, // width of the indicator
            },
          }}
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
                sx={{
                  backgroundColor: value === tab.path || value === index ? theme.palette.primary.main : 'transparent',
                  fontSize: 20,
                  height: '80px',
                  alignItems: 'flex-start',
                  textTransform: 'none',
                  borderRadius: 1,
                  '&&': {
                    color:
                      value === tab.path || value === index
                        ? theme.palette.primary.contrastText
                        : theme.palette.text.primary,
                    border:
                      value === tab.path || value === index
                        ? 'none'
                        : `0.5px solid ${alpha(theme.palette.divider, 0.2)}`,
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
