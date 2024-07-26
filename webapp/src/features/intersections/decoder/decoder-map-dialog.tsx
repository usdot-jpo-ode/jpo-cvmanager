import React from 'react'
import { Dialog, DialogTitle, Container, DialogActions, Button } from '@mui/material'
import MapTab from '../map/map-component'

type Props = {
  onClose: () => void
  open: boolean
  intersectionId: number
  roadRegulatorId: number
  map: ProcessedMap[]
  spat: ProcessedSpat[]
  bsm: OdeBsmData[]
}

export const DecoderMapDialog = (props: Props) => {
  const { onClose, open, intersectionId, roadRegulatorId, map, spat, bsm } = props

  const handleClose = () => {
    onClose()
  }

  console.log('Map Dialog', map, spat, bsm)

  return (
    <>
      <Dialog onClose={handleClose} open={open} fullWidth maxWidth={'lg'}>
        <DialogTitle>View Decoded Data</DialogTitle>
        <Container sx={{ height: '60vh' }}>
          <MapTab
            sourceData={{
              map,
              spat,
              bsm,
            }}
            sourceDataType={'exact'}
            intersectionId={intersectionId}
            roadRegulatorId={roadRegulatorId}
          />
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
