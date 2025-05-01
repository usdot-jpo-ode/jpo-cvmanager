import React from 'react'
import MaterialTable, { Action, Column, MTableAction, MTableCell, MTableToolbar } from '@material-table/core'
import { makeStyles } from '@mui/styles'

import '../features/adminRsuTab/Admin.css'
import { alpha, Tooltip, useTheme, Button, Typography, Box } from '@mui/material'
import { AddCircleOutline, DeleteOutline, Margin, ModeEditOutline, Refresh } from '@mui/icons-material'

interface AdminTableProps {
  actions: Action<any>[]
  columns: Column<any>[]
  data: any[]
  title: string
  editable?: any
  selection?: boolean
  tableLayout?: 'auto' | 'fixed'
  pageSizeOptions?: any
}

const useStyles = makeStyles({
  toolbarWrapper: {
    '& .MuiToolbar-gutters': {
      paddingLeft: '0px',
      paddingRight: '16px',
      display: 'flex',
      justifyContent: 'space-between',
    },
    '& .MuiTextField-root': {
      paddingLeft: 16,
      width: '260px',
    },
    '& .MuiBox-root:empty': {
      display: 'none',
    },
  },
})

const getActionIcon = (title: string) => {
  switch (title) {
    case 'New':
      return <AddCircleOutline />
    case 'Add RSU':
      return <AddCircleOutline />
    case 'Add User':
      return <AddCircleOutline />
    case 'Add Intersection':
      return <AddCircleOutline />
    case 'Refresh':
      return <Refresh />
    case 'Delete':
      return <DeleteOutline color="primary" />
    case 'Edit':
      return <ModeEditOutline color="primary" />
    default:
      return null
  }
}

const AdminTable = (props: AdminTableProps) => {
  const theme = useTheme()
  const classes = useStyles()
  // Function to check if a row is missing organizations
  const isMissingOrganizations = (rowData: any) => {
    try {
      return Array.isArray(rowData?.organizations) && rowData?.organizations?.length === 0
    } catch (e) {
      console.error('Error checking if row is missing organizations:', e)
      return false
    }
  }

  return (
    <Box
      sx={{
        '& .MuiTableSortLabel-root': { textTransform: 'capitalize' },
        '& .MuiTableCell-footer': {
          borderBottom: 'none',
        },
        '& .MuiPaper-root': {
          boxShadow: 'none',
        },
      }}
      className="admin-table"
    >
      <MaterialTable
        actions={props.actions}
        columns={props.columns?.map((column) => ({
          ...column,
        }))}
        data={props.data}
        title={props.title}
        editable={props.editable}
        options={{
          selection: props.selection === undefined ? true : props.selection,
          searchFieldAlignment: 'left',
          actionsColumnIndex: -1,
          tableLayout: props.tableLayout === undefined ? 'fixed' : props.tableLayout,
          rowStyle: (rowData) => ({
            overflowWrap: 'break-word',
            border: `1px solid ${alpha(theme.palette.divider, 0.1)}`, // Add cell borders
            backgroundColor: isMissingOrganizations(rowData) ? theme.palette.custom.tableErrorBackground : 'inherit', // Highlight row if missing organizations
          }),
          headerStyle: {
            backgroundColor: theme.palette.background.paper,
          },
          pageSize: 5,
          pageSizeOptions: props.pageSizeOptions === undefined ? [5, 10, 20] : props.pageSizeOptions,
        }}
        components={{
          Cell: (props) => {
            const rowData = props.data
            return (
              <Tooltip title={isMissingOrganizations(rowData) ? 'Missing organizations' : ''}>
                <MTableCell
                  sx={{
                    padding: '6px 16px',
                    textTransform: 'none !important',
                    '&.MuiTableCell-body': {
                      color: theme.palette.text.secondary + ' !important',
                    },
                    '& .MuiButtonBase-root': {
                      borderRadius: '4px !important',
                    },
                  }}
                  {...props}
                />
              </Tooltip>
            )
          },
          Action: (props: any) => {
            const { action } = props
            const iconProps = action?.iconProps

            if (iconProps?.itemType === 'displayIcon') {
              return (
                <Box>
                  <action.icon />
                </Box>
              )
            }

            if (iconProps?.title !== null && iconProps?.title !== undefined) {
              return (
                <Button
                  variant={iconProps?.itemType}
                  color={iconProps?.color}
                  size="small"
                  startIcon={getActionIcon(iconProps?.title)}
                  sx={{ padding: '4px 8px', margin: '0px 4px' }}
                  onClick={action?.onClick}
                >
                  <Typography className="capital-case museo-slab" fontSize="12px">
                    {iconProps?.title}
                  </Typography>
                </Button>
              )
            }

            return (
              <Box
                sx={{
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.custom.rowActionIcon, 0.1),
                    borderRadius: '4px',
                  },
                  '& .MuiButtonBase-root': {
                    borderRadius: '4px',
                    color: theme.palette.custom.rowActionIcon,
                  },
                }}
              >
                <MTableAction {...props} />
              </Box>
            )
          },
          Toolbar: (props) => {
            return (
              <div className={classes.toolbarWrapper}>
                <MTableToolbar {...props} />
              </div>
            )
          },
        }}
      />
    </Box>
  )
}
export default AdminTable
