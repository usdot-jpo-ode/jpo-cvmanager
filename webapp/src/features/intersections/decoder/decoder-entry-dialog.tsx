import { Dialog, DialogTitle, Container, DialogActions, Button, Typography } from '@mui/material'
import React, { useEffect } from 'react'
import { DecoderTables } from './decoder-tables'
import {
  setAsn1DecoderDialogOpen,
  selectDialogOpen,
  updateCurrentBsms,
  selectData,
  selectSelectedBsms,
  updateAllDataOnMap,
  selectSelectedMapMessage,
} from './asn1-decoder-slice'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'

const DecoderEntryDialog = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const open = useSelector(selectDialogOpen)
  const data = useSelector(selectData)
  const selectedBsms = useSelector(selectSelectedBsms)
  const selectedMapMessage = useSelector(selectSelectedMapMessage)

  const handleClose = () => {
    dispatch(setAsn1DecoderDialogOpen(false))
  }

  useEffect(() => {
    dispatch(updateCurrentBsms(Object.values(data)))

    dispatch(updateAllDataOnMap())
  }, [data, selectedBsms])

  useEffect(() => {
    dispatch(updateAllDataOnMap())
  }, [selectedMapMessage])

  return (
    <>
      <Dialog onClose={handleClose} open={open} fullWidth maxWidth={'lg'}>
        <DialogTitle>Select Intersection</DialogTitle>

        <Typography sx={{ m: 1 }} variant="h6" color="white">
          1. Upload data, either by uploading individual files or pasting the data directly into the text box.
          <br />
          2. Select an uploaded MAP message to view the decoded data. SPAT data is filtered by intersection ID.
          <br />
          3. Select BSM messages to view, all selected BSM data is shown, regardless of the time slider.
        </Typography>
        <Container sx={{ height: '60vh' }}>
          <DecoderTables />
        </Container>
        <DialogActions>
          <Button autoFocus onClick={handleClose} variant="contained">
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default DecoderEntryDialog
