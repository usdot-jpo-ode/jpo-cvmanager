import { Paper, TableContainer, Table, TableHead, TableRow, TableBody, TableCell, useTheme } from '@mui/material'

import './custom-table.css'

interface CustomTableProps {
  headers: string[]
  data: (string | number)[][]
  [x: string]: any // for extra props
}

export const CustomTable = (props: CustomTableProps) => {
  const theme = useTheme()
  const { headers, data, maxHeight = 400, ...rest } = props
  let rowKey = 0
  let cellKey = 0

  return (
    <TableContainer
      component={Paper}
      sx={{ pt: 0, pb: 0, px: 0, width: 'auto', overflow: 'auto', maxHeight }}
      elevation={0}
    >
      <Table
        stickyHeader
        size="small"
        className="mapSideTable"
        {...rest}
        sx={{
          '& .MuiTableCell-root': {
            px: 0.75,
            py: 0.5,
            fontSize: '12px',
            lineHeight: 1.2,
          },
          '& .MuiTableRow-head, .MuiTableCell-head': {
            backgroundColor: theme.palette.background.paper,
          },
        }}
      >
        <TableHead>
          <TableRow>
            {headers.map((head) => (
              <TableCell
                key={head}
                sx={{
                  minWidth: 0,
                  px: 0.75,
                  py: 0.5,
                  fontSize: '12px !important',
                  fontWeight: 600,
                  textTransform: 'capitalize !important',
                }}
              >
                {head}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {data.map((row) => (
            <TableRow hover key={++rowKey}>
              {row.map((cell) => (
                <TableCell
                  component="th"
                  scope="row"
                  key={++cellKey}
                  sx={{ minWidth: 0, px: 0.75, py: 0.5, whiteSpace: 'pre', fontSize: '12px' }}
                >
                  <>{cell}</>
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
