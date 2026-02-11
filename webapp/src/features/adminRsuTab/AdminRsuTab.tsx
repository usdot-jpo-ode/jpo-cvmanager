import React, { useState } from 'react'
import AdminAddRsu from '../adminAddRsu/AdminAddRsu'
import AdminEditRsu, { AdminEditRsuFormType } from '../adminEditRsu/AdminEditRsu'
import AdminTable from '../../components/AdminTable'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import { selectOrganizationName } from '../../generalSlices/userSlice'
import { useSelector, useDispatch } from 'react-redux'

import './Admin.css'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action, OrderByCollection } from '@material-table/core'
import { Route, Routes, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { useTheme } from '@mui/material'
import { DeleteOutline, ModeEditOutline } from '@mui/icons-material'
import { useGetAllRsusQuery } from '../api/rsuApiSlice'
import { useDeleteRsuMutation, useDeleteMultipleRsusMutation } from '../api/rsuApiSlice'

const AdminRsuTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const theme = useTheme()
  const organization = useSelector(selectOrganizationName)

  // Pagination and sorting state
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(100)
  const [sortField, setSortField] = useState<string>('milepost')
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc')

  const {
    data: paginatedData,
    refetch,
    isLoading,
  } = useGetAllRsusQuery({
    organization,
    page,
    size: pageSize,
    sort: `${sortField},${sortDirection}`, // Spring Boot sort format
  })
  const [deleteRsuApi, { isLoading: isDeleting }] = useDeleteRsuMutation()
  const [deleteMultipleRsusApi, { isLoading: isDeletingMultiple }] = useDeleteMultipleRsusMutation()

  const tableData = paginatedData?.content ?? []
  const totalElements = paginatedData?.totalElements ?? 0

  const [columns] = useState([
    { title: 'Milepost', field: 'milepost', id: 0 },
    { title: 'IP Address', field: 'ip', id: 1 },
    { title: 'Primary Route', field: 'primary_route', id: 2 },
    { title: 'RSU Model', field: 'model', id: 3 },
    { title: 'Serial Number', field: 'serial_number', id: 4 },
  ])

  const tableActions: Action<AdminEditRsuFormType>[] = [
    {
      icon: () => <ModeEditOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      tooltip: 'Edit RSU',
      position: 'row',
      iconProps: {
        itemType: 'rowAction',
      },
      onClick: (event, rowData: AdminEditRsuFormType) => {
        onEdit(rowData)
      },
    },
    {
      icon: () => <DeleteOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      tooltip: 'Delete RSU',
      position: 'row',
      iconProps: {
        itemType: 'rowAction',
      },
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
      tooltip: 'Remove All Selected From Organization',
      icon: 'delete',
      iconProps: {
        itemType: 'rowAction',
      },
      position: 'toolbarOnSelect',
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
    {
      icon: () => null,
      iconProps: {
        title: 'Refresh',
        color: 'info',
        itemType: 'outlined',
      },
      position: 'toolbar',
      onClick: refetch,
    },
    {
      icon: () => null,
      position: 'toolbar',
      iconProps: {
        title: 'New',
        color: 'primary',
        itemType: 'contained',
      },
      onClick: () => {
        navigate('addRsu')
      },
    },
  ]

  const onEdit = (row: AdminEditRsuFormType) => {
    navigate('editRsu/' + row.ip)
  }

  const onDelete = async (row: AdminEditRsuFormType) => {
    const loadingToast = toast.loading(`Deleting RSU ${row.ip}...`)
    try {
      await deleteRsuApi(row.ip).unwrap()
      toast.success('RSU Deleted Successfully', { id: loadingToast })
    } catch (error) {
      toast.error('Failed to delete RSU due to error: ' + error, { id: loadingToast })
    }
  }

  const multiDelete = async (rows: AdminEditRsuFormType[]) => {
    const loadingToast = toast.loading(`Deleting ${rows.length} RSUs...`)
    try {
      await deleteMultipleRsusApi(rows.map((row) => row.ip)).unwrap()
      toast.success('RSUs Deleted Successfully', { id: loadingToast })
    } catch (error) {
      toast.error('Failed to delete RSUs due to error: ' + error, { id: loadingToast })
    }
  }

  const handlePageChange = (newPage: number, newPageSize: number) => {
    setPage(newPage)
    setPageSize(newPageSize)
  }

  const handleOrderCollectionChange = (orderByCollection: OrderByCollection[]) => {
    if (orderByCollection.length > 0) {
      const order = orderByCollection[0] // Get the first sort order
      const column = columns[order.orderBy]

      if (column?.field) {
        setSortField(column.field)
        setSortDirection(order.orderDirection === 'desc' ? 'desc' : 'asc')
        setPage(0) // Reset to first page when sorting changes
      }
    }
  }

  return (
    <div>
      <Routes>
        <Route
          path="/"
          element={
            isLoading === false && (
              <div className="scroll-div-tab">
                <AdminTable
                  title={''}
                  data={tableData}
                  columns={columns}
                  actions={tableActions}
                  page={page}
                  totalCount={totalElements}
                  onPageChange={handlePageChange}
                  onOrderCollectionChange={handleOrderCollectionChange}
                />
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
