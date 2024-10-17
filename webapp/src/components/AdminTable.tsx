import React from 'react'
import MaterialTable, { Action, Column } from '@material-table/core'
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles'
import { tableTheme } from '../styles'

import '../features/adminRsuTab/Admin.css'

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
  return (
    <div>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={tableTheme}>
          <MaterialTable
            actions={props.actions}
            columns={props.columns}
            data={props.data}
            title={props.title}
            editable={props.editable}
            options={{
              selection: props.selection === undefined ? true : props.selection,
              actionsColumnIndex: -1,
              tableLayout: props.tableLayout === undefined ? 'fixed' : props.tableLayout,
              rowStyle: {
                overflowWrap: 'break-word',
              },
              pageSize: 5,
              pageSizeOptions: props.pageSizeOptions === undefined ? [5, 10, 20] : props.pageSizeOptions,
            }}
          />
        </ThemeProvider>
      </StyledEngineProvider>
    </div>
  )
}
export default AdminTable
