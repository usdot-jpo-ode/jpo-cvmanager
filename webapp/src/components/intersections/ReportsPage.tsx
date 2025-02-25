import React, { useState, useEffect, useRef } from 'react'
import { Box, Container, Grid2, Typography } from '@mui/material'
import { styled, useTheme } from '@mui/material/styles'
import { ReportListFilters } from '../../features/intersections/reports/report-list-filters'
import { ReportListTable } from '../../features/intersections/reports/report-list-table'
import ReportsApi, { ReportMetadata } from '../../apis/intersections/reports-api'
import { ReportGenerationDialog } from '../../features/intersections/reports/report-generation-dialog'
import { selectSelectedIntersectionId, selectSelectedRoadRegulatorId } from '../../generalSlices/intersectionSlice'
import { selectToken } from '../../generalSlices/userSlice'
import { useSelector } from 'react-redux'

const applyPagination = (logs, page, rowsPerPage) => logs.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

const Page = () => {
  const rootRef = useRef(null)
  const intersectionId = useSelector(selectSelectedIntersectionId)
  const roadRegulatorId = useSelector(selectSelectedRoadRegulatorId)
  const token = useSelector(selectToken)
  const theme = useTheme()

  const [group, setGroup] = useState(true)
  const [logs, setLogs] = useState<ReportMetadata[]>([])
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(5)
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

  // Usually query is done on backend with indexing solutions
  const paginatedLogs = applyPagination(logs, page, rowsPerPage)

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
          mt: 11,
          mb: 1,
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
            group={group}
            reports={paginatedLogs}
            reportsCount={logs.length}
            onPageChange={handlePageChange}
            onRowsPerPageChange={handleRowsPerPageChange}
            page={page}
            rowsPerPage={rowsPerPage}
          />
        </Grid2>
      </Grid2>
      <ReportGenerationDialog
        open={openReportGenerationDialog}
        onClose={() => {
          setOpenReportGenerationDialog(false)
        }}
      />
    </Box>
  )
}

export default Page
