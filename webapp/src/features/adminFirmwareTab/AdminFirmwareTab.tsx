import { useCallback, useRef, useState } from 'react'
import { Action, Column, Query } from '@material-table/core'
import { Box, Button, FormControl, InputLabel, MenuItem, Select, Typography } from '@mui/material'
import DeleteOutline from '@mui/icons-material/DeleteOutline'
import { confirmAlert } from 'react-confirm-alert'
import { Options } from '../../components/AdminDeletionOptions'
import toast from 'react-hot-toast'
import AdminTable from '../../components/AdminTable'
import { FirmwareObject } from '../../models/Firmware'
import {
  useDeleteFirmwareObjectMutation,
  useGetFirmwareUploadOptionsQuery,
  useLazyListFirmwareObjectsQuery,
  useListFirmwareRulesQuery,
} from '../api/firmwareApiSlice'
import FirmwareUploadForm from './FirmwareUploadForm'
import FirmwareRulesDialog from './FirmwareRulesDialog'
import { formatFileSize } from './firmwareUpload'
import '../adminRsuTab/Admin.css'

const DEFAULT_PAGE_SIZE = 25
const HEADER_STYLE = { textTransform: 'none' as const }
const FIRMWARE_SORT_FIELDS = ['manufacturer', 'model', 'version', 'content_length', 'updated_at', 'verification_status']

const firmwareSort = (query: Query<FirmwareObject>) => {
  const sort = query.orderByCollection?.[0]
  const orderBy = sort?.orderBy as number | Column<FirmwareObject> | undefined
  const field = typeof orderBy === 'number' ? FIRMWARE_SORT_FIELDS[orderBy] : orderBy?.field
  return typeof field === 'string' ? `${field},${sort.orderDirection || 'asc'}` : 'manufacturer,asc'
}

const formatUpdatedAt = (value: string | number | null | undefined) => {
  if (value == null) return ''
  const timestamp = typeof value === 'number' ? value * 1000 : value
  return new Date(timestamp).toLocaleString()
}

const verificationLabel = {
  VERIFIED: 'Verified',
  UNVERIFIED: 'Unverified',
  UNTRACKED: 'Untracked',
  CHANGED: 'Object changed',
  MISSING: 'Missing file',
}

const verificationColor = {
  VERIFIED: 'success.light',
  UNVERIFIED: 'warning.light',
  UNTRACKED: 'text.primary',
  CHANGED: 'error.light',
  MISSING: 'warning.light',
}

const FirmwareDeleteIcon = () => (
  <DeleteOutline sx={{ color: (theme) => theme.palette.custom.rowActionIcon }} />
)

const FirmwareDeletionDetails = ({ object }: { object: FirmwareObject }) => (
  <div style={{ marginTop: 16, whiteSpace: 'pre-line', overflowWrap: 'anywhere' }}>
    {`Manufacturer: ${object.manufacturer ?? 'Unknown'}\nModel: ${object.model ?? 'Unknown'}\nVersion: ${object.version ?? 'Unknown'}\nFile: ${object.file_name}`}
  </div>
)

