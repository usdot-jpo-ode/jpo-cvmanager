import { Box, Checkbox, Fab, Grid2, IconButton, Paper, TextField, Typography, useTheme } from '@mui/material'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import {
  selectLaneLabelsVisible,
  selectShowPopupOnHover,
  selectSigGroupLabelsVisible,
  setLaneLabelsVisible,
  setSigGroupLabelsVisible,
  setShowPopupOnHover,
  selectBsmTrailLength,
  setBsmTrailLength,
} from './map-slice'
import { selectSignalStateLayerStyle, setSignalLayerLayout } from './map-layer-style-slice'

import { useEffect, useState } from 'react'
import React from 'react'
import { Close, SettingsOutlined } from '@mui/icons-material'
import { getNumber } from './control-panel'

type VisualSettingsProps = {
  openPanel: string
  setOpenPanel: (panel: string) => void
}

function VisualSettings(props: VisualSettingsProps) {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const signalStateLayerStyle = useSelector(selectSignalStateLayerStyle)
  const laneLabelsVisible = useSelector(selectLaneLabelsVisible)
  const showPopupOnHover = useSelector(selectShowPopupOnHover)
  const sigGroupLabelsVisible = useSelector(selectSigGroupLabelsVisible)
  const bsmTrailLength = useSelector(selectBsmTrailLength)

  const theme = useTheme()

  const toggleOpen = () => {
    if (props.openPanel === 'visual-settings') {
      props.setOpenPanel('')
    } else {
      props.setOpenPanel('visual-settings')
    }
  }
  const [bsmTrailLengthLocal, setBsmTrailLengthLocal] = useState<string | undefined>(bsmTrailLength.toString())

  useEffect(() => {
    setBsmTrailLengthLocal(bsmTrailLength.toString())
  }, [bsmTrailLength])

  useEffect(() => {
    if (getNumber(bsmTrailLengthLocal) !== null && getNumber(bsmTrailLengthLocal) !== bsmTrailLength) {
      dispatch(setBsmTrailLength(getNumber(bsmTrailLengthLocal)!))
    }
  }, [bsmTrailLengthLocal])

  return (
    <>
      <Fab
        size="small"
        onClick={() => {
          toggleOpen()
        }}
        sx={{
          position: 'absolute',
          zIndex: 10,
          top: theme.spacing(3),
          right: theme.spacing(17),
          backgroundColor: theme.palette.background.paper,
          '&:hover': {
            backgroundColor: theme.palette.custom.intersectionMapButtonHover,
          },
        }}
      >
        <SettingsOutlined />
      </Fab>
      <div
        style={{
          position: 'absolute',
          zIndex: 10,
          bottom: theme.spacing(3),
          maxHeight: 'calc(100vh - 240px)',
          right: 0,
          width: props.openPanel === 'visual-settings' ? 600 : 0,
          fontSize: '16px',
          overflow: 'auto',
          scrollBehavior: 'auto',
          borderRadius: '4px',
        }}
      >
        <Box style={{ position: 'relative', height: '100%', width: '100%' }}>
          <Paper sx={{ height: '100%', width: '100%', px: 2, pb: 2 }} square>
            <Box>
              {props.openPanel !== 'visual-settings' ? null : (
                <>
                  <Box
                    sx={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '8px 16px',
                    }}
                  >
                    <Typography fontSize="16px">Visual Settings</Typography>
                    <IconButton
                      onClick={() => {
                        toggleOpen()
                      }}
                    >
                      <Close color="info" />
                    </IconButton>
                  </Box>
                  <div>
                    <Grid2 container spacing={1} justifyContent="flex-start">
                      <Grid2 size={6} display="flex" flexDirection="row" alignItems="center">
                        <Checkbox
                          checked={signalStateLayerStyle?.layout?.['icon-rotation-alignment'] === 'map'}
                          onChange={(event) =>
                            dispatch(
                              setSignalLayerLayout({
                                ...signalStateLayerStyle.layout,
                                'icon-rotation-alignment': event.target.checked ? 'map' : 'viewport',
                                'icon-rotate': event.target.checked ? ['get', 'orientation'] : 0,
                              })
                            )
                          }
                        />
                        <Typography fontSize="16px">Rotate Signal Head Icons With Map </Typography>
                      </Grid2>
                      <Grid2 size={6} display="flex" flexDirection="row" alignItems="center">
                        <Checkbox
                          checked={laneLabelsVisible}
                          onChange={(event) => dispatch(setLaneLabelsVisible(event.target.checked))}
                        />
                        <Typography fontSize="16px">Show Lane IDs </Typography>
                      </Grid2>
                      <Grid2 size={6} display="flex" flexDirection="row" alignItems="center">
                        <Checkbox
                          checked={sigGroupLabelsVisible}
                          onChange={(event) => dispatch(setSigGroupLabelsVisible(event.target.checked))}
                        />
                        <Typography fontSize="16px">Show Signal Group IDs </Typography>
                      </Grid2>
                      <Grid2 size={6} display="flex" flexDirection="row" alignItems="center">
                        <Checkbox
                          checked={showPopupOnHover}
                          onChange={(event) => dispatch(setShowPopupOnHover(event.target.checked))}
                        />
                        <Typography fontSize="16px">Show Popup on Hover </Typography>
                      </Grid2>
                      <Grid2 size={12} display="flex" flexDirection="row">
                        <TextField
                          label="BSM Trail length"
                          name="bsmTrailLength"
                          type="number"
                          sx={{ mt: 1 }}
                          onChange={(e) => {
                            setBsmTrailLengthLocal(e.target.value)
                          }}
                          value={bsmTrailLengthLocal}
                        />
                      </Grid2>
                    </Grid2>
                  </div>
                </>
              )}
            </Box>
          </Paper>
        </Box>
      </div>
    </>
  )
}

export default VisualSettings
