import { useMemo } from 'react'
import AdminAddIntersection from '../adminAddIntersection/AdminAddIntersection'
import AdminEditIntersection, { AdminEditIntersectionFormType } from '../adminEditIntersection/AdminEditIntersection'
import AdminTable from '../../components/AdminTable'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import { setEditIntersectionRowData, selectColumns } from './adminIntersectionTabSlice'
import { selectOrganizationName } from '../../generalSlices/userSlice'
import { clear } from '../adminEditIntersection/adminEditIntersectionSlice'
import { useSelector, useDispatch } from 'react-redux'
import { useGetIntersectionsQuery, useDeleteIntersectionMutation } from '../api/adminIntersectionApiSlice'

import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import { Action } from '@material-table/core'
import { Route, Routes, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { DeleteOutline, ModeEditOutline } from '@mui/icons-material'
import { useTheme } from '@mui/material'

const AdminIntersectionTab = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const navigate = useNavigate()
  const theme = useTheme()

  const organization = useSelector(selectOrganizationName)
  const { data, isFetching, refetch } = useGetIntersectionsQuery(organization ?? '', {
    skip: !organization,
  })
  const [deleteIntersectionMutation] = useDeleteIntersectionMutation()

  const columns = useSelector(selectColumns)

  const tableData = useMemo(
    () =>
      (data?.intersection_data ?? []).map((element) => ({
        ...element,
        intersection_id: element.intersection_id?.toString(),
        rsus: Array.isArray(element.rsus) ? (element.rsus as string[]).join(', ') : element.rsus,
      })),
    [data]
  )

  const tableActions: Action<AdminEditIntersectionFormType>[] = [
    {
      icon: () => <ModeEditOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      position: 'row',
      iconProps: {
        itemType: 'rowAction',
      },
      onClick: (_, rowData: AdminEditIntersectionFormType) => onEdit(rowData),
    },
    {
      icon: () => <DeleteOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      iconProps: {
        itemType: 'rowAction',
      },
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
      iconProps: {
        itemType: 'rowAction',
      },
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
      icon: () => null,
      position: 'toolbar',
      iconProps: {
        title: 'Refresh',
        color: 'info',
        itemType: 'outlined',
      },
      onClick: () => {
        refetch()
      },
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
        navigate('addIntersection')
      },
    },
  ]

  const onEdit = (row: AdminEditIntersectionFormType) => {
    dispatch(clear())
    dispatch(setEditIntersectionRowData(row))
    navigate('editIntersection/' + row.intersection_id)
  }

  const extractErrorDetail = (error: unknown): string | undefined =>
    error && typeof error === 'object' && 'data' in error && error.data && typeof error.data === 'object'
      ? (error.data as { detail?: string }).detail
      : undefined

  const onDelete = (row: AdminEditIntersectionFormType) => {
    toast.promise(deleteIntersectionMutation(row.intersection_id).unwrap(), {
      loading: `Deleting intersection ${row.intersection_id}`,
      success: `Successfully deleted intersection ${row.intersection_id}`,
      error: (error) => {
        const detail = extractErrorDetail(error)
        return detail
          ? `Failed to delete intersection ${row.intersection_id}: ${detail}`
          : `Failed to delete intersection ${row.intersection_id}`
      },
    })
  }

  const multiDelete = async (rows: AdminEditIntersectionFormType[]) => {
    toast.promise(Promise.all(rows.map((row) => deleteIntersectionMutation(row.intersection_id).unwrap())), {
      loading: 'Deleting selected intersections',
      success: 'Intersections Deleted Successfully',
      error: (error) => {
        const detail = extractErrorDetail(error)
        return detail
          ? `Failed to delete one or more Intersection(s): ${detail}`
          : 'Failed to delete one or more Intersection(s)'
      },
    })
  }

  return (
    <div>
      <Routes>
        <Route
          path="/"
          element={
            !isFetching && (
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
