import React from 'react'
import AdminAddIntersection from '../adminAddIntersection/AdminAddIntersection'
import AdminEditIntersection, { AdminEditIntersectionFormType } from '../adminEditIntersection/AdminEditIntersection'
import AdminTable from '../../components/AdminTable'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import {
  selectLoading,
  selectTableData,

  // actions
  updateTableData,
  deleteMultipleIntersections,
  deleteIntersection,
  setEditIntersectionRowData,
  selectColumns,
} from './adminIntersectionTabSlice'
import { clear, getIntersectionInfo } from '../adminEditIntersection/adminEditIntersectionSlice'
import { useSelector, useDispatch } from 'react-redux'

import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action } from '@material-table/core'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { Button } from '@mui/material'
import { AddCircleOutline, DeleteOutline, ModeEditOutline, Refresh } from '@mui/icons-material'

const AdminIntersectionTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()

  const tableData = useSelector(selectTableData)
  const columns = useSelector(selectColumns)

  const loading = useSelector(selectLoading)

  const tableActions: Action<AdminEditIntersectionFormType>[] = [
    {
      icon: () => <ModeEditOutline />,
      tooltip: 'Edit Intersection',
      position: 'row',
      onClick: (_, rowData: AdminEditIntersectionFormType) => onEdit(rowData),
    },
    {
      icon: () => <DeleteOutline />,
      tooltip: 'Delete Intersection',
      position: 'row',
      onClick: (_, rowData: AdminEditIntersectionFormType) => {
        const buttons = [
          { label: 'Yes', onClick: () => onDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Intersection',
          'Are you sure you want to delete "' + rowData.intersection_id + '"?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      position: 'toolbarOnSelect',
      onClick: (_, rowData: AdminEditIntersectionFormType[]) => {
        const buttons = [
          { label: 'Yes', onClick: () => multiDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Selected Intersections',
          'Are you sure you want to delete ' + rowData.length + ' Intersections?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
    {
      icon: () => <Refresh />,
      position: 'toolbar',
      iconProps: {
        title: 'Refresh Data',
        color: 'info',
        itemType: 'outlined',
      },
      onClick: () => {
        updateTableData()
      },
    },
    {
      icon: () => <AddCircleOutline />,
      position: 'toolbar',
      iconProps: {
        title: 'Add New Intersection',
        color: 'info',
        itemType: 'outlined',
      },
      onClick: () => {
        navigate('addIntersection')
      },
    },
  ]

  const onEdit = (row: AdminEditIntersectionFormType) => {
    // Fetch the intersection info before navigating to ensure updated menu state
    dispatch(clear())
    dispatch(getIntersectionInfo(row.intersection_id))

    dispatch(setEditIntersectionRowData(row))
    navigate('editIntersection/' + row.intersection_id)
  }

  const onDelete = (row: AdminEditIntersectionFormType) => {
    dispatch(deleteIntersection({ intersection_id: row.intersection_id, shouldUpdateTableData: true })).then(
      (data: any) => {
        data.payload.success
          ? toast.success('Intersection Deleted Successfully')
          : toast.error('Failed to delete Intersection due to error: ' + data.payload)
      }
    )
  }

  const multiDelete = (rows: AdminEditIntersectionFormType[]) => {
    dispatch(deleteMultipleIntersections(rows)).then((data: any) => {
      data.payload.success ? toast.success('Intersections Deleted Successfully') : toast.error(data.payload.message)
    })
  }

  return (
    <div>
      <Routes>
        <Route
          path="/"
          element={
            loading === false && (
              <div className="scroll-div-tab">
                <AdminTable
                  title={''}
                  data={tableData}
                  columns={columns?.map((column) => ({ ...column }))}
                  actions={tableActions}
                />
              </div>
            )
          }
        />
        <Route path="addIntersection" element={<AdminAddIntersection />} />
        <Route path="editIntersection/:intersectionId" element={<AdminEditIntersection />} />
        <Route
          path="*"
          element={
            <NotFound
              redirectRoute="/dashboard/admin/intersections"
              redirectRouteName="Admin Intersection Page"
              offsetHeight={319}
              description="This page does not exist. Please return to the admin Intersection page."
            />
          }
        />
      </Routes>
    </div>
  )
}

export default AdminIntersectionTab
