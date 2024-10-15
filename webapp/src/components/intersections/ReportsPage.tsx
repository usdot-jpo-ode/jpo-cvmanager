import React, { useState, useEffect, useRef } from 'react'
import { Box, Button, Stack, Typography } from '@mui/material'
import { styled } from '@mui/material/styles'
import { FilterAlt } from '@mui/icons-material'
import { ReportListFilters } from '../../features/intersections/reports/report-list-filters'
import { ReportListTable } from '../../features/intersections/reports/report-list-table'
import ReportsApi, { ReportMetadata } from '../../apis/intersections/reports-api'
import { ReportGenerationDialog } from '../../features/intersections/reports/report-generation-dialog'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useAppSelector } from '../../hooks'

const applyPagination = (logs, page, rowsPerPage) => logs.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

const LogsListInner = styled('div', { shouldForwardProp: (prop) => prop !== 'open' })(
  ({ theme, open }: { theme: any; open: boolean }) => ({
    flexGrow: 1,
    overflow: 'hidden',
    paddingLeft: theme.spacing(3),
    paddingRight: theme.spacing(3),
    paddingTop: theme.spacing(8),
    paddingBottom: theme.spacing(8),
    zIndex: 1,
    marginLeft: -380,
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    ...(open && {
      marginLeft: 0,
      transition: theme.transitions.create('margin', {
        easing: theme.transitions.easing.easeOut,
        duration: theme.transitions.duration.enteringScreen,
      }),
    }),
  })
) as React.FC<{ open: boolean; theme: any }>

const Page = () => {
  const rootRef = useRef(null)
  const intersectionId = useAppSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useAppSelector(selectSelectedRoadRegulatorId)
  const token = useAppSelector(selectToken)

  const [group, setGroup] = useState(true)
  const [logs, setLogs] = useState<ReportMetadata[]>([])
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(5)
  const [openFilters, setOpenFilters] = useState(true)
  const [loading, setLoading] = useState(false)
  const [filters, setFilters] = useState({
    query: '',
    endDate: new Date(),
    startDate: new Date(new Date().getTime() - 7 * 24 * 60 * 60 * 1000),
    logLevel: 'ERROR',
    customer: [],
  })
  const [openReportGenerationDialog, setOpenReportGenerationDialog] = useState(false)

  function sortReportByAge(a: ReportMetadata, b: ReportMetadata) {
    if (a.reportGeneratedAt < b.reportGeneratedAt) {
      return -1
    }
    if (a.reportGeneratedAt > b.reportGeneratedAt) {
      return 1
    }
    return 0
  }

  const listReports = async (
    start_timestamp: Date,
    end_timestamp: Date,
    intersectionId: number,
    roadRegulatorId: number
  ) => {
    try {
      setLoading(true)
      let data =
        (await ReportsApi.listReports({
          token: token,
          intersectionId,
          roadRegulatorId,
          startTime: start_timestamp,
          endTime: end_timestamp,
        })) ?? []
      data = data.sort(sortReportByAge)
      setLogs(data)
      setLoading(false)
    } catch (err) {
      console.error(err)
      setLoading(false)
    }
  }

  useEffect(
    () => {
      setLoading(true)
      setTimeout(() => listReports(filters.startDate, filters.endDate, intersectionId, roadRegulatorId), 300)
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [filters, intersectionId]
  )

  const handleChangeGroup = (event) => {
    setGroup(event.target.checked)
  }

  const handleToggleFilters = () => {
    setOpenFilters((prevState) => !prevState)
  }

  const handleChangeFilters = (newFilters) => {
    setFilters(newFilters)
    setPage(0)
  }

  const handleCloseFilters = () => {
    setOpenFilters(false)
  }

  const handlePageChange = (event, newPage) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10))
  }

  // Usually query is done on backend with indexing solutions
  const paginatedLogs = applyPagination(logs, page, rowsPerPage)

  return (
    <>
      <Box
        component="main"
        ref={rootRef}
        sx={{
          backgroundColor: 'background.default',
          display: 'flex',
          flexGrow: 1,
          overflow: 'hidden',
        }}
      >
        <ReportListFilters
          containerRef={rootRef}
          filters={filters}
          onChange={handleChangeFilters}
          onClose={handleCloseFilters}
          open={openFilters}
          loading={loading}
          setOpenReportGenerationDialog={setOpenReportGenerationDialog}
        />
        <LogsListInner open={openFilters} theme={undefined}>
          <Box sx={{ mb: 3 }}>
            <Stack spacing={3} maxWidth="sm">
              <Typography noWrap variant="h4" color="text.secondary">
                Reports
              </Typography>
              <Box>
                <Button
                  endIcon={<FilterAlt fontSize="small" />}
                  onClick={handleToggleFilters}
                  variant="outlined"
                  fullWidth={false}
                  size="small"
                >
                  Filters
                </Button>
              </Box>
            </Stack>
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'flex-end',
                mt: 3,
              }}
            ></Box>
          </Box>
          <ReportListTable
            group={group}
            reports={paginatedLogs}
            reportsCount={logs.length}
            onPageChange={handlePageChange}
            onRowsPerPageChange={handleRowsPerPageChange}
            page={page}
            rowsPerPage={rowsPerPage}
          />
        </LogsListInner>
      </Box>
      <ReportGenerationDialog
        open={openReportGenerationDialog}
        onClose={() => {
          setOpenReportGenerationDialog(false)
        }}
      />
    </>
  )
}

export default Page
