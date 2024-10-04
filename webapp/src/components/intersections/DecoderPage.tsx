import React, { useEffect, useState } from 'react'
import { Box, Container, Grid, Typography } from '@mui/material'
import DecoderApi from '../../apis/intersections/decoder-api'
import { DecoderTables } from '../../features/intersections/decoder/decoder-tables'
import { v4 as uuidv4 } from 'uuid'
import MapTab, { getTimestamp } from '../../features/intersections/map/map-component'
import { selectToken } from '../../generalSlices/userSlice'
import { useDispatch, useSelector } from 'react-redux'
import { centerMapOnPoint } from '../../features/intersections/map/map-slice'
import { ThunkDispatch, AnyAction } from '@reduxjs/toolkit'
import { RootState } from '../../store'
import {
  selectData,
  selectSelectedBsms,
  selectSelectedMapMessage,
  selectCurrentBsms,
  initializeData,
  updateCurrentBsms,
  setAsn1DecoderDialogOpen,
} from '../../features/intersections/decoder/asn1-decoder-slice'
import DecoderEntryDialog from '../../features/intersections/decoder/decoder-entry-dialog'
import { Button } from 'react-bootstrap'

const DecoderPage = () => {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const data = useSelector(selectData)
  const selectedMapMessage = useSelector(selectSelectedMapMessage)
  const selectedBsms = useSelector(selectSelectedBsms)
  const currentBsms = useSelector(selectCurrentBsms)

  useEffect(() => {
    dispatch(initializeData())
  }, [])

  const getIntersectionId = (decodedResponse: DecoderApiResponseGeneric | undefined): number | undefined => {
    if (!decodedResponse) {
      return undefined
    }

    switch (decodedResponse.type) {
      case 'MAP':
        const mapPayload = decodedResponse.processedMap
        return mapPayload?.properties?.intersectionId
      case 'SPAT':
        const spatPayload = decodedResponse.processedSpat
        return spatPayload?.intersectionId
      default:
        return undefined
    }
  }

  const isGreyedOut = (intersectionId: number | undefined) => {
    return selectedMapMessage?.intersectionId === undefined || intersectionId !== selectedMapMessage?.intersectionId
  }

  useEffect(() => {
    console.log('Data or selected BSMs changed')
    dispatch(updateCurrentBsms(Object.values(data)))
  }, [data, selectedBsms])

  return (
    <>
      <Box
        component="main"
        sx={{
          backgroundColor: 'background.default',
          flexGrow: 1,
          py: 4,
        }}
      >
        <Container maxWidth={false} style={{ minWidth: '1000px' }}>
          <Box
            sx={{
              alignItems: 'center',
              display: 'flex',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              m: -1,
            }}
          >
            <Grid container justifyContent="space-between" spacing={3}>
              <Grid item>
                <Typography sx={{ m: 1 }} variant="h4" color="text.secondary">
                  ASN.1 Decoder
                </Typography>
              </Grid>
            </Grid>
          </Box>
          <Box
            sx={{
              alignItems: 'center',
              display: 'flex',
              overflow: 'hidden',
              height: '80vh',
            }}
          >
            <MapTab
              sourceData={{
                map: Object.values(data)
                  .filter((v) => v.type === 'MAP' && v.status == 'COMPLETED' && v.id == selectedMapMessage?.id)
                  .map((v) => v.decodedResponse?.processedMap!),
                spat: Object.values(data)
                  .filter(
                    (v) =>
                      v.type === 'SPAT' && v.status == 'COMPLETED' && !isGreyedOut(getIntersectionId(v.decodedResponse))
                  )
                  .map((v) => v.decodedResponse?.processedSpat!),
                bsm: currentBsms,
              }}
              sourceDataType={undefined}
              intersectionId={-1}
              roadRegulatorId={-1}
            />
          </Box>
        </Container>
      </Box>
      <DecoderEntryDialog />
    </>
  )
}

export default DecoderPage
