import React from 'react'
import AdminAddIntersection from '../adminAddIntersection/AdminAddIntersection'
import AdminEditIntersection, { AdminEditIntersectionFormType } from '../adminEditIntersection/AdminEditIntersection'
import AdminTable from '../../components/AdminTable'
import { IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
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
import { useSelector, useDispatch } from 'react-redux'

import './Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action } from '@material-table/core'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager Intersections'
  } else if (activeTab === 'editIntersection') {
    return ''
  } else if (activeTab === 'addIntersection') {
    return ''
  }
  return 'Unknown'
}

const AdminIntersectionTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]
  const title = getTitle(activeTab)

  const tableData = useSelector(selectTableData)
  const columns = useSelector(selectColumns)

  const loading = useSelector(selectLoading)

  const tableActions: Action<AdminEditIntersectionFormType>[] = [
    {
      icon: 'delete',
      tooltip: 'Delete Intersection',
      position: 'row',
      onClick: (event, rowData: AdminEditIntersectionFormType) => {
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
      icon: 'edit',
      tooltip: 'Edit Intersection',
      position: 'row',
      onClick: (event, rowData: AdminEditIntersectionFormType) => onEdit(rowData),
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      onClick: (event, rowData: AdminEditIntersectionFormType[]) => {
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
  ]

  const onEdit = (row: AdminEditIntersectionFormType) => {
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
      <div>
        <h3 className="panel-header">
          {title}
          {activeTab === undefined && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={(value) => {
                navigate('addIntersection')
              }}
              title="Add Intersection"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={(value) => {
                dispatch(updateTableData())
              }}
              title="Refresh Intersections"
            >
              <IoRefresh size={20} />
            </button>,
          ]}
        </h3>
      </div>
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
