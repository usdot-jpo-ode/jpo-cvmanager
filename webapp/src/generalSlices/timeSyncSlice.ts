// store/timeSyncSlice.ts
import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { RootState } from '../store'

import EnvironmentVars from '../EnvironmentVars'

interface TimeSyncState {
  timeOffsetMillis: number // Offset in milliseconds
  lastSync: string | null // Timestamp of the last synchronization
}

const initialState: TimeSyncState = {
  timeOffsetMillis: 0,
  lastSync: null,
}

export const computeAccurateTimeMillis = (utcMillis: number, timeOffsetMillis: number): number =>
  utcMillis + timeOffsetMillis

export const getAccurateTimeMillis = (timeOffsetMillis: number): number => Date.now() + timeOffsetMillis

export const syncTimeOffset = createAsyncThunk('timeSync/syncTimeOffset', async (_) => {
  const start = Date.now() // Record the start time
  const response = await fetch(EnvironmentVars.timeSyncEndpoint)
  const end = Date.now() // Record the end time

  const rtt = end - start // Calculate round-trip time
  const serverTime: number = await response.json()

  const currentTime = Date.now()
  return serverTime - currentTime - Math.floor(rtt / 2) // Adjust for half of the round-trip time
})

const timeSyncSlice = createSlice({
  name: 'timeSync',
  initialState,
  reducers: {
    setTimeOffset(state, action: PayloadAction<number>) {
      state.timeOffsetMillis = action.payload
      state.lastSync = new Date().toISOString()
    },
  },
  extraReducers: (builder) => {
    builder.addCase(syncTimeOffset.fulfilled, (state, action) => {
      state.timeOffsetMillis = action.payload
      state.lastSync = new Date().toISOString()
      console.debug('Time offset synchronized:', action.payload, 'ms')
    })
  },
})

export const { setTimeOffset } = timeSyncSlice.actions

export const selectTimeOffsetMillis = (state: RootState) => state.timeSync.timeOffsetMillis
export const selectLastSync = (state: RootState) => state.timeSync.lastSync

export default timeSyncSlice.reducer
