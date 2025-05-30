import React from 'react'
import { format } from 'date-fns'
import {
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TablePagination,
  TableRow,
  Typography,
  useTheme,
} from '@mui/material'
import PerfectScrollbar from 'react-perfect-scrollbar'
import { ReportMetadata } from '../../../apis/intersections/reports-api'

interface ReportRowProps {
  report: ReportMetadata
  onViewReport: (report: ReportMetadata) => void
}

const ReportRow = (props: ReportRowProps) => {
  const theme = useTheme()
  const { report, onViewReport } = props

  return (
    <TableRow
      key={report.reportName}
      sx={{
        boxShadow: 1,
        transition: (theme) =>
          theme.transitions.create('box-shadow', {
            easing: theme.transitions.easing.easeOut,
          }),
        '&:hover': {
          boxShadow: 8,
        },
        '& > td': {
          backgroundColor: 'background.paper',
          borderBottom: 0,
        },
      }}
    >
      <TableCell width="25%">
        <Box
          component="a"
          sx={{
            alignItems: 'center',
            display: 'inline-flex',
            textDecoration: 'none',
            whiteSpace: 'nowrap',
          }}
        >
          <Box sx={{ ml: 2 }}>
            <Typography color="textSecondary" variant="subtitle2">
              {report.reportName}
            </Typography>
          </Box>
        </Box>
      </TableCell>
      <TableCell>
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Typography variant="subtitle2">Report Duration</Typography>
          <Typography color="textSecondary" variant="body2">
            {report.reportStartTime && format(report.reportStartTime, 'MMM dd, h:mm:ss a')} -{' '}
            {report.reportStopTime && format(report.reportStopTime, 'MMM dd, h:mm:ss a')}
          </Typography>
        </Box>
      </TableCell>
      <TableCell>
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Typography variant="subtitle2">Generated At</Typography>
          <Typography color="textSecondary" variant="body2">
            {report.reportGeneratedAt && format(report.reportGeneratedAt, 'MMM dd, h:mm:ss a')}
          </Typography>
        </Box>
      </TableCell>
      <TableCell align="right">
        <Button
          onClick={() => onViewReport(report)}
          className="capital-case"
          sx={{ color: theme.palette.custom.rowActionIcon }}
        >
          View
        </Button>
      </TableCell>
    </TableRow>
  )
}

interface ReportListTableProps {
  reports: ReportMetadata[]
  reportsCount: number
  onPageChange: (event: React.MouseEvent<HTMLButtonElement> | null, newPage: number) => void
  onRowsPerPageChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void
  page: number
  rowsPerPage: number
  onViewReport: (report: ReportMetadata) => void
}

export const ReportListTable = (props: ReportListTableProps) => {
  const { reports, reportsCount, onPageChange, onRowsPerPageChange, page, rowsPerPage, onViewReport, ...other } = props

  return (
    <div {...other}>
      <PerfectScrollbar>
        <Table
          sx={{
            borderCollapse: 'separate',
            borderSpacing: (theme) => `0 ${theme.spacing(3)}`,
            minWidth: 600,
            p: '1px',
          }}
        >
          {
            <TableBody>
              {reports.map((report: ReportMetadata) => (
                <ReportRow key={report.reportName} report={report} onViewReport={onViewReport} />
              ))}
            </TableBody>
          }
        </Table>
      </PerfectScrollbar>
      <TablePagination
        component="div"
        count={reportsCount}
        onPageChange={onPageChange}
        onRowsPerPageChange={onRowsPerPageChange}
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[5, 10, 25]}
      />
    </div>
  )
}
