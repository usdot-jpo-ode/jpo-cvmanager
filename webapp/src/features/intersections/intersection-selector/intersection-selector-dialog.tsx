import { Dialog, DialogTitle, Container, DialogActions, Button } from '@mui/material'
import IntersectionMap from './intersection-selection-map'
import { useSelector } from 'react-redux'
import {
  selectSelectedIntersectionId,
  selectSelectedRoadRegulatorId,
  setSelectedIntersectionId,
} from '../../../generalSlices/intersectionSlice'
import React from 'react'

type Props = {
  onClose: () => void
  open: boolean
  intersections: IntersectionReferenceData[]
}

const MapDialog = (props: Props) => {
  const { onClose, intersections, open } = props

  const intersectionId = useSelector(selectSelectedIntersectionId)

  const handleClose = () => {
    onClose()
  }

  return (
    <>
      <Dialog onClose={handleClose} open={open} fullWidth maxWidth={'lg'}>
        <DialogTitle>Select Intersection</DialogTitle>
        <Container sx={{ height: '60vh' }}>
          <IntersectionMap
            intersections={intersections}
            selectedIntersection={intersections.find((e) => e.intersectionID == intersectionId)}
            onSelectIntersection={(intersectionId, _) => setSelectedIntersectionId(intersectionId)}
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

export default MapDialog
