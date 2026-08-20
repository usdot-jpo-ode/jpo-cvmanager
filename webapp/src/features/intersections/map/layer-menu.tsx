import { Box, Checkbox, IconButton, Typography, useTheme, Tooltip } from '@mui/material'
import { useDispatch, useSelector } from 'react-redux'
import { AnyAction, ThunkDispatch } from '@reduxjs/toolkit'
import { RootState } from '../../../store'
import MapFabTab from './map-fab-tab'
import { selectLayersVisible, MAP_LAYERS, setLayerVisibility } from './map-slice'
import { Layers, Label } from '@mui/icons-material'

type LayerMenuProps = {
  openPanel: string
  setOpenPanel: (panel: string) => void
}

type LayerConfig = {
  label: string
  layerId: MAP_LAYERS
  labelLayerId?: MAP_LAYERS
}

// Layer configurations - order matches LAYER_RENDER_ORDER from map-layer-style-slice
const layerConfigs: LayerConfig[] = [
  { label: 'SRM Requested Lanes', layerId: 'srm-requested-lanes' },
  { label: 'SSM Highlighted Lanes', layerId: 'ssm-connection-highlight' },
  { label: 'Map Lanes', layerId: 'map-message', labelLayerId: 'map-message-labels' },
  { label: 'Connecting Lanes', layerId: 'connecting-lanes', labelLayerId: 'connecting-lanes-labels' },
  { label: 'Invalid Lane Collections', layerId: 'invalid-lane-collection' },
  { label: 'Signal States', layerId: 'signal-states' },
  { label: 'SSM Connection Status', layerId: 'ssm-connection-status' },
  { label: 'BSMs', layerId: 'bsm' },
  { label: 'SRMs', layerId: 'srm' },
]

function LayerMenu(props: LayerMenuProps) {
  const dispatch: ThunkDispatch<RootState, void, AnyAction> = useDispatch()

  const layersVisible = useSelector(selectLayersVisible)

  const theme = useTheme()

  const layerRow = (config: LayerConfig, index: number) => {
    const { label, layerId, labelLayerId } = config
    const isLayerVisible = layersVisible[layerId]
    const isLabelVisible = labelLayerId ? layersVisible[labelLayerId] : undefined

    return (
      <Box
        key={layerId}
        sx={{
          display: 'flex',
          flexDirection: 'row',
          alignItems: 'center',
          gap: 1,
          justifyContent: 'space-between',
          '&:hover': {
            backgroundColor: theme.palette.action.hover,
          },
          px: 1,
          py: 0.5,
          borderRadius: 1,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flex: 1 }}>
          <Checkbox
            onChange={(event) => {
              dispatch(setLayerVisibility({ key: layerId, visible: event.target.checked }))
            }}
            checked={isLayerVisible}
            size="small"
          />
          <Typography fontSize="16px">{label}</Typography>
        </Box>
        {labelLayerId && (
          <Tooltip title={isLabelVisible ? 'Hide Labels' : 'Show Labels'}>
            <IconButton
              size="small"
              onClick={() => {
                dispatch(setLayerVisibility({ key: labelLayerId, visible: !isLabelVisible }))
              }}
              sx={{
                color: isLabelVisible ? theme.palette.primary.main : theme.palette.action.disabled,
                '&:hover': {
                  backgroundColor: theme.palette.action.hover,
                },
              }}
            >
              <Label fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
      </Box>
    )
  }

  return (
    <MapFabTab
      title="Map Layers"
      width={300}
      right={theme.spacing(24)}
      panelId="layer-menu"
      openPanel={props.openPanel}
      setOpenPanel={props.setOpenPanel}
      icon={<Layers />}
      content={<Box>{layerConfigs.map((config, index) => layerRow(config, index))}</Box>}
    />
  )
}

export default LayerMenu
