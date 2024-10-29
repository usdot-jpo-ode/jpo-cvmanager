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
          selection: true,
          actionsColumnIndex: -1,
          tableLayout: 'fixed',
          rowStyle: {
            overflowWrap: 'break-word',
            border: `1px solid ${alpha(theme.palette.divider, 0.1)}`, // Add cell borders
          },
          headerStyle: {
            backgroundColor: theme.palette.custom.tableHeaderBackground,
          },
        }}
      />
    </div>
  )
}
export default AdminTable
