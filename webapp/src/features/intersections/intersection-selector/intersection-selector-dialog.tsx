import { Dialog, DialogTitle, Container, DialogActions, Button } from '@mui/material'
import IntersectionMap from './intersection-selection-map'
import React from 'react'

type Props = {
  onClose: () => void
  open: boolean
}

const MapDialog = (props: Props) => {
  const { onClose, open } = props

  const handleClose = () => {
    onClose()
  }

  return (
    <>
      <Dialog onClose={handleClose} open={open} fullWidth maxWidth={'lg'}>
        <DialogTitle>Select Intersection</DialogTitle>
        <Container sx={{ height: '60vh' }}>
          <IntersectionMap />
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

export default MapDialog
