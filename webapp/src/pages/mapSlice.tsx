import { createSlice } from '@reduxjs/toolkit'
import EnvironmentVars from '../EnvironmentVars'
import { RootState } from '../store'

const initialState = EnvironmentVars.getMapboxInitViewState()

export const mapSlice = createSlice({
  name: 'map',
  initialState: {
    mapViewState: {
      ...initialState,
    },
  },
  reducers: {
    setMapViewState: (state, action) => {
      state.mapViewState = action.payload
    },
  },
})

export const { setMapViewState } = mapSlice.actions

export const selectViewState = (state: RootState) => state.map.mapViewState

export default mapSlice.reducer
