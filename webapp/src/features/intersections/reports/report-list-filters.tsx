import React, { useState, useEffect } from 'react'
import PropTypes from 'prop-types'
import { Box, Button, CircularProgress, Drawer, IconButton, Stack, TextField, Typography } from '@mui/material'
import { styled } from '@mui/material/styles'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import { Close } from '@mui/icons-material'
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'

import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'

const FiltersDrawerDesktop = styled(Drawer)({
  flexShrink: 0,
  width: 380,
  '& .MuiDrawer-paper': {
    position: 'relative',
    width: 380,
  },
})

const FiltersDrawerMobile = styled(Drawer)({
  maxWidth: '100%',
  width: 380,
  '& .MuiDrawer-paper': {
    height: 'calc(100% - 64px)',
    maxWidth: '100%',
    top: 64,
    width: 380,
  },
})

export type ReportListFilter = {
  startDate: Date | null
  endDate: Date | null
}

interface ReportListFiltersProps {
  filters: ReportListFilter
  onChange: (filters: ReportListFilter) => void
  onClose: () => void
  open: boolean
  loading: boolean
  containerRef: any
  setOpenReportGenerationDialog: (open: boolean) => void
}

export const ReportListFilters = (props: ReportListFiltersProps) => {
  const { containerRef, filters, onChange, onClose, open, loading, setOpenReportGenerationDialog, ...other } = props
  const [currentFilters, setCurrentFilters] = useState(filters)
  const [filtersValid, setFiltersValid] = useState([true, ''])

  useEffect(() => {
    updateFiltersValid()
  }, [currentFilters])

  const startDateChange = (date: Date | null) => {
    setCurrentFilters({ ...currentFilters, startDate: date })
  }

  const endDateChange = (date: Date | null) => {
    setCurrentFilters({ ...currentFilters, endDate: date })
  }

  const updateFilters = () => {
    onChange?.(currentFilters)
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

  const content = (
    <Box
      sx={{
        pb: 1,
        pt: 1,
        px: 3,
      }}
    >
      <Box
        sx={{
          mb: 2,
        }}
      >
        <IconButton onClick={onClose}>
          <Close fontSize="small" />
        </IconButton>
      </Box>
      <Typography color="textSecondary" sx={{ mt: 3 }} variant="subtitle2">
        Issue date
      </Typography>
      <Stack spacing={2} sx={{ mt: 2 }}>
        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <DateTimePicker label="From" onChange={startDateChange} value={currentFilters.startDate} disabled={loading} />
          <DateTimePicker label="To" onChange={endDateChange} value={currentFilters.endDate} disabled={loading} />
        </LocalizationProvider>
      </Stack>
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
        onClick={() => {
          setOpenReportGenerationDialog(true)
        }}
      >
        Generate Manual Report
      </Button>
    </Box>
  )

  return (
    <FiltersDrawerDesktop
      anchor="left"
      open={open}
      SlideProps={{ container: containerRef?.current }}
      variant="persistent"
      {...other}
    >
      {content}
    </FiltersDrawerDesktop>
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
