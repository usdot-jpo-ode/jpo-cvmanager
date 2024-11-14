import React from 'react'
import MaterialTable, { Action, Column, MTableBodyRow, MTableCell } from '@material-table/core'
import { ThemeProvider, StyledEngineProvider, alpha } from '@mui/material/styles'
import { tableTheme } from '../styles'

import '../features/adminRsuTab/Admin.css'
import { Tooltip } from '@mui/material'

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
              rowStyle: (rowData) => ({
                overflowWrap: 'break-word',
                backgroundColor: isMissingOrganizations(rowData) ? '#4d2e2e' : 'inherit', // Highlight row if missing organizations
              }),
              pageSize: 5,
              pageSizeOptions: props.pageSizeOptions === undefined ? [5, 10, 20] : props.pageSizeOptions,
            }}
            components={{
              Row: (props) => {
                const rowData = props.data
                return (
                  <Tooltip title={isMissingOrganizations(rowData) ? 'Missing organizations' : ''} arrow>
                    <MTableBodyRow {...props} />
                  </Tooltip>
                )
              },
            }}
          />
        </ThemeProvider>
      </StyledEngineProvider>
    </div>
  )
}
export default AdminTable
