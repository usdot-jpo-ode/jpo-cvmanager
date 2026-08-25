import { useCallback, useRef, useState } from 'react'
import AdminAddRsu from '../adminAddRsu/AdminAddRsu'
import AdminEditRsu, { AdminEditRsuFormType } from '../adminEditRsu/AdminEditRsu'
import AdminTable, { buildAdminTableQueryParams } from '../../components/AdminTable'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import RsuStatusDialog from './RsuStatusDialog'
import { selectToken } from '../../generalSlices/userSlice'

import { selectOrganizationName } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'
import './Admin.css'
import { Action } from '@material-table/core'
import { Route, Routes, useNavigate } from 'react-router-dom'
import { NotFound } from '../../pages/404'
import toast from 'react-hot-toast'
import { useTheme, Typography } from '@mui/material'
import { DeleteOutline, ModeEditOutline } from '@mui/icons-material'
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined'
import {
  useLazyGetAllRsusQuery,
  useDeleteRsuMutation,
  useDeleteMultipleRsusMutation,
  useGetAllRsusQuery,
} from '../api/rsuApiSlice'
import { useAdminTableQuerySync } from '../../hooks/useAdminTableQuerySync'
import { applyFlagsToList } from '../../feature-flags'

const AdminRsuTab = () => {
  const navigate = useNavigate()
  const theme = useTheme()

  const token = useSelector(selectToken)
  const organization = useSelector(selectOrganizationName)

  const tableRef = useRef<any>(null)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [currentParams, setCurrentParams] = useState({
    page: 0,
    size: 20,
    sort: 'ip,asc',
    search: '',
    organization: organization || '',
  })

  const [trigger] = useLazyGetAllRsusQuery()
  const { data: subscribedData } = useGetAllRsusQuery(currentParams, {
    skip: !organization,
  })
  const { currentQueryRef, markTableRenderedData, handleRefresh } = useAdminTableQuerySync({
    organization,
    tableRef,
    isRefreshing,
    currentPage: currentParams.page,
    subscribedData,
  })

  const [deleteRsuApi] = useDeleteRsuMutation()
  const [deleteMultipleRsusApi] = useDeleteMultipleRsusMutation()

  const [statusDialogOpen, setStatusDialogOpen] = useState(false)
  const [selectedRsuIp, setSelectedRsuIp] = useState<string | null>(null)

  // const tableData = useSelector(selectTableData)
  const [columns] = useState([
    { title: 'Milepost', field: 'milepost', id: 0 },
    { title: 'IP Address', field: 'ip', id: 1 },
    { title: 'Primary Route', field: 'primary_route', id: 2 },
    { title: 'RSU Model', field: 'model', id: 3 },
    { title: 'Serial Number', field: 'serial_number', id: 4 },
    {
      title: 'TIM Deposit',
      field: 'tim_deposit',
      id: 5,
      render: (rowData: any) => (
        <Typography
          variant="body2"
          sx={{
            color: rowData.tim_deposit ? theme.palette.success.light : theme.palette.error.light,
            fontWeight: 'bold',
          }}
        >
          {rowData.tim_deposit ? 'Enabled' : 'Disabled'}
        </Typography>
      ),
    },
    {
      title: 'SNMP Monitoring',
      field: 'snmp_monitoring',
      id: 6,
      render: (rowData: any) => (
        <Typography
          variant="body2"
          sx={{
            color: rowData.snmp_monitoring ? theme.palette.success.light : theme.palette.error.light,
            fontWeight: 'bold',
          }}
        >
          {rowData.snmp_monitoring ? 'Enabled' : 'Disabled'}
        </Typography>
      ),
    },
  ])

  const handleStatusClick = (rowData: AdminEditRsuFormType) => {
    setSelectedRsuIp(rowData.ip)
    setStatusDialogOpen(true)
  }

  const handleStatusDialogClose = () => {
    setStatusDialogOpen(false)
    setSelectedRsuIp(null)
  }

  const tableActions: Action<AdminEditRsuFormType>[] = applyFlagsToList<Action<AdminEditRsuFormType> & FlaggedElem>([
    {
      tag: 'rsuStatusMonitor',
      icon: () => <InfoOutlinedIcon sx={{ color: theme.palette.custom.rowActionIcon }} />,
      tooltip: 'RSU Status',
      position: 'row',
      iconProps: {
        itemType: 'rowAction',
      },
      onClick: (event, rowData: AdminEditRsuFormType) => {
        handleStatusClick(rowData)
      },
    },
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
      isFreeAction: true,
      iconProps: {
        title: 'Refresh',
        color: 'info',
        itemType: 'outlined',
      },
      position: 'toolbar',
      onClick: handleRefresh,
    },
    {
      icon: () => null,
      isFreeAction: true,
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
  ])

  const handleQueryChange = useCallback(
    async (query) => {
      setIsRefreshing(true)

      try {
        const params = buildAdminTableQueryParams(query, columns, organization, 'ip', 'asc')

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
        console.error('Failed to fetch rsus:', error)
        toast.error('Failed to fetch RSUs')
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
              {statusDialogOpen && (
                <RsuStatusDialog
                  open={statusDialogOpen}
                  onClose={handleStatusDialogClose}
                  rsuIp={selectedRsuIp}
                  token={token}
                />
              )}
            </div>
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