const AdminFirmwareTab = () => {
  const tableRef = useRef<any>(null)
  const manufacturerRef = useRef('')
  const [manufacturer, setManufacturer] = useState('')
  const [showUpload, setShowUpload] = useState(false)
  const [rulesFirmwareId, setRulesFirmwareId] = useState<number>()
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const deleting = useRef(false)

  const [deleteFirmwareObject] = useDeleteFirmwareObjectMutation()
  const [listFirmwareObjects] = useLazyListFirmwareObjectsQuery()
  const { data: uploadOptions, isFetching: isFetchingOptions } = useGetFirmwareUploadOptionsQuery()
  const { data: upgradeRules, isError: rulesFailed, refetch: refreshRules } = useListFirmwareRulesQuery()

  const refreshListing = useCallback(() => {
    void refreshRules()
    setIsRefreshing(true)
    if (!tableRef.current?.onQueryChange) {
      setIsRefreshing(false)
      return
    }
    void Promise.resolve(tableRef.current.onQueryChange({ page: 0 }))
      .catch(() => { /* The query handler displays the fetch error. */ })
  }, [refreshRules])

  const deleteFirmware = async (object: FirmwareObject) => {
    if (deleting.current) return
    const missing = object.verification_status === 'MISSING'
    if (!missing && !object.provider_object_version) {
      toast.error('This file has no object version. Refresh the table before deleting it.')
      return
    }
    deleting.current = true
    setIsDeleting(true)
    const notification = toast.loading(missing ? 'Cleaning up firmware records...' : 'Deleting firmware...')
    try {
      await deleteFirmwareObject({
        object_id: object.object_id,
        provider_object_version: missing ? null : object.provider_object_version,
      }).unwrap()
      toast.success(missing ? 'Firmware records cleaned up successfully' : 'Firmware deleted successfully', {
        id: notification,
      })
      refreshListing()
    } catch (error) {
      const response = error as { data?: { detail?: string; message?: string } }
      toast.error(response?.data?.detail || response?.data?.message || 'Failed to delete firmware. Please retry.', {
        id: notification,
      })
    } finally {
      deleting.current = false
      setIsDeleting(false)
    }
  }

  const handleQueryChange = useCallback(
    async (query: Query<FirmwareObject>) => {
      try {
        const params = {
          page: query.page,
          size: query.pageSize,
          search: query.search || '',
          manufacturer: manufacturerRef.current || undefined,
          sort: firmwareSort(query),
        }

        const result = await listFirmwareObjects(params).unwrap()

        return {
          data: result.objects,
          page: query.page,
          totalCount: result.total_elements,
        }
      } catch (error) {
        console.error('Failed to fetch firmware:', error)
        toast.error('Failed to fetch firmware')
        throw error
      } finally {
        setIsRefreshing(false)
      }
    },
    [listFirmwareObjects]
  )

  const columns: Column<FirmwareObject>[] = [
    { title: 'Manufacturer', field: 'manufacturer', headerStyle: HEADER_STYLE },
    { title: 'Model', field: 'model', headerStyle: HEADER_STYLE },
    {
      title: 'Version',
      field: 'version',
      headerStyle: HEADER_STYLE,
      render: (object) => object.version ?? object.object_name,
    },
    {
      title: 'Size',
      field: 'content_length',
      headerStyle: HEADER_STYLE,
      render: (object) => (object.content_length == null ? '' : formatFileSize(object.content_length)),
    },
    {
      title: 'Last Modified',
      field: 'updated_at',
      headerStyle: HEADER_STYLE,
      render: (object) => formatUpdatedAt(object.updated_at),
    },
    {
      title: 'Verification',
      field: 'verification_status',
      headerStyle: HEADER_STYLE,
      render: (object) => (
        <Typography variant="body2" sx={{ color: verificationColor[object.verification_status], fontWeight: 'bold' }}>
          {verificationLabel[object.verification_status]}
        </Typography>
      ),
    },
  ]

  columns.push({
    title: 'Upgrade Rules',
    sorting: false,
    headerStyle: HEADER_STYLE,
    render: (object) => {
      if (!object.firmware_id) return <Typography variant="body2">Not Verified</Typography>
      const related = upgradeRules?.filter(
        (rule) => rule.source.firmware_id === object.firmware_id || rule.destination.firmware_id === object.firmware_id
      )
      let label = 'No upgrade rules'
      if (rulesFailed) {
        label = 'Could not load rules'
      } else if (!related) {
        label = 'Loading rules…'
      } else if (related.length) {
        label = `${related.length} rule${related.length === 1 ? '' : 's'}`
      }
      return (
        <Button
          size="small"
          color={related?.length === 0 && object.verification_status === 'VERIFIED' ? 'warning' : 'primary'}
          sx={{
            px: 0, minWidth: 0, justifyContent: 'flex-start', textAlign: 'left',
            ...(related?.length === 0 && object.verification_status === 'VERIFIED' && { color: 'warning.light' }),
          }}
          onClick={() => setRulesFirmwareId(object.firmware_id!)}
        >
          {label}
        </Button>
      )
    },
  })

  const tableActions: (Action<FirmwareObject> | ((row: FirmwareObject) => Action<FirmwareObject>))[] = [
    (object) => ({
      position: 'row',
      icon: FirmwareDeleteIcon,
      iconProps: { itemType: 'rowAction' },
      tooltip: object.verification_status === 'MISSING' ? 'Clean Up Records' : 'Delete Firmware',
      disabled: isDeleting,
      onClick: (_event, row: FirmwareObject) => {
        const missing = row.verification_status === 'MISSING'
        confirmAlert({
          ...Options(
            missing ? 'Clean Up Firmware Records' : 'Delete Firmware',
            missing
              ? 'The file is missing from storage. Remove its database records and associated upgrade rules?'
              : 'Delete this file and its database records and associated upgrade rules?',
            [
              { label: 'Yes', onClick: () => deleteFirmware(row) },
              { label: 'No', onClick: () => {} },
            ]
          ),
          childrenElement: FirmwareDeletionDetails.bind(null, { object: row }),
        })
      },
    }),
    {
      position: 'toolbar',
      icon: () => null,
      tooltip: 'Filter by manufacturer',
      iconProps: {
        itemType: 'custom',
        render: () => (
          <FormControl size="small" sx={{ minWidth: 210, mx: 0.5 }} disabled={isFetchingOptions}>
            <InputLabel id="firmware-filter-manufacturer-label">Manufacturer</InputLabel>
            <Select
              labelId="firmware-filter-manufacturer-label"
              label="Manufacturer"
              value={manufacturer}
              onChange={(event) => {
                const value = event.target.value
                manufacturerRef.current = value
                setManufacturer(value)
                tableRef.current?.onQueryChange({ page: 0 })
              }}
            >
              <MenuItem value="">All manufacturers</MenuItem>
              {uploadOptions?.manufacturers.map((option) => (
                <MenuItem key={option.manufacturer_id} value={option.name}>
                  {option.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        ),
      } as any,
      onClick: () => undefined,
    },
    {
      position: 'toolbar',
      icon: () => null,
      tooltip: 'Refresh firmware',
      iconProps: { itemType: 'outlined', color: 'info', title: 'Refresh' },
      disabled: isRefreshing,
      onClick: refreshListing,
    },
    {
      position: 'toolbar',
      icon: () => null,
      tooltip: 'Add firmware',
      iconProps: { itemType: 'contained', title: 'New' },
      onClick: () => setShowUpload(true),
    },
  ]

  return (
    <Box
      className="scroll-div-tab"
      sx={{
        // Match the action-column space of the two-button RSU and User tables.
        '& thead th:last-child, & tbody td:last-child:not([colspan])': {
          width: '96px !important',
        },
        '& tbody td:last-child:not([colspan]) > div': {
          justifyContent: 'center',
        },
      }}
    >
      <AdminTable
        actions={tableActions}
        columns={columns}
        defaultPageSize={DEFAULT_PAGE_SIZE}
        handleQueryChange={handleQueryChange}
        isLoading={isRefreshing}
        selection={false}
        tableRef={tableRef}
        thirdSortClick={false}
        title=""
      />

      {showUpload && (
        <FirmwareUploadForm
          open
          onClose={() => setShowUpload(false)}
          onSuccess={() => {
            setShowUpload(false)
            refreshListing()
          }}
        />
      )}
      {rulesFirmwareId !== undefined && (
        <FirmwareRulesDialog
          key={rulesFirmwareId}
          firmwareId={rulesFirmwareId}
          onClose={() => setRulesFirmwareId(undefined)}
          onChanged={() => {
            void refreshRules()
          }}
        />
      )}
    </Box>
  )
}

export default AdminFirmwareTab
