import React, { useEffect, useState } from 'react'
import AdminAddRsu from '../adminAddRsu/AdminAddRsu'
import AdminEditRsu, { AdminEditRsuFormType } from '../adminEditRsu/AdminEditRsu'
import AdminTable from '../../components/AdminTable'
import { IoChevronBackCircleOutline, IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import {
  selectLoading,
  selectTableData,
  selectEditRsuRowData,

  // actions
  updateTableData,
  deleteMultipleRsus,
  deleteRsu,
  setEditRsuRowData,
} from './adminRsuTabSlice'
import { useSelector, useDispatch } from 'react-redux'

import './Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action } from '@material-table/core'
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { NotFoundRedirect } from '../../pages/404'
import { setRouteNotFound } from '../../generalSlices/userSlice'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager RSUs'
  } else if (activeTab === 'editRsu') {
    return 'Edit RSU'
  } else if (activeTab === 'addRsu') {
    return 'Add RSU'
  }
  return 'Unknown'
}

const AdminRsuTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]
  const title = getTitle(activeTab)
  console.log('Active Tab:', activeTab)

  const tableData = useSelector(selectTableData)
  const [columns] = useState([
    { title: 'Milepost', field: 'milepost', id: 0 },
    { title: 'IP Address', field: 'ip', id: 1 },
    { title: 'Primary Route', field: 'primary_route', id: 2 },
    { title: 'RSU Model', field: 'model', id: 3 },
    { title: 'Serial Number', field: 'serial_number', id: 4 },
  ])

  const loading = useSelector(selectLoading)

  const tableActions: Action<AdminEditRsuFormType>[] = [
    {
      icon: 'delete',
      tooltip: 'Delete RSU',
      position: 'row',
      onClick: (event, rowData: AdminEditRsuFormType) => {
        const buttons = [
          { label: 'Yes', onClick: () => onDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options('Delete RSU', 'Are you sure you want to delete "' + rowData.ip + '"?', buttons)
        confirmAlert(alertOptions)
      },
    },
    {
      icon: 'edit',
      tooltip: 'Edit RSU',
      position: 'row',
      onClick: (event, rowData: AdminEditRsuFormType) => onEdit(rowData),
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      onClick: (event, rowData: AdminEditRsuFormType[]) => {
        const buttons = [
          { label: 'Yes', onClick: () => multiDelete(rowData) },
          { label: 'No', onClick: () => {} },
        ]
        const alertOptions = Options(
          'Delete Selected RSUs',
          'Are you sure you want to delete ' + rowData.length + ' RSUs?',
          buttons
        )
        confirmAlert(alertOptions)
      },
    },
  ]

  const onEdit = (row: AdminEditRsuFormType) => {
    dispatch(setEditRsuRowData(row))
    navigate('editRsu/' + row.ip)
  }

  const onDelete = (row: AdminEditRsuFormType) => {
    dispatch(deleteRsu({ rsu_ip: row.ip, shouldUpdateTableData: true }))
  }

  const multiDelete = (rows: AdminEditRsuFormType[]) => {
    dispatch(deleteMultipleRsus(rows))
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeTab !== undefined && (
            <button
              key="rsu_table"
              className="admin_table_button"
              onClick={(value) => {
                navigate('.')
                // dispatch(setActiveDiv('rsu_table'))
              }}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeTab === undefined && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={(value) => {
                navigate('addRsu')
                // dispatch(setActiveDiv('add_rsu'))
              }}
              title="Add RSU"
            >
              <AiOutlinePlusCircle size={20} />
            </button>,
            <button
              key="refresh_button"
              className="plus_button"
              onClick={(value) => {
                dispatch(updateTableData())
              }}
              title="Refresh RSUs"
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
                <AdminTable title={''} data={tableData} columns={columns} actions={tableActions} />
              </div>
            )
          }
        />
        <Route
          path="addRsu"
          element={
            <div className="scroll-div-tab">
              <AdminAddRsu />
            </div>
          }
        />
        <Route
          path="editRsu/:rsuIp"
          element={
            <div className="scroll-div-tab">
              <AdminEditRsu />
            </div>
          }
        />
        <Route path="*" element={<NotFoundRedirect />} />
      </Routes>
    </div>
  )
}

export default AdminRsuTab
