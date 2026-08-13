import { Checkbox, Grid2, TextField, Typography, useTheme } from '@mui/material'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import { selectShowPopupOnHover, setShowPopupOnHover, selectBsmTrailLength, setBsmTrailLength } from './map-slice'
import { selectSignalStateLayerStyle, setSignalLayerLayout } from './map-layer-style-slice'

import MapFabTab from './map-fab-tab'
import { useEffect, useState } from 'react'
import { SettingsOutlined } from '@mui/icons-material'
import { getNumber } from './control-panel'

type VisualSettingsProps = {
  openPanel: string
  setOpenPanel: (panel: string) => void
}

function VisualSettings(props: VisualSettingsProps) {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()
  const theme = useTheme()

  const signalStateLayerStyle = useSelector(selectSignalStateLayerStyle)
  const showPopupOnHover = useSelector(selectShowPopupOnHover)
  const bsmTrailLength = useSelector(selectBsmTrailLength)

  const [bsmTrailLengthLocal, setBsmTrailLengthLocal] = useState<string | undefined>(bsmTrailLength.toString())

  useEffect(() => {
    setBsmTrailLengthLocal(bsmTrailLength.toString())
  }, [bsmTrailLength])

  useEffect(() => {
    if (getNumber(bsmTrailLengthLocal) !== null && getNumber(bsmTrailLengthLocal) !== bsmTrailLength) {
      dispatch(setBsmTrailLength(getNumber(bsmTrailLengthLocal)!))
    }
  }, [bsmTrailLengthLocal])

  const content = (
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
  )

  return (
    <MapFabTab
      title="Visual Settings"
      width={600}
      right={theme.spacing(10)}
      panelId="visual-settings"
      openPanel={props.openPanel}
      setOpenPanel={props.setOpenPanel}
      icon={<SettingsOutlined />}
      content={content}
    />
  )
}

export default VisualSettings
