import { AnyAction, createAsyncThunk, createSlice, PayloadAction, ThunkDispatch } from '@reduxjs/toolkit'
import { CircleLayer, LayerProps, LineLayer, SymbolLayer } from 'react-map-gl'
import { CirclePaint, Layer } from 'mapbox-gl'
import { RootState } from '../../store'
import { createDraft } from 'immer'

const mapMessageLayer: LineLayer = {
  id: 'map-message',
  type: 'line',
  paint: {
    'line-width': 5,
    'line-color': ['case', ['==', ['get', 'ingressPath'], true], '#eb34e8', '#0004ff'],
  },
}

const mapMessageLabelsLayer: SymbolLayer = {
  id: 'map-message-labels',
  type: 'symbol',
  layout: {
    'text-field': ['concat', 'lane: ', ['to-string', ['get', 'laneId']]],
    'text-size': 20,
    // "text-offset": [0, 1],
    'text-variable-anchor': ['top', 'left', 'right', 'bottom'],
    'text-allow-overlap': true,
    'icon-allow-overlap': true,
  },
  paint: {
    'text-color': '#000000',
    'text-halo-color': '#ffffff',
    'text-halo-width': 5,
  },
}

const connectingLanesLayer: LineLayer = {
  id: 'connecting-lanes',
  type: 'line',
  paint: {
    'line-width': [
      'match',
      ['get', 'signalState'],
      'UNAVAILABLE',
      3,
      'DARK',
      3,
      'STOP_THEN_PROCEED',
      3,
      'STOP_AND_REMAIN',
      3,
      'PRE_MOVEMENT',
      5,
      'PERMISSIVE_MOVEMENT_ALLOWED',
      5,
      'PROTECTED_MOVEMENT_ALLOWED',
      5,
      'PERMISSIVE_CLEARANCE',
      5,
      'PROTECTED_CLEARANCE',
      5,
      'CAUTION_CONFLICTING_TRAFFIC',
      5,
      5,
    ],
    'line-color': [
      'match',
      ['get', 'signalState'],
      'UNAVAILABLE',
      '#797979',
      'DARK',
      '#3a3a3a',
      'STOP_THEN_PROCEED',
      '#c00000',
      'STOP_AND_REMAIN',
      '#c00000',
      'PRE_MOVEMENT',
      '#c00000',
      'PERMISSIVE_MOVEMENT_ALLOWED',
      '#267700',
      'PROTECTED_MOVEMENT_ALLOWED',
      '#267700',
      'PERMISSIVE_CLEARANCE',
      '#e6b000',
      'PROTECTED_CLEARANCE',
      '#e6b000',
      'CAUTION_CONFLICTING_TRAFFIC',
      '#e6b000',
      '#797979',
    ],
    'line-dasharray': [
      'match',
      ['get', 'signalState'],
      'UNAVAILABLE',
      ['literal', [2, 1]],
      'DARK',
      ['literal', [2, 1]],
      'STOP_THEN_PROCEED',
      ['literal', [2, 1]],
      'STOP_AND_REMAIN',
      ['literal', [1]],
      'PRE_MOVEMENT',
      ['literal', [2, 2]],
      'PERMISSIVE_MOVEMENT_ALLOWED',
      ['literal', [2, 1]],
      'PROTECTED_MOVEMENT_ALLOWED',
      ['literal', [1]],
      'PERMISSIVE_CLEARANCE',
      ['literal', [2, 1]],
      'PROTECTED_CLEARANCE',
      ['literal', [1]],
      'CAUTION_CONFLICTING_TRAFFIC',
      ['literal', [1, 4]],
      ['literal', [2, 1]],
    ],
  },
}

const connectingLanesLabelsLayer: SymbolLayer = {
  id: 'connecting-lanes-labels',
  type: 'symbol',
  layout: {
    'text-field': ['concat', 'sig-group: ', ['to-string', ['get', 'signalGroupId']]],
    'text-size': 20,
    'text-offset': [0, 1],
    'text-variable-anchor': ['top', 'left', 'right', 'bottom'],
    'text-allow-overlap': true,
    'icon-allow-overlap': true,
    'icon-image': 'rounded',
    'icon-text-fit': 'both',
  },
  paint: {
    'text-color': '#000000',
    'text-halo-color': '#ffffff',
    'text-halo-width': 5,
  },
}

const markerLayer: LineLayer = {
  id: 'invalid-lane-collection',
  type: 'line',
  paint: {
    'line-width': 20,
    'line-color': '#d40000',
    // "line-dasharray": [2, 1],
  },
}

const bsmLayerStyle: CircleLayer = {
  id: 'bsm',
  type: 'circle',
  paint: {
    'circle-color': ['match', ['get', 'id'], 'temp-id', '#0004ff', '#0004ff'],
    'circle-radius': 8,
  },
}

