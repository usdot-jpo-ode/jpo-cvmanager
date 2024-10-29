import React from 'react'
import MaterialTable, { Action, Column } from '@material-table/core'

import '../features/adminRsuTab/Admin.css'
import { alpha, useTheme } from '@mui/material'

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

const AdminTable = (props: AdminTableProps) => {
  const theme = useTheme()
  return (
    <div>
      <MaterialTable
        actions={props.actions}
        columns={props.columns.map((column) => ({
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
          actionsColumnIndex: -1,
          tableLayout: props.tableLayout === undefined ? 'fixed' : props.tableLayout,
          rowStyle: {
            overflowWrap: 'break-word',
            border: `1px solid ${alpha(theme.palette.divider, 0.1)}`, // Add cell borders
          },
          headerStyle: {
            backgroundColor: theme.palette.custom.tableHeaderBackground,
          },
          pageSize: 5,
          pageSizeOptions: props.pageSizeOptions === undefined ? [5, 10, 20] : props.pageSizeOptions,
        }}
      />
    </div>
  )
}
export default AdminTable
