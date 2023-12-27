import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import RsuApi from '../apis/rsu-api'
import { selectToken } from './userSlice'

const initialState = { type: 'FeatureCollection', features: [] }

export const getWzdxData = createAsyncThunk('wzdx/getWzdxData', async (_, { getState }) => {
  const currentState = getState()
  const token = selectToken(currentState)
  return await RsuApi.getWzdxData(token)
})

export const wzdxSlice = createSlice({
  name: 'wzdx',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(getWzdxData.pending, (state) => {
        state.loading = true
      })
      .addCase(getWzdxData.fulfilled, (state, action) => {
        state.loading = false
        state.value = action.payload
      })
      .addCase(getWzdxData.rejected, (state) => {
        state.loading = false
      })
  },
})

export const selectWzdxData = (state) => state.wzdx.value
export const selectLoading = (state) => state.wzdx.loading

export default wzdxSlice.reducer