const signalStateLayer: SymbolLayer = {
  id: 'signal-states',
  type: 'symbol',
  layout: {
    'icon-image': [
      'match',
      ['get', 'signalState'],
      'UNAVAILABLE',
      'traffic-light-icon-unknown',
      'DARK',
      'traffic-light-icon-unknown',
      'STOP_THEN_PROCEED',
      'traffic-light-icon-red-flashing',
      'STOP_AND_REMAIN',
      'traffic-light-icon-red-1',
      'PRE_MOVEMENT',
      'traffic-light-icon-yellow-red-1',
      'PERMISSIVE_MOVEMENT_ALLOWED',
      'traffic-light-icon-yellow-1',
      'PROTECTED_MOVEMENT_ALLOWED',
      'traffic-light-icon-green-1',
      'PERMISSIVE_CLEARANCE',
      'traffic-light-icon-yellow-1',
      'PROTECTED_CLEARANCE',
      'traffic-light-icon-yellow-1',
      'CAUTION_CONFLICTING_TRAFFIC',
      'traffic-light-icon-yellow-1',
      'traffic-light-icon-unknown',
    ],
    'icon-rotate': ['get', 'orientation'],
    'icon-allow-overlap': true,
    'icon-rotation-alignment': 'map',
    'icon-size': ['interpolate', ['linear'], ['zoom'], 0, 0, 9, 0.01, 19, 0.15, 22, 0.4],
  },
}

export type MAP_LEGEND_COLORS = {
  bsmColors: { [key: string]: string }
  laneColors: { [key: string]: string }
  travelConnectionColors: { [key: string]: [string, number[]] }
  signalHeadIcons: { [key: string]: string }
}

const mapLegendColors: MAP_LEGEND_COLORS = {
  bsmColors: { Other: '#0004ff' },
  laneColors: {
    Ingress: '#eb34e8',
    Egress: '#0004ff',
  },
  travelConnectionColors: {
    UNAVAILABLE: ['#797979', [2, 1]],
    DARK: ['#3a3a3a', [2, 1]],
    STOP_THEN_PROCEED: ['#c00000', [2, 1]],
    STOP_AND_REMAIN: ['#c00000', [1]],
    PRE_MOVEMENT: ['#c00000', [2, 2]],
    PERMISSIVE_MOVEMENT_ALLOWED: ['#267700', [2, 1]],
    PROTECTED_MOVEMENT_ALLOWED: ['#267700', [1]],
    PERMISSIVE_CLEARANCE: ['#e6b000', [2, 1]],
    PROTECTED_CLEARANCE: ['#e6b000', [1]],
    CAUTION_CONFLICTING_TRAFFIC: ['#e6b000', [1, 4]],
  },
  signalHeadIcons: {
    UNAVAILABLE: '/icons/traffic-light-icon-unknown.svg',
    DARK: '/icons/traffic-light-icon-unknown.svg',
    STOP_THEN_PROCEED: '/icons/traffic-light-icon-red-flashing.svg',
    STOP_AND_REMAIN: '/icons/traffic-light-icon-red-1.svg',
    PRE_MOVEMENT: '/icons/traffic-light-icon-yellow-red-1.svg',
    PERMISSIVE_MOVEMENT_ALLOWED: '/icons/traffic-light-icon-yellow-1.svg',
    PROTECTED_MOVEMENT_ALLOWED: '/icons/traffic-light-icon-green-1.svg',
    PERMISSIVE_CLEARANCE: '/icons/traffic-light-icon-yellow-1.svg',
    PROTECTED_CLEARANCE: '/icons/traffic-light-icon-yellow-1.svg',
    CAUTION_CONFLICTING_TRAFFIC: '/icons/traffic-light-icon-yellow-1.svg',
  },
}

export const initialState = {
  mapMessageLayerStyle: { ...mapMessageLayer, source: 'string' },
  mapMessageLabelsLayerStyle: { ...mapMessageLabelsLayer, source: 'string' },
  connectingLanesLayerStyle: { ...connectingLanesLayer, source: 'string' },
  connectingLanesLabelsLayerStyle: { ...connectingLanesLabelsLayer, source: 'string' },
  markerLayerStyle: { ...markerLayer, source: 'string' },
  bsmLayerStyle: { ...bsmLayerStyle, source: 'string' },
  signalStateLayerStyle: { ...signalStateLayer, source: 'string' },
  mapLegendColors: mapLegendColors,
}

export const intersectionMapLayerStyleSlice = createSlice({
  name: 'intersectionMapLayerStyle',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    setBsmLegendColors: (state, action: PayloadAction<{ [key: string]: string }>) => {
      state.value.mapLegendColors = { ...state.value.mapLegendColors, bsmColors: action.payload }
    },
    setBsmCircleColor: (state, action: PayloadAction<mapboxgl.CirclePaint['circle-color']>) => {
      state.value.bsmLayerStyle = {
        ...state.value.bsmLayerStyle,
        paint: { ...state.value.bsmLayerStyle.paint, 'circle-color': action.payload },
      }
    },
    setSignalLayerLayout: (state, action: PayloadAction<mapboxgl.SymbolLayout>) => {
      state.value.signalStateLayerStyle = { ...state.value.signalStateLayerStyle, layout: action.payload }
    },
  },
  extraReducers: (builder) => {
    builder
  },
})

export const selectMapMessageLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.mapMessageLayerStyle
export const selectMapMessageLabelsLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.mapMessageLabelsLayerStyle
export const selectConnectingLanesLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.connectingLanesLayerStyle
export const selectConnectingLanesLabelsLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.connectingLanesLabelsLayerStyle
export const selectMarkerLayerStyle = (state: RootState) => state.intersectionMapLayerStyle.value.markerLayerStyle
export const selectBsmLayerStyle = (state: RootState) => state.intersectionMapLayerStyle.value.bsmLayerStyle
export const selectSignalStateLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.signalStateLayerStyle
export const selectMapLegendColors = (state: RootState) => state.intersectionMapLayerStyle.value.mapLegendColors

export const { setBsmLegendColors, setBsmCircleColor, setSignalLayerLayout } = intersectionMapLayerStyleSlice.actions

export default intersectionMapLayerStyleSlice.reducer
