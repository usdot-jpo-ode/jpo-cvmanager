import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import EnvironmentVars from '../EnvironmentVars'
import { RootState } from '../store'
import { evaluateFeatureFlags } from '../feature-flags'

const initialState = {
  mapViewState: EnvironmentVars.getMapboxInitViewState(),
  activeLayers: [{ id: 'rsu-layer', tag: 'rsu' as FEATURE_KEY }]
    .filter((layer) => evaluateFeatureFlags(layer.tag))
    .map((layer) => layer.id),
  mooveAiPolygonSource: {
    type: 'Feature',
    geometry: {
      type: 'Polygon',
      coordinates: [],
    },
    properties: {},
  } as GeoJSON.Feature<GeoJSON.Geometry>,
  mooveAiPolygonPointSource: {
    type: 'FeatureCollection',
    features: [],
  } as GeoJSON.FeatureCollection<GeoJSON.Geometry>,
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
    setMooveAiPolygonSource: (state, action) => {
      state.value.mooveAiPolygonSource = action.payload
    },
    setMooveAiPolygonPointSource: (state, action) => {
      state.value.mooveAiPolygonPointSource = action.payload
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

export const { setMapViewState, setMooveAiPolygonSource, setMooveAiPolygonPointSource, toggleLayerActive } =
  mapSlice.actions

export const selectViewState = (state: RootState) => state.map.value.mapViewState
export const selectMooveAiPolygonSource = (state: RootState) => state.map.value.mooveAiPolygonSource
export const selectMooveAiPolygonPointSource = (state: RootState) => state.map.value.mooveAiPolygonPointSource
export const selectActiveLayers = (state: RootState) => state.map.value.activeLayers

export default mapSlice.reducer
