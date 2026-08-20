import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { CircleLayer, LineLayer, SymbolLayer } from 'react-map-gl'
import { RootState } from '../../../store'

const mapMessageLayer: LineLayer = {
  id: 'map-message',
  type: 'line',
  paint: {
    'line-width': 5,
    'line-color': ['case', ['==', ['get', 'ingressPath'], true], '#eb34e8', '#0004ff'],
  },
}

const mapMessageHighlightLayer: LineLayer = {
  id: 'srm-requested-lanes',
  type: 'line',
  paint: {
    'line-width': 12,
    'line-color': '#ccff33',
  },
}

const mapMessageLabelsLayer: SymbolLayer = {
  id: 'map-message-labels',
  type: 'symbol',
  layout: {
    'text-field': ['concat', '#', ['to-string', ['get', 'laneId']]],
    'text-size': 20,
    'text-font': ['literal', ['Open Sans Bold', 'Arial Unicode MS Bold']],
    'text-allow-overlap': true,
    'icon-allow-overlap': true,
  },
  paint: {
    'text-color': '#ffffff',
    'text-halo-color': '#000000',
    'text-halo-width': 2,
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

const connectingLanesSsmStatusLayer: SymbolLayer = {
  id: 'ssm-connection-status',
  type: 'symbol',
  minzoom: 1,
  maxzoom: 24,
  layout: {
    'icon-image': [
      'match',
      ['get', 'ssmStatus'],
      'UNKNOWN',
      'question-mark',
      'REQUESTED',
      'circular-arrow',
      'PROCESSING',
      'gear',
      'WATCH_OTHER_TRAFFIC',
      'warning',
      'GRANTED',
      'check',
      'REJECTED',
      'close',
      'MAX_PRESENCE',
      'timer',
      'RESERVICE_LOCKED',
      'lock',
      'close',
    ],
    'symbol-placement': 'line-center',
    'icon-allow-overlap': true,
    'icon-ignore-placement': true,
    'text-allow-overlap': true,
    'text-ignore-placement': true,
    'icon-optional': false,
    'icon-rotation-alignment': 'viewport',
    'icon-size': ['interpolate', ['linear'], ['zoom'], 10, 0.003, 14, 0.05, 18, 0.08, 22, 0.12],
  },
  paint: {
    'icon-color': [
      'match',
      ['get', 'ssmStatus'],
      'UNKNOWN',
      '#202020',
      'REQUESTED',
      '#002f9e',
      'PROCESSING',
      '#383838',
      'WATCH_OTHER_TRAFFIC',
      '#c06600',
      'GRANTED',
      '#109501',
      'REJECTED',
      '#ff0000',
      'MAX_PRESENCE',
      '#740000',
      'RESERVICE_LOCKED',
      '#680c00',
      '#202020',
    ],
    'icon-opacity': 1,
  },
}

const connectingLanesHighlightLayer: LineLayer = {
  id: 'ssm-connection-highlight',
  type: 'line',
  paint: {
    'line-width': 12,
    'line-color': '#ccff33',
  },
}

const connectingLanesLabelsLayer: SymbolLayer = {
  id: 'connecting-lanes-labels',
  type: 'symbol',
  layout: {
    'text-field': ['concat', 'SG:', ['to-string', ['get', 'signalGroupId']]],
    'text-size': 20,
    'text-offset': [0, 1],
    'text-font': ['literal', ['Open Sans Bold', 'Arial Unicode MS Bold']],
    'text-allow-overlap': true,
    'icon-allow-overlap': true,
    'icon-image': 'rounded',
    'icon-text-fit': 'both',
  },
  paint: {
    'text-color': '#ffffff',
    'text-halo-color': '#000000',
    'text-halo-width': 2,
  },
}

const srmLayer: SymbolLayer = {
  id: 'srm',
  type: 'symbol',
  source: 'srmData',
  layout: {
    'icon-image': 'srm_square',
    'icon-size': ['interpolate', ['linear'], ['zoom'], 12, 0.4, 16, 0.6, 20, 1],
    'icon-allow-overlap': true,
    'icon-ignore-placement': true,
    'icon-rotation-alignment': 'viewport',
  },
  paint: {
    'icon-color': ['match', ['get', 'vehicleID'], 'temp-id', '#0004ff', '#0004ff'],
    'icon-opacity': 1,
    'icon-halo-color': '#000000',
    'icon-halo-width': 20,
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
    'circle-stroke-color': '#000000',
    'circle-stroke-width': 1,
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
      'traffic-light-icon-green-1',
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

/**
 * LAYER RENDERING ORDER
 * Layers are rendered from bottom to top (first = bottom, last = top).
 * This controls which layers appear on top of others on the map.
 * Modify this array to change the visual stacking order.
 */
export const LAYER_RENDER_ORDER = [
  // Base layers (bottom)
  'srm-requested-lanes', // SRM requested lanes highlight (yellow)
  'ssm-connection-highlight', // SSM connection highlight (yellow)
  'map-message', // Map lanes
  'connecting-lanes', // Connecting lanes with signal states
  'invalid-lane-collection', // Invalid lane markers (red)
  'signal-states', // Signal head icons
  'ssm-connection-status', // SSM status icons on lanes
  'bsm', // BSM vehicle circles
  'srm', // SRM vehicle markers
  // Label layers (top - always visible over other elements)
  'map-message-labels', // Map lane labels
  'connecting-lanes-labels', // Connecting lane labels
] as const

export type MAP_LEGEND_COLORS = {
  bsmColors: { [key: string]: string }
  laneColors: { [key: string]: string }
  travelConnectionColors: { [key: string]: [string, number[]] }
  signalHeadIcons: { [key: string]: string }
  ssmStatusIcons: { [key: string]: [string, string] }
  srmColors: { [key: string]: string }
}

const mapLegendColors: MAP_LEGEND_COLORS = {
  bsmColors: { Other: '#0004ff' },
  laneColors: {
    Ingress: '#eb34e8',
    Egress: '#0004ff',
    'RSM/SSM Info (highlighted)': '#ccff33',
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
    'RSM/SSM Info (highlighted)': ['#ccff33', [1]],
  },
  signalHeadIcons: {
    UNAVAILABLE: '/icons/traffic-light-icon-unknown.svg',
    DARK: '/icons/traffic-light-icon-unknown.svg',
    STOP_THEN_PROCEED: '/icons/traffic-light-icon-red-flashing.svg',
    STOP_AND_REMAIN: '/icons/traffic-light-icon-red-1.svg',
    PRE_MOVEMENT: '/icons/traffic-light-icon-yellow-red-1.svg',
    PERMISSIVE_MOVEMENT_ALLOWED: '/icons/traffic-light-icon-green-1.svg',
    PROTECTED_MOVEMENT_ALLOWED: '/icons/traffic-light-icon-green-1.svg',
    PERMISSIVE_CLEARANCE: '/icons/traffic-light-icon-yellow-1.svg',
    PROTECTED_CLEARANCE: '/icons/traffic-light-icon-yellow-1.svg',
    CAUTION_CONFLICTING_TRAFFIC: '/icons/traffic-light-icon-yellow-1.svg',
  },
  ssmStatusIcons: {
    UNKNOWN: ['/icons/question-mark.png', '#202020'],
    REQUESTED: ['/icons/circular-arrow.png', '#002f9e'],
    PROCESSING: ['/icons/gear.png', '#383838'],
    WATCH_OTHER_TRAFFIC: ['/icons/warning.png', '#c06600'],
    GRANTED: ['/icons/check.png', '#109501'],
    REJECTED: ['/icons/close.png', '#ff0000'],
    MAX_PRESENCE: ['/icons/timer.png', '#740000'],
    RESERVICE_LOCKED: ['/icons/lock.png', '#680c00'],
  },
  srmColors: { Other: '#0004ff' },
}

export const initialState = {
  mapMessageLayerStyle: { ...mapMessageLayer, source: 'string' },
  mapMessageHighlightLayerStyle: { ...mapMessageHighlightLayer, source: 'string' },
  mapMessageLabelsLayerStyle: { ...mapMessageLabelsLayer, source: 'string' },
  connectingLanesLayerStyle: { ...connectingLanesLayer, source: 'string' },
  connectingLanesSsmStatusLayerStyle: { ...connectingLanesSsmStatusLayer, source: 'string' },
  connectingLanesHighlightLayerStyle: { ...connectingLanesHighlightLayer, source: 'string' },
  connectingLanesLabelsLayerStyle: { ...connectingLanesLabelsLayer, source: 'string' },
  srmLayerStyle: { ...srmLayer, source: 'string' },
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
    setSrmLegendColors: (state, action: PayloadAction<{ [key: string]: string }>) => {
      state.value.mapLegendColors = { ...state.value.mapLegendColors, srmColors: action.payload }
    },
    setBsmCircleColor: (state, action: PayloadAction<mapboxgl.CirclePaint['circle-color']>) => {
      state.value.bsmLayerStyle = {
        ...state.value.bsmLayerStyle,
        paint: { ...state.value.bsmLayerStyle.paint, 'circle-color': action.payload },
      }
    },
    setSrmCircleColor: (state, action: PayloadAction<mapboxgl.SymbolPaint['icon-color']>) => {
      state.value.srmLayerStyle = {
        ...state.value.srmLayerStyle,
        paint: { ...state.value.srmLayerStyle.paint, 'icon-color': action.payload },
      }
    },
    setSignalLayerLayout: (state, action: PayloadAction<mapboxgl.SymbolLayout>) => {
      state.value.signalStateLayerStyle = { ...state.value.signalStateLayerStyle, layout: action.payload }
    },
  },
})

export const selectMapMessageLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.mapMessageLayerStyle
export const selectMapMessageHighlightLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.mapMessageHighlightLayerStyle
export const selectMapMessageLabelsLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.mapMessageLabelsLayerStyle
export const selectConnectingLanesLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.connectingLanesLayerStyle
export const selectConnectingLanesSsmStatusLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.connectingLanesSsmStatusLayerStyle
export const selectConnectingLanesHighlightLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.connectingLanesHighlightLayerStyle
export const selectConnectingLanesLabelsLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.connectingLanesLabelsLayerStyle
export const selectSrmLayerStyle = (state: RootState) => state.intersectionMapLayerStyle.value.srmLayerStyle
export const selectMarkerLayerStyle = (state: RootState) => state.intersectionMapLayerStyle.value.markerLayerStyle
export const selectBsmLayerStyle = (state: RootState) => state.intersectionMapLayerStyle.value.bsmLayerStyle
export const selectSignalStateLayerStyle = (state: RootState) =>
  state.intersectionMapLayerStyle.value.signalStateLayerStyle
export const selectMapLegendColors = (state: RootState) => state.intersectionMapLayerStyle.value.mapLegendColors

export const { setBsmLegendColors, setSrmLegendColors, setBsmCircleColor, setSrmCircleColor, setSignalLayerLayout } =
  intersectionMapLayerStyleSlice.actions

export default intersectionMapLayerStyleSlice.reducer
