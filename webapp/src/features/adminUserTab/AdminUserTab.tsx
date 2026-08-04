import { useState, useRef, useCallback } from 'react'
import AdminAddUser from '../adminAddUser/AdminAddUser'
import AdminEditUser from '../adminEditUser/AdminEditUser'
import AdminTable, { buildAdminTableQueryParams } from '../../components/AdminTable'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import { selectOrganizationName } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'

import '../adminRsuTab/Admin.css'
import { Action } from '@material-table/core'
import { Route, Routes, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { DeleteOutline, ModeEditOutline } from '@mui/icons-material'
import { useTheme } from '@mui/material'
import {
  useDeleteMultipleUsersMutation,
  useDeleteUserMutation,
  useGetUsersQuery,
  useLazyGetUsersQuery,
} from '../api/userApiSlice'
import { useAdminTableQuerySync } from '../../hooks/useAdminTableQuerySync'

const AdminUserTab = () => {
  const navigate = useNavigate()
  const theme = useTheme()
  const organization = useSelector(selectOrganizationName)

  const tableRef = useRef<any>(null)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [currentParams, setCurrentParams] = useState({
    page: 0,
    size: 20,
    sort: 'first_name,asc',
    search: '',
    organization: organization || '',
  })

  const [trigger] = useLazyGetUsersQuery()
  const { data: subscribedData } = useGetUsersQuery(currentParams, {
    skip: !organization,
  })
  const { currentQueryRef, markTableRenderedData, handleRefresh } = useAdminTableQuerySync({
    organization,
    tableRef,
    isRefreshing,
    currentPage: currentParams.page,
    subscribedData,
  })

  const handleQueryChange = useCallback(
    async (query) => {
      setIsRefreshing(true)

      try {
        const params = buildAdminTableQueryParams(query, columns, organization, 'first_name', 'asc')

        // Check if organization changed - if so, reset to page 0
        if (currentQueryRef.current && currentQueryRef.current.organization !== params.organization) {
          params.page = 0
          query.page = 0
        }

        // Store current query for comparison
        currentQueryRef.current = params
        setCurrentParams(params)

        // Trigger the query and await the result
        const result = await trigger(params).unwrap()

        markTableRenderedData(params, result)

        return {
          data: result.content || [],
          page: params.page,
          totalCount: result.totalElements || 0,
        }
      } catch (error) {
        console.error('Failed to fetch users:', error)
        toast.error('Failed to fetch Users')
        return {
          data: [],
          page: query.page,
          totalCount: 0,
        }
      } finally {
        setIsRefreshing(false)
      }
    },
    [trigger, organization, markTableRenderedData]
  )

  const [deleteUserApi] = useDeleteUserMutation()
  const [deleteMultipleUsersApi] = useDeleteMultipleUsersMutation()

  const [columns] = useState([
    { title: 'First Name', field: 'first_name', id: 0 },
    { title: 'Last Name', field: 'last_name', id: 1 },
    { title: 'Email', field: 'email', id: 2 },
    {
      title: 'Super User',
      field: 'super_user',
      id: 3,
      render: (rowData: AdminUserWithId) => (rowData.super_user ? 'Yes' : 'No'),
    },
  ])

  const tableActions: Action<AdminUserWithId>[] = [
    {
      icon: () => <ModeEditOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      iconProps: {
        itemType: 'rowAction',
      },
      position: 'row',
      onClick: (event, rowData: AdminUserWithId) => onEdit(rowData),
    },
    {
      icon: () => <DeleteOutline sx={{ color: theme.palette.custom.rowActionIcon }} />,
      iconProps: {
        itemType: 'rowAction',
      },
      position: 'row',
      onClick: (event, rowData: AdminUserWithId) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => onDelete(rowData),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options('Delete User', 'Are you sure you want to delete "' + rowData.email + '"?', buttons)
        confirmAlert(alertOptions)
      },
    },
    {
      tooltip: 'Remove All Selected Users',
      icon: 'delete',
      position: 'toolbarOnSelect',
      iconProps: {
        itemType: 'rowAction',
      },
      onClick: (event, rowData: AdminUserWithId[]) => {
        const buttons = [
          {
            label: 'Yes',
            onClick: () => multiDelete(rowData),
          },
          {
            label: 'No',
            onClick: () => {},
          },
        ]
        const alertOptions = Options(
          'Delete Selected Users',
          'Are you sure you want to delete ' + rowData.length + ' users?',
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
      onClick: handleRefresh,
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
        navigate('addUser')
      },
    },
  ]

  const onDelete = async (row: AdminUserWithId) => {
    const loadingToast = toast.loading(`Deleting User ${row.email}...`)
    try {
      await deleteUserApi(row.email).unwrap()
      handleRefresh()
      toast.success('User Deleted Successfully', { id: loadingToast })
    } catch (error) {
      toast.error('Failed to delete User due to error: ' + error, { id: loadingToast })
    }
  }

  const multiDelete = async (rows: AdminUserWithId[]) => {
    const loadingToast = toast.loading(`Deleting ${rows.length} Users...`)
    try {
      await deleteMultipleUsersApi(rows.map((row) => row.email)).unwrap()
      handleRefresh()
      toast.success('Users Deleted Successfully', { id: loadingToast })
    } catch (error) {
      toast.error('Failed to delete Users due to error: ' + error, { id: loadingToast })
    }
  }

  const onEdit = (row: AdminUserWithId) => {
    navigate('editUser/' + row.email)
  }

  return (
    <div>
      <Routes>
        <Route
          path="/"
          element={
            <div className="scroll-div-tab">
              <AdminTable
                title={''}
                columns={columns}
                actions={tableActions}
                handleQueryChange={handleQueryChange}
                isLoading={isRefreshing}
                tableRef={tableRef}
              />
            </div>
          }
        />
        <Route
          path="addUser"
          element={
            <div className="scroll-div-tab">
              <AdminAddUser />
            </div>
          }
        />
        <Route
          path="editUser/:email"
          element={
            <div className="scroll-div-tab">
              <AdminEditUser />
            </div>
          }
        />
        <Route
          path="*"
          element={
            <NotFound
              redirectRoute="/dashboard/admin/users"
              redirectRouteName="Admin User Page"
              offsetHeight={319}
              description="This page does not exist. Please return to the admin User page."
            />
          }
        />
      </Routes>
    </div>
  )
}

export default AdminUserTab
