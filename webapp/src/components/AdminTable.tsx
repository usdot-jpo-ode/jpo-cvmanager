import React from 'react'
import MaterialTable, { Action, Column, MTableCell, MTableToolbar } from '@material-table/core'
import { makeStyles } from '@mui/styles'

import '../features/adminRsuTab/Admin.css'
import { alpha, Tooltip, useTheme, Button, Typography } from '@mui/material'

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
      paddingLeft: 0,
      paddingRight: 0,
    },
    '& .MuiTextField-root': {
      paddingLeft: 16,
      width: '50%',
    },
    '& .MuiBox-root:empty': {
      display: 'none',
    },
    '& .MuiBox-root': {
      width: '50%',
      display: 'flex',
      justifyContent: 'flex-end',
    },
  },
  tableWrapper: {
    '& .MuiTableCell-root': {
      textTransform: 'capitalize',
    },
  },
})

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
    <div>
      <MaterialTable
        actions={props.actions}
        columns={props.columns?.map((column) => ({
          ...column,
          cellStyle: {
            borderRight: `1px solid ${alpha(theme.palette.divider, 0.1)}`, // Add column lines
          },
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
            backgroundColor: theme.palette.custom.tableHeaderBackground,
          },
          pageSize: 5,
          pageSizeOptions: props.pageSizeOptions === undefined ? [5, 10, 20] : props.pageSizeOptions,
        }}
        components={{
          Cell: (props) => {
            const rowData = props.data
            return (
              <Tooltip title={isMissingOrganizations(rowData) ? 'Missing organizations' : ''}>
                <MTableCell {...props} />
              </Tooltip>
            )
          },
          Action: (props) => {
            const { action } = props
            const icon = action.icon
            const iconProps = action.iconProps
            return (
              <Button
                variant={iconProps.itemType}
                color={iconProps.color}
                size="small"
                startIcon={icon}
                sx={{ padding: '4px 8px', margin: '0px 4px' }}
                onClick={() => action.onClick()}
              >
                <Typography className="capital-case museo-slab" fontSize="12px">
                  {iconProps.title}
                </Typography>
              </Button>
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
    </div>
  )
}
export default AdminTable
