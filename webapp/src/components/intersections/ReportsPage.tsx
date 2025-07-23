import React, { useState, useEffect, useRef } from 'react'
import { Box, Grid2, Typography } from '@mui/material'
import { StyledEngineProvider, ThemeProvider, useTheme } from '@mui/material/styles'
import { ReportListFilters } from '../../features/intersections/reports/report-list-filters'
import { ReportListTable } from '../../features/intersections/reports/report-list-table'
import ReportsApi, { ReportMetadata } from '../../apis/intersections/reports-api'
import { ReportGenerationDialog } from '../../features/intersections/reports/report-generation-dialog'
import { selectSelectedIntersectionId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'
import ReportDetailsModal from '../../features/intersections/reports/report-details-modal'
import { ReportTheme } from '../../styles/report-theme'

const applyPagination = (logs, page, rowsPerPage) => logs.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
const WEEK_IN_MILLISECONDS = 7 * 24 * 60 * 60 * 1000

const Page = () => {
  const rootRef = useRef(null)
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const token = useSelector(selectToken)
  const theme = useTheme()

  const [logs, setLogs] = useState<ReportMetadata[]>([])
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(5)
  const [loading, setLoading] = useState(false)
  const [filters, setFilters] = useState({
    query: '',
    endDate: new Date(),
    startDate: new Date(new Date().getTime() - WEEK_IN_MILLISECONDS),
    logLevel: 'ERROR',
    customer: [],
  })
  const [openReportGenerationDialog, setOpenReportGenerationDialog] = useState(false)

  // Sort reports by age, newest first
  function sortReportByAge(a: ReportMetadata, b: ReportMetadata) {
    if (a.reportGeneratedAt < b.reportGeneratedAt) {
      return 1
    }
    if (a.reportGeneratedAt > b.reportGeneratedAt) {
      return -1
    }
    return 0
  }

  const listReports = async (start_timestamp: Date, end_timestamp: Date, intersectionId: number) => {
    try {
      setLoading(true)
      let data =
        (await ReportsApi.listReports({
          token: token,
          intersectionId,
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

  useEffect(() => {
    setLoading(true)
    setTimeout(() => listReports(filters.startDate, filters.endDate, intersectionId), 300)
  }, [filters, intersectionId])

  const handleChangeFilters = (newFilters) => {
    setFilters(newFilters)
    setPage(0)
  }

  const handlePageChange = (event, newPage) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10))
  }

  const refreshReportData = () => {
    setFilters({
      ...filters,
      startDate: new Date(new Date().getTime() - WEEK_IN_MILLISECONDS),
      endDate: new Date(),
    })
  }

  // Usually query is done on backend with indexing solutions
  const paginatedLogs = applyPagination(logs, page, rowsPerPage)

  // Inside the parent component
  const [selectedReport, setSelectedReport] = useState<ReportMetadata | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  const handleViewReport = (report: ReportMetadata) => {
    setSelectedReport(report)
    setIsModalOpen(true)
  }

  const handleCloseReportModal = () => {
    setIsModalOpen(false)
    setSelectedReport(null)
  }

  const handleReportGenerated = () => {
    setOpenReportGenerationDialog(false)
    refreshReportData()
  }

  return (
    <Box>
      <Grid2
        container
        component="main"
        ref={rootRef}
        sx={{
          backgroundColor: theme.palette.background.paper,
          display: 'flex',
          flexGrow: 1,
          overflow: 'hidden',
        }}
        justifyContent="flex-start"
      >
        <Grid2 size={12}>
          <Typography variant="h6" sx={{ m: 2 }}>
            Generate Report
          </Typography>
        </Grid2>
        <Grid2 size={12}>
          <ReportListFilters
            containerRef={rootRef}
            filters={filters}
            onChange={handleChangeFilters}
            loading={loading}
            setOpenReportGenerationDialog={setOpenReportGenerationDialog}
          />
        </Grid2>
        <Grid2 size={12} sx={{ my: 3 }}>
          <ReportListTable
            reports={paginatedLogs}
            reportsCount={logs.length}
            onPageChange={handlePageChange}
            onRowsPerPageChange={handleRowsPerPageChange}
            page={page}
            rowsPerPage={rowsPerPage}
            onViewReport={handleViewReport}
          />
        </Grid2>
      </Grid2>
      <ReportGenerationDialog
        open={openReportGenerationDialog}
        onClose={() => {
          setOpenReportGenerationDialog(false)
        }}
        onReportGenerated={handleReportGenerated}
      />
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={ReportTheme}>
          <ReportDetailsModal open={isModalOpen} onClose={handleCloseReportModal} report={selectedReport} />
        </ThemeProvider>
      </StyledEngineProvider>
    </Box>
  )
}

export default Page
