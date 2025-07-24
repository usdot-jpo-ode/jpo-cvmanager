import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import './css/NoTableWidth.css'
import { NotFound } from './404'
import VerticalTabs from '../components/VerticalTabs'
import DashboardPage from '../components/intersections/DashboardPage'
import NotificationPage from '../components/intersections/NotificationPage'
import DataSelectorPage from '../components/intersections/DataSelectorPage'
import ReportsPage from '../components/intersections/ReportsPage'
import {
  InputLabel,
  Select,
  MenuItem,
  FormControl,
  Tooltip,
  Button,
  useTheme,
  Grid2,
  Box,
  Typography,
} from '@mui/material'
import {
  selectIntersections,
  selectSelectedIntersectionId,
  setSelectedIntersection,
} from '../generalSlices/intersectionSlice'
import MapDialog from '../features/intersections/intersection-selector/intersection-selector-dialog'
import ConfigurationPage from '../components/intersections/ConfigurationPage'
import { headerTabHeight } from '../styles/index'
import { TrafficOutlined } from '@mui/icons-material'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'

function IntersectionDashboard() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const intersections = useSelector(selectIntersections)
  const [openMapDialog, setOpenMapDialog] = useState(false)
  const theme = useTheme()

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  return (
    <>
      <div id="admin" style={{ height: `calc(100vh - ${headerTabHeight}px)` }}>
        <div
          style={{
            width: `fit-content`,
            position: 'absolute',
            left: `calc(226px + ${theme.spacing(3)})`,
            backgroundColor: theme.palette.background.default,
            justifyContent: 'flex-start',
            display: 'flex',
            zIndex: 100,
            padding: '15px',
            paddingBottom: '17px',
            paddingLeft: '24px',
            borderRadius: '0 0 10px 0', // Only round the bottom-right corner
          }}
        >
          <FormControl
            size="small"
            sx={{
              mt: 1,
              width: 200,
              minWidth: 100,
            }}
          >
            <InputLabel htmlFor="intersection-select">Intersection ID</InputLabel>
            <Select
              labelId="intersection-select"
              label="Intersection ID"
              value={intersectionId}
              onChange={(e) => {
                dispatch(setSelectedIntersection(e.target.value as number))
              }}
            >
              {/* TODO: Update to display intersection Name */}
              {intersections.map((intersection) => (
                <MenuItem value={intersection.intersectionID} key={intersection.intersectionID}>
                  {intersection.intersectionID}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Grid2 sx={{ display: 'flex', alignItems: 'flex-end' }}>
            <Tooltip title="Select Intersection on Map">
              <Button
                variant="contained"
                onClick={() => {
                  setOpenMapDialog(true)
                }}
                startIcon={<TrafficOutlined />}
                sx={{
                  ml: 2,
                  height: '40px',
                }}
                className="museo-slab capital-case"
              >
                <Typography fontSize="16px">Select Intersection</Typography>
              </Button>
            </Tooltip>
          </Grid2>
        </div>
        <VerticalTabs
          height={`calc(100vh - ${headerTabHeight}px)`}
          notFoundRoute={
            <NotFound
              redirectRoute="/dashboard/intersection"
              redirectRouteName="Intersection Dashboard Page"
              description="This page does not exist. Please return to the main admin page."
            />
          }
          defaultTabIndex={0}
          tabs={[
            {
              path: '', // Default path, prevent errors
              title: 'Dashboard',
              child: <DashboardPage />,
            },
            {
              path: 'dashboard',
              title: 'Dashboard',
              child: <DashboardPage />,
            },
            {
              path: 'notifications',
              title: 'Notifications',
              child: (
                <Box sx={{ paddingTop: theme.spacing(5) }}>
                  <NotificationPage />
                </Box>
              ),
            },
            {
              path: 'data-selector',
              title: 'Data Selector',
              child: <DataSelectorPage />,
            },
            {
              path: 'reports',
              title: 'Reports',
              child: (
                <Box sx={{ paddingTop: theme.spacing(8) }}>
                  <ReportsPage />
                </Box>
              ),
            },
            {
              path: 'configuration',
              title: 'Configuration',
              child: (
                <Box sx={{ paddingTop: theme.spacing(5) }}>
                  <ConfigurationPage />
                </Box>
              ),
            },
          ]}
        />
      </div>
      <MapDialog
        open={openMapDialog}
        onClose={() => {
          setOpenMapDialog(false)
        }}
      />
    </>
  )
}

export default IntersectionDashboard
