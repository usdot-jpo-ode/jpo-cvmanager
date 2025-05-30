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
  onItemSelected,
} from './asn1-decoder-slice'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { selectDecoderModeEnabled } from '../map/map-slice'

const DecoderEntryDialog = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const open = useSelector(selectDialogOpen)
  const data = useSelector(selectData)
  const selectedBsms = useSelector(selectSelectedBsms)
  const selectedMapMessage = useSelector(selectSelectedMapMessage)
  const decoderModeEnabled = useSelector(selectDecoderModeEnabled)

  const handleClose = () => {
    dispatch(setAsn1DecoderDialogOpen(false))
  }

  useEffect(() => {
    if (decoderModeEnabled) {
      if (Object.values(data).filter((v) => v.type === 'MAP').length !== 0 && selectedMapMessage === undefined) {
        dispatch(onItemSelected(Object.values(data).filter((v) => v.type === 'MAP')[0].id))
      }
      dispatch(updateCurrentBsms(Object.values(data)))
      dispatch(updateAllDataOnMap())
    }
  }, [data, selectedBsms, decoderModeEnabled])

  useEffect(() => {
    dispatch(updateAllDataOnMap())
  }, [selectedMapMessage])

  return (
    <>
      <Dialog onClose={handleClose} open={open} fullWidth maxWidth={'lg'}>
        <DialogTitle sx={{ padding: '8px' }}>
          <Typography fontSize="16px">Select Intersection</Typography>
        </DialogTitle>

        <Typography sx={{ m: 1 }} fontSize="16px" color="white">
          1. Upload data, either by uploading individual files or pasting the data directly into the text box.
          <br />
          2. Select an uploaded MAP message to view the decoded data. SPaT data is filtered by intersection ID. SPaT
          data can only be viewed with a corresponding MAP message.
          <br />
          3. Select BSM messages to view, all selected BSM data is shown, regardless of the time slider.
        </Typography>
        <Container sx={{ height: 'fit-content' }}>
          <DecoderTables />
        </Container>
        <DialogActions>
          <Button autoFocus onClick={handleClose} variant="contained" className="capital-case">
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default DecoderEntryDialog
