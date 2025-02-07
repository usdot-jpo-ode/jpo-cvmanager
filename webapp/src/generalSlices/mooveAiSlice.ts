import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import RsuApi from '../apis/rsu-api'
import { RootState } from '../store'
import { MooveAiFeature } from '../models/moove-ai/MooveAiData'
import { selectToken } from './userSlice'
import { toast } from 'react-hot-toast'

const initialState = {
  mooveAiData: [] as Array<MooveAiFeature>,
  mooveAiCoordinates: [] as number[][],
  mooveAiFilter: false,
  addMooveAiPoint: false,
}

export const getMooveAiData = createAsyncThunk(
  'mooveai/getMooveAiData',
  async (_, { getState }) => {
    const currentState = getState() as RootState
    const token = selectToken(currentState)

    const requestBody = {
      geometry: currentState.mooveai.value.mooveAiCoordinates,
    }

    try {
      const getMoveAiDataPromise = RsuApi.postMoveAiData(token, JSON.stringify(requestBody), '')
      toast.promise(getMoveAiDataPromise, {
        loading: `Retrieving Moove AI Data`,
        success: (data) => `Retrieved ${data.body.length.toLocaleString()} messages`,
        error: (err) => `Query failed: ${err}`,
      })
      const mooveAiData = await getMoveAiDataPromise

      // Check if response exists
      if (!mooveAiData && !mooveAiData.body) {
        toast.error('No data returned from API')
        return []
      }

      // Check if array is empty
      if (mooveAiData.body.length === 0) {
        toast.error('No segments found for the selected criteria')
        return []
      }

      return mooveAiData.body
    } catch (err) {
      const toastMessage = `Query failed: ${err}`
      toast.error(toastMessage)
      console.error(err)
      return [] // Return empty array on error
    }
  },
  {
    // Will guard thunk from being executed
    condition: (_, { getState }) => {
      const { mooveai } = getState() as RootState
      const valid = mooveai.value.mooveAiCoordinates.length > 2
      return valid
    },
  }
)

export const mooveAiSlice = createSlice({
  name: 'mooveai',
  initialState: {
    loading: false,
    value: initialState,
  },
  reducers: {
    clearMooveAiData: (state) => {
      state.value.mooveAiCoordinates = []
      state.value.mooveAiData = []
      state.value.mooveAiFilter = false
    },
    updateMooveAiPoints: (state, action: PayloadAction<number[][]>) => {
      state.value.mooveAiCoordinates = action.payload
    },
    toggleMooveAiPointSelect: (state) => {
      state.value.addMooveAiPoint = !state.value.addMooveAiPoint
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getMooveAiData.pending, (state) => {
        state.loading = true
        state.value.addMooveAiPoint = false
      })
      .addCase(getMooveAiData.fulfilled, (state, action) => {
        state.loading = false
        state.value.mooveAiData = action.payload
        state.value.mooveAiFilter = true
      })
      .addCase(getMooveAiData.rejected, (state) => {
        state.loading = false
      })
  },
})

export const selectLoading = (state: RootState) => state.mooveai.loading

export const selectMooveAiData = (state: RootState) => state.mooveai.value.mooveAiData
export const selectAddMooveAiPoint = (state: RootState) => state.mooveai.value.addMooveAiPoint
export const selectMooveAiCoordinates = (state: RootState) => state.mooveai.value.mooveAiCoordinates
export const selectMooveAiFilter = (state: RootState) => state.mooveai.value.mooveAiFilter

export const { clearMooveAiData, toggleMooveAiPointSelect, updateMooveAiPoints } = mooveAiSlice.actions

export default mooveAiSlice.reducer
