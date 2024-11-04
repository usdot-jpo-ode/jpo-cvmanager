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
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { ContainedIconButton } from '../../styles/components/ContainedIconButton'

const getTitle = (activeTab: string) => {
  if (activeTab === undefined) {
    return 'CV Manager RSUs'
  } else if (activeTab === 'editRsu') {
    return ''
  } else if (activeTab === 'addRsu') {
    return ''
  }
  return 'Unknown'
}

const AdminRsuTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()

  const activeTab = location.pathname.split('/')[4]
  const title = getTitle(activeTab)

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
    dispatch(deleteRsu({ rsu_ip: row.ip, shouldUpdateTableData: true })).then((data: any) => {
      data.payload.success
        ? toast.success('RSU Deleted Successfully')
        : toast.error('Failed to delete RSU due to error: ' + data.payload)
    })
  }

  const multiDelete = (rows: AdminEditRsuFormType[]) => {
    dispatch(deleteMultipleRsus(rows)).then((data: any) => {
      data.payload.success ? toast.success('RSUs Deleted Successfully') : toast.error(data.payload.message)
    })
  }

  return (
    <div>
      <div>
        <h3 className="panel-header" key="adminRsuTab">
          {title}
          {activeTab === undefined && [
            <>
              <ContainedIconButton
                key="plus_button"
                title="Add RSU"
                onClick={() => navigate('addRsu')}
                sx={{
                  float: 'right',
                  margin: 2,
                  mt: -0.5,
                  mr: 0,
                  ml: 0.5,
                }}
              >
                <AiOutlinePlusCircle size={20} />
              </ContainedIconButton>
              <ContainedIconButton
                key="refresh_button"
                title="Refresh RSUs"
                onClick={() => dispatch(updateTableData())}
                sx={{
                  float: 'right',
                  margin: 2,
                  mt: -0.5,
                  mr: 0,
                  ml: 0.5,
                }}
              >
                <IoRefresh size={20} />
              </ContainedIconButton>
            </>,
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
        <Route path="addRsu" element={<AdminAddRsu />} />
        <Route path="editRsu/:rsuIp" element={<AdminEditRsu />} />
        <Route
          path="*"
          element={
            <NotFound
              redirectRoute="/dashboard/admin/rsus"
              redirectRouteName="Admin RSU Page"
              offsetHeight={319}
              description="This page does not exist. Please return to the admin RSU page."
            />
          }
        />
      </Routes>
    </div>
  )
}

export default AdminRsuTab
