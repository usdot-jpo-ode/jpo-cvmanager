import React, { useState, useEffect } from 'react'
import AdminAddRsu from '../adminAddRsu/AdminAddRsu'
import AdminEditRsu from '../adminEditRsu/AdminEditRsu'
import AdminTable from '../../components/AdminTable'
import { IoChevronBackCircleOutline, IoRefresh } from 'react-icons/io5'
import { AiOutlinePlusCircle } from 'react-icons/ai'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import {
  selectLoading,
  selectActiveDiv,
  selectTableData,
  selectTitle,
  selectEditRsuRowData,

  // actions
  updateTableData,
  setTitle,
  setActiveDiv,
  deleteMultipleRsus,
  deleteRsu,
  setEditRsuRowData,
} from './adminRsuTabSlice'
import { useSelector, useDispatch } from 'react-redux'

import './Admin.css'

const AdminRsuTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const activeDiv = useSelector(selectActiveDiv)
  const tableData = useSelector(selectTableData)
  const title = useSelector(selectTitle)
  const [columns] = useState([
    { title: 'Milepost', field: 'milepost', id: 0 },
    { title: 'IP Address', field: 'ip', id: 1 },
    { title: 'Primary Route', field: 'primary_route', id: 2 },
    { title: 'RSU Model', field: 'model', id: 3 },
    { title: 'Serial Number', field: 'serial_number', id: 4 },
  ])
  const editRsuRowData = useSelector(selectEditRsuRowData)

  const loading = useSelector(selectLoading)

  const tableActions = [
    {
      icon: 'delete',
      tooltip: 'Delete RSU',
      position: 'row',
      onClick: (event, rowData) => {
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
      onClick: (event, rowData) => onEdit(rowData),
    },
    {
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      onClick: (event, rowData) => {
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
  useEffect(() => {
    dispatch(setActiveDiv('rsu_table'))
  }, [dispatch])

  useEffect(() => {
    dispatch(setTitle())
  }, [activeDiv, dispatch])

  const onEdit = (row) => {
    dispatch(setEditRsuRowData(row))
    dispatch(setActiveDiv('edit_rsu'))
  }

  const onDelete = (row) => {
    dispatch(deleteRsu({ rsu_ip: row.ip, shouldUpdateTableData: true }))
  }

  const multiDelete = (rows) => {
    dispatch(deleteMultipleRsus(rows))
  }

  return (
    <div>
      <div>
        <h3 className="panel-header">
          {activeDiv !== 'rsu_table' && (
            <button
              key="rsu_table"
              className="admin_table_button"
              onClick={(value) => {
                dispatch(setActiveDiv('rsu_table'))
              }}
            >
              <IoChevronBackCircleOutline size={20} />
            </button>
          )}
          {title}
          {activeDiv === 'rsu_table' && [
            <button
              key="plus_button"
              className="plus_button"
              onClick={(value) => {
                dispatch(setActiveDiv('add_rsu'))
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
      {activeDiv === 'rsu_table' && loading === false && (
        <div className="scroll-div-tab">
          <AdminTable
            title={''}
            data={tableData}
            columns={columns}
            actions={tableActions}
            onEdit={onEdit}
            onDelete={onDelete}
            multiDelete={multiDelete}
          />
        </div>
      )}

      {activeDiv === 'add_rsu' && (
        <div className="scroll-div-tab">
          <AdminAddRsu updateRsuData={() => dispatch(updateTableData())} />
        </div>
      )}

      {activeDiv === 'edit_rsu' && (
        <div className="scroll-div-tab">
          <AdminEditRsu rsuData={editRsuRowData} updateRsuData={() => dispatch(updateTableData())} />
        </div>
      )}
    </div>
  )
}

export default AdminRsuTab
