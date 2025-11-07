import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import HaasApi from '../apis/intersections/haas-api'
import { RootState } from '../store'
import { HaasWebsocketLocationResponse, HaasWebsocketLocationParams } from '../models/haas/HaasWebsocketLocation'
import { selectToken } from './userSlice'
import { toast } from 'react-hot-toast'

const initialState: HaasWebsocketLocationResponse = {
  data: { type: 'FeatureCollection', features: [] },
  metadata: { limit: 1000, returnedCount: 0, truncated: false, message: 'No data loaded' },
}

export const getHaasLocationData = createAsyncThunk(
  'haas/getHaasLocationData',
  async (params: HaasWebsocketLocationParams, { getState, rejectWithValue }) => {
    try {
      const currentState = getState() as RootState
      const token = selectToken(currentState)
      const query_params = {
        active_only: params.active_only.toString(),
        start_time_utc_millis: params.start_time_utc_millis.toString(),
        end_time_utc_millis: params.end_time_utc_millis.toString(),
        ...(params.limit && { limit: params.limit.toString() }),
      }
      const response = await HaasApi.getHaasLocationData({ token, query_params })
      // Validate response structure
      if (!response?.data?.features) {
        throw new Error('Invalid response format from server')
      }

      return response
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to fetch HAAS Alert data'
      return rejectWithValue(errorMessage)
    }
  }
)

export const haasSlice = createSlice({
  name: 'haas',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(getHaasLocationData.pending, (state) => {
        state.loading = true
      })
      .addCase(getHaasLocationData.fulfilled, (state, action) => {
        state.loading = false
        state.value = action.payload
        const featureCount = action.payload.data?.features?.length ?? 0
        const truncated = action.payload.metadata?.truncated ?? false

        // Show warning toast if results are truncated
        if (truncated) {
          toast.error(`Found ${featureCount} incidents (truncated due to limit)`, {
            duration: 5000,
          })
        } else {
          toast.success(`Found ${featureCount} incidents`)
        }
      })
      .addCase(getHaasLocationData.rejected, (state, action) => {
        state.loading = false
        state.value = initialState // Reset to initial state on error
        toast.error((action.payload as string) || 'Failed to fetch HAAS Alert data')
      })
  },
})

export const selectHaasLocationData = (state: RootState) => state.haas.value
export const selectLoading = (state: RootState) => state.haas.loading

export default haasSlice.reducer
