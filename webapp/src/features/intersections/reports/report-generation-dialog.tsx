import React from 'react'
import { Dialog, DialogTitle, Container, DialogActions, Button } from '@mui/material'

import { ReportRequestEditForm } from './report-request-edit-form'
import ReportsApi from '../../../apis/intersections/reports-api'
import toast from 'react-hot-toast'
import { selectToken } from '../../../generalSlices/userSlice'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { useSelector } from 'react-redux'

type ReportGenerationDialogProps = {
  onClose: () => void
  open: boolean
  onReportGenerated: () => void
}

export const ReportGenerationDialog = (props: ReportGenerationDialogProps) => {
  const token = useSelector(selectToken)
  const intersectionId = useSelector(selectSelectedIntersectionId)

  const { onClose, open, onReportGenerated } = props

  const handleClose = () => {
    onClose()
  }

  const getReport = async ({
    intersectionId,
    startTime,
    endTime,
  }: {
    intersectionId?: number
    startTime: Date
    endTime: Date
  }) => {
    if (!intersectionId) {
      console.error('Did not attempt to generate report. Intersection ID:', intersectionId)
      return
    }
    const promise = ReportsApi.generateReport({
      token: token,
      intersectionId,
      startTime,
      endTime,
    })

    toast.promise(promise, {
      loading: `Generating Performance Report - this may take up to 15 minutes (started at ${new Date().toLocaleTimeString()})`,
      success: `Successfully Generated Performance Report!`,
      error: 'Error Generating Performance Report',
    })
    await promise
    onReportGenerated()
  }

  return (
    <>
      <Dialog onClose={handleClose} open={open} fullWidth maxWidth={'lg'}>
        <DialogTitle>Generate Performance Report</DialogTitle>
        <Container>
          <ReportRequestEditForm onGenerateReport={getReport} dbIntersectionId={intersectionId} />
        </Container>
        <DialogActions>
          <Button autoFocus onClick={handleClose} className="capital-case">
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
