import React from 'react'
import { Dialog, DialogTitle, Container, DialogActions, Button } from '@mui/material'

import { ReportRequestEditForm } from './report-request-edit-form'
import ReportsApi from '../../../apis/intersections/reports-api'
import toast from 'react-hot-toast'
import { selectToken } from '../../../generalSlices/userSlice'
import { selectSelectedIntersectionId } from '../../../generalSlices/intersectionSlice'
import { useSelector } from 'react-redux'

const FIFTEEN_MINUTES_IN_MILLISECONDS = 7 * 60 * 1000

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
    roadRegulatorId,
    startTime,
    endTime,
  }: {
    intersectionId?: number
    roadRegulatorId?: number
    startTime: Date
    endTime: Date
  }) => {
    if (!intersectionId || !roadRegulatorId) {
      console.error(
        'Did not attempt to generate report. Intersection ID:',
        intersectionId,
        'Road Regulator ID:',
        roadRegulatorId
      )
      return
    }
    const promise = ReportsApi.generateReport({
      token: token,
      intersectionId,
      roadRegulatorId,
      startTime,
      endTime,
    })

    const refreshTime = new Date(Date.now() + FIFTEEN_MINUTES_IN_MILLISECONDS)
    toast.promise(promise, {
      loading: 'Submitting Performance Report Request',
      success: `Successfully Submitted Performance Report Request!\nReports usually take 10-15 minutes to generate - please refresh the page at ${refreshTime.toLocaleTimeString()} to see the new report.`,
      error: 'Error Submitting Performance Report Request',
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
          <Button autoFocus onClick={handleClose}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
