import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import './css/NoTableWidth.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { NotFound } from './404'
import VerticalTabs from '../components/VerticalTabs'
import DashboardPage from '../components/intersections/DashboardPage'
import NotificationPage from '../components/intersections/NotificationPage'
import DataSelectorPage from '../components/intersections/DataSelectorPage'
import ReportsPage from '../components/intersections/ReportsPage'
import { InputLabel, Select, MenuItem, FormControl, Tooltip, Button, Box, useTheme, Grid2 } from '@mui/material'
import {
  selectIntersections,
  selectSelectedIntersectionId,
  setSelectedIntersection,
} from '../generalSlices/intersectionSlice'
import MapDialog from '../features/intersections/intersection-selector/intersection-selector-dialog'
import { headerTabHeight } from '../styles/index'
import { TrafficOutlined } from '@mui/icons-material'

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
            width: `calc(100vw - (300px + ${theme.spacing(3)}))`,
            position: 'absolute',
            top: `calc(${headerTabHeight}px + ${theme.spacing(3)})`,
            right: '50px',
            backgroundColor: theme.palette.background.default,
            justifyContent: 'flex-start',
            display: 'flex',
          }}
        >
          <FormControl
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
                startIcon={<TrafficOutlined fontSize="medium" />}
                sx={{
                  ml: 2,
                  height: '4em',
                }}
              >
                Select Intersection
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
              path: 'dashboard',
              title: 'Dashboard',
              child: <DashboardPage />,
            },
            {
              path: 'notifications',
              title: 'Notifications',
              child: <NotificationPage />,
            },
            {
              path: 'data-selector',
              title: 'Data Selector',
              child: <DataSelectorPage />,
            },
            {
              path: 'reports',
              title: 'Reports',
              child: <ReportsPage />,
            },
            // The configuration page is still under development
            // {
            //   path: 'configuration',
            //   title: 'Configuration',
            //   child: <ConfigurationPage />,
            // },
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
