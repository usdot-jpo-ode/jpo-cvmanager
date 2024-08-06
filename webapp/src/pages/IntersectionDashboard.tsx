import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { updateTableData as updateRsuTableData } from '../features/adminRsuTab/adminRsuTabSlice'
import { getAvailableUsers } from '../features/adminUserTab/adminUserTabSlice'

import '../features/adminRsuTab/Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../store'
import { NotFound } from './404'
import VerticalTabs from '../components/VerticalTabs'
import DashboardPage from '../components/intersections/DashboardPage'
import NotificationPage from '../components/intersections/NotificationPage'
import AssessmentsPage from '../components/intersections/AssessmentsPage'
import DataSelectorPage from '../components/intersections/DataSelectorPage'
import ConfigurationPage from '../components/intersections/ConfigurationPage'
import ReportsPage from '../components/intersections/ReportsPage'
import DecoderPage from '../components/intersections/DecoderPage'
import { InputLabel, Select, MenuItem, IconButton, FormControl, Tooltip } from '@mui/material'
import { setOpenMapDialog } from '../features/intersections/data-selector/dataSelectorSlice'
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
  const [chosenIntersectionId, setChosenIntersectionId] = useState(-1)

  useEffect(() => {
    dispatch(updateRsuTableData())
    dispatch(getAvailableUsers())
  }, [dispatch])

  useEffect(() => {
    console.log('IntersectionDashboard useEffect', intersectionId)
  }, [intersectionId])

  return (
    <>
      <div id="admin">
        <h2 className="adminHeader">CV Manager Admin Interface</h2>
        <Tooltip title="Select Intersection">
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
                <MenuItem value={intersection.intersectionID}>{intersection.intersectionID}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Tooltip>
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
              path: 'assessments',
              title: 'Assessments',
              child: <AssessmentsPage />,
            },
            {
              path: 'data-selector',
              title: 'Data Selector',
              child: <DataSelectorPage />,
            },
            // The decoder page is still under development
            // {
            //   path: 'decoder',
            //   title: 'Decoder',
            //   child: <DecoderPage />,
            // },
            {
              path: 'reports',
              title: 'Reports',
              child: <ReportsPage />,
            },
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
        intersections={intersections.filter((v) => v?.intersectionID != undefined) as IntersectionReferenceData[]}
      />
    </>
  )
}

export default IntersectionDashboard
