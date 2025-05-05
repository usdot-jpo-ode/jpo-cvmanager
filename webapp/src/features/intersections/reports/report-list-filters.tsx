import React, { useState, useEffect } from 'react'
import PropTypes from 'prop-types'
import { Box, Button, CircularProgress, Drawer, IconButton, Stack, Typography } from '@mui/material'
import { styled } from '@mui/material/styles'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { Close } from '@mui/icons-material'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'

import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import dayjs from 'dayjs'

export type ReportListFilter = {
  startDate: Date | null
  endDate: Date | null
}

interface ReportListFiltersProps {
  filters: ReportListFilter
  onChange: (filters: ReportListFilter) => void
  loading: boolean
  containerRef: any
  setOpenReportGenerationDialog: (open: boolean) => void
}

export const ReportListFilters = (props: ReportListFiltersProps) => {
  const { containerRef, filters, onChange, loading, setOpenReportGenerationDialog, ...other } = props
  const [currentFilters, setCurrentFilters] = useState(filters)
  const [filtersValid, setFiltersValid] = useState([true, ''])

  useEffect(() => {
    setCurrentFilters(filters)
  }, [filters])

  useEffect(() => {
    updateFiltersValid()
  }, [currentFilters])

  const startDateChange = (date: Date | null) => {
    setCurrentFilters({ ...currentFilters, startDate: date })
  }

  const endDateChange = (date: Date | null) => {
    setCurrentFilters({ ...currentFilters, endDate: date })
  }

  const updateFiltersValid = () => {
    let filtersValidLocal = true
    const reasons: string[] = []
    if (currentFilters.startDate === null || currentFilters.endDate === null) {
      filtersValidLocal = false
      reasons.push('Start Date and End Date must be set')
    } else if (currentFilters.startDate >= currentFilters.endDate) {
      filtersValidLocal = false
      reasons.push('Start Date must be before End Date')
    }
    // TODO: validate dates
    setFiltersValid([filtersValidLocal, reasons.join('\n')])
    if (filtersValidLocal) {
      onChange?.(currentFilters)
    }
  }

  return (
    <Box
      sx={{
        pb: 3,
        px: 3,
      }}
    >
      <Box sx={{ my: 2 }}>
        <LocalizationProvider dateAdapter={AdapterDayjs}>
          <DateTimePicker
            label="From"
            onChange={(e) => startDateChange(e?.toDate())}
            value={dayjs(currentFilters.startDate)}
            disabled={loading}
          />
          <DateTimePicker
            label="To"
            onChange={(e) => endDateChange(e?.toDate())}
            value={dayjs(currentFilters.endDate)}
            disabled={loading}
            sx={{
              ml: 3,
            }}
          />
        </LocalizationProvider>
      </Box>
      {!filtersValid[0] && (
        <Typography color="red" sx={{ mt: 3 }} variant="subtitle2">
          Invalid filters: {filtersValid[1]}
        </Typography>
      )}
      <Box sx={{ m: 1, position: 'relative', display: 'flex', alignItems: 'center' }}>
        {loading && (
          <CircularProgress
            size={24}
            sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              marginTop: '-12px',
              marginLeft: '-12px',
            }}
          />
        )}
      </Box>
      <Button
        component="a"
        variant="contained"
        onClick={() => {
          setOpenReportGenerationDialog(true)
        }}
      >
        Generate Manual Report
      </Button>
    </Box>
  )
}

ReportListFilters.propTypes = {
  containerRef: PropTypes.any,
  filters: PropTypes.object,
  onChange: PropTypes.func,
  onClose: PropTypes.func,
  open: PropTypes.bool,
  loading: PropTypes.bool,
}
