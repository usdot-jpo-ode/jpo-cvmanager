import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import './css/IntersectionDashboard.css'
import './css/NoTableWidth.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { NotFound } from './404'
import VerticalTabs from '../components/VerticalTabs'
import DashboardPage from '../components/intersections/DashboardPage'
import NotificationPage from '../components/intersections/NotificationPage'
import DataSelectorPage from '../components/intersections/DataSelectorPage'
import ReportsPage from '../components/intersections/ReportsPage'
import { InputLabel, Select, MenuItem, IconButton, FormControl, Tooltip } from '@mui/material'
import {
  selectIntersections,
  selectSelectedIntersectionId,
  setSelectedIntersection,
} from '../generalSlices/intersectionSlice'
import MapIconRounded from '@mui/icons-material/Map'
import MapDialog from '../features/intersections/intersection-selector/intersection-selector-dialog'

function IntersectionDashboard() {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const intersections = useSelector(selectIntersections)
  const [openMapDialog, setOpenMapDialog] = useState(false)

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  return (
    <>
      <div id="admin">
        <h2 className="adminHeader">CV Manager Admin Interface</h2>
        <FormControl sx={{ mt: 1, minWidth: 200 }}>
          <InputLabel>Intersection ID</InputLabel>
          <Select
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
        <Tooltip title="Select Intersection on Map">
          <IconButton
            onClick={() => {
              setOpenMapDialog(true)
            }}
          >
            <MapIconRounded fontSize="large" />
          </IconButton>
        </Tooltip>
        <VerticalTabs
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
