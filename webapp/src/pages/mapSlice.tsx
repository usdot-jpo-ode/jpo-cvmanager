import { createSlice } from '@reduxjs/toolkit'
import EnvironmentVars from '../EnvironmentVars'
import { RootState } from '../store'
import { evaluateFeatureFlags } from '../feature-flags'
import { MessageType } from '../models/MessageTypes'

const MESSAGE_COUNT_FREQUENCIES = {
  SPAT: 10 * 86400, // 10 Hz for 24 hours
  MAP: 86400, // 1 Hz for 24 hours
} as const

const initialState = {
  mapViewState: EnvironmentVars.getMapboxInitViewState(),
  activeLayers: [{ id: 'rsu-layer', tag: 'rsu' as FEATURE_KEY }]
    .filter((layer) => evaluateFeatureFlags(layer.tag))
    .map((layer) => layer.id),
}

export function getClusterColorStops(msgType: MessageType, heatMapData: GeoJSON.FeatureCollection<GeoJSON.Geometry>) {
  // Safely calculate max with fallback
  const counts = heatMapData.features
    .map((f) => f.properties?.count as number)
    .filter((count) => typeof count === 'number' && !isNaN(count) && count > 0)

  const max = counts.length > 0 ? Math.max(...counts) : 86400 // default to 1Hz for 24 hours

  // Get expected frequency for message type, fallback to actual max
  const desiredValue = MESSAGE_COUNT_FREQUENCIES[msgType] ?? max

  // Prevent division by zero
  if (desiredValue === 0) {
    return [
      [0, '#ffffcc'],
      [1, '#800026'],
    ] as [number, string][]
  }

  // Generate color stops based on message type frequency
  return [
    [0, '#ffffcc'], // Light yellow (low)
    [Math.round(desiredValue * 0.04), '#ffeda0'],
    [Math.round(desiredValue * 0.1), '#fed976'],
    [Math.round(desiredValue * 0.2), '#feb24c'],
    [Math.round(desiredValue * 0.3), '#fd8d3c'],
    [Math.round(desiredValue * 0.4), '#fc4e2a'], // Orange-red (high)
    [Math.round(desiredValue * 0.6), '#e31a1c'],
    [Math.round(desiredValue * 0.8), '#bd0026'], // Deep red (very high)
    [Math.round(desiredValue), '#800026'], // Dark red (extreme)
  ] as [number, string][]
}

export function getClusterRadiusStops(msgType: MessageType, heatMapData: GeoJSON.FeatureCollection<GeoJSON.Geometry>) {
  const counts = heatMapData.features
    .map((f) => f.properties?.count as number)
    .filter((count) => typeof count === 'number' && !isNaN(count) && count > 0)

  const max = counts.length > 0 ? Math.max(...counts) : 86400

  const desiredValue = MESSAGE_COUNT_FREQUENCIES[msgType] ?? max

  if (desiredValue === 0) {
    return [
      [0, 10],
      [1, 20],
    ] as [number, number][]
  }

  // Generate radius stops based on message type frequency
  return [
    [0, 0], // Min count = 0px radius
    [Math.round(desiredValue * 0.04), 20], // 4% = 20px
    [Math.round(desiredValue * 0.2), 22], // 20% = 22px
    [Math.round(desiredValue * 0.4), 25], // 40% = 25px
    [Math.round(desiredValue * 0.8), 30], // 80% = 30px
    [Math.round(desiredValue), 35], // 100% = 35px
  ] as [number, number][]
}

export function getClusterLabelSizeStops(
  msgType: MessageType,
  heatMapData: GeoJSON.FeatureCollection<GeoJSON.Geometry>
) {
  // Safely calculate max with fallback
  const counts = heatMapData.features
    .map((f) => f.properties?.count as number)
    .filter((count) => typeof count === 'number' && !isNaN(count) && count > 0)

  const max = counts.length > 0 ? Math.max(...counts) : 86400 // default to 1Hz for 24 hours

  // Get expected frequency for message type, fallback to actual max
  const desiredValue = MESSAGE_COUNT_FREQUENCIES[msgType] ?? max

  // Prevent division by zero
  if (desiredValue === 0) {
    return [
      [0, 10],
      [1, 16],
    ] as [number, number][]
  }

  // Generate text size stops based on message type frequency
  // Text sizes range from 10px (low counts) to 16px (high counts)
  return [
    [0, 10], // Min count = 10px font
    [Math.round(desiredValue * 0.04), 10], // 4% = 10px
    [Math.round(desiredValue * 0.2), 11], // 20% = 11px
    [Math.round(desiredValue * 0.4), 12], // 40% = 12px
    [Math.round(desiredValue * 0.6), 13], // 60% = 13px
    [Math.round(desiredValue * 0.8), 14], // 80% = 14px
    [Math.round(desiredValue), 16], // 100% = 16px
  ] as [number, number][]
}

export function getHeatmapCountsStops(msgType: MessageType, heatMapData: GeoJSON.FeatureCollection<GeoJSON.Geometry>) {
  // Safely calculate max with fallback
  const counts = heatMapData.features
    .map((f) => f.properties?.count as number)
    .filter((count) => typeof count === 'number' && !isNaN(count))

  const max = counts.length > 0 ? Math.max(...counts) : 86400 // default to 1Hz for 24 hours

  // Get expected frequency for message type, fallback to actual max
  const desiredValue = MESSAGE_COUNT_FREQUENCIES[msgType] ?? max

  // Prevent division by zero
  if (desiredValue === 0) {
    return [
      [0, 0],
      [1, 1],
    ]
  }

  // Generate mapbox heatmap layer stops from 0 -> 1
  return [
    [0, 0],
    [desiredValue * 0.2, 0.2],
    [desiredValue * 0.4, 0.4],
    [desiredValue * 0.6, 0.6],
    [desiredValue * 0.8, 0.8],
    [desiredValue, 1],
  ] as [number, number][]
}

export const mapSlice = createSlice({
  name: 'map',
  initialState: {
    value: initialState,
  },
  reducers: {
    setMapViewState: (state, action) => {
      state.value.mapViewState = action.payload
    },
    toggleLayerActive: (state, action) => {
      const layerId = action.payload
      if (state.value.activeLayers.includes(layerId)) {
        state.value.activeLayers = state.value.activeLayers.filter((id) => id !== layerId)
      } else {
        state.value.activeLayers.push(layerId)
      }
    },
  },
})

export const { setMapViewState, toggleLayerActive } = mapSlice.actions

export const selectViewState = (state: RootState) => state.map.value.mapViewState
export const selectActiveLayers = (state: RootState) => state.map.value.activeLayers

export default mapSlice.reducer
