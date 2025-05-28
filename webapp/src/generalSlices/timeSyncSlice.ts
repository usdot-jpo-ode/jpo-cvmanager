// store/timeSyncSlice.ts
import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import ntpClient from 'ntp-client'
import { RootState } from '../store'

interface TimeSyncState {
  timeOffsetMillis: number // Offset in milliseconds
  lastSync: string | null // Timestamp of the last synchronization
  syncError: string | null // Error message if synchronization fails
}

const initialState: TimeSyncState = {
  timeOffsetMillis: 0,
  lastSync: null,
  syncError: null,
}

export const computeAccurateTimeMillis = (utcMillis: number, timeOffsetMillis: number): number =>
  utcMillis + timeOffsetMillis

export const getNewAccurateTimeMillis = (timeOffsetMillis: number): number => Date.now() + timeOffsetMillis

export const syncWithNtp = createAsyncThunk('timeSync/syncWithNtp', async (_, { dispatch }) => {
  try {
    ntpClient.getNetworkTime('pool.ntp.org', 123, (err, date) => {
      if (err) {
        console.error('Failed to sync with NTP server:', err)
        return { offset: undefined, error: 'Failed to sync with NTP server' }
      }
      const ntpTime = date.getTime()
      const systemTime = Date.now()
      const offset = ntpTime - systemTime
      console.log('Time offset updated:', offset, 'ms')
      return { offset, error: null }
    })
  } catch (error) {
    console.error('Unexpected error during NTP sync:', error)
    return { offset: undefined, error: 'Unexpected error during NTP sync' }
  }
})

const timeSyncSlice = createSlice({
  name: 'timeSync',
  initialState,
  reducers: {
    setTimeOffset(state, action: PayloadAction<number>) {
      state.timeOffsetMillis = action.payload
      state.lastSync = new Date().toISOString()
      state.syncError = null
    },
    setSyncError(state, action: PayloadAction<string>) {
      state.syncError = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(syncWithNtp.fulfilled, (state, action) => {
        state.timeOffsetMillis = action.payload.offset
        state.lastSync = new Date().toISOString()
        state.syncError = action.payload.error
      })
      .addCase(syncWithNtp.rejected, (state) => {
        state.syncError = 'Unexpected error during NTP sync'
      })
  },
})

export const { setTimeOffset, setSyncError } = timeSyncSlice.actions

export const selectTimeOffsetMillis = (state: RootState) => state.timeSync.timeOffsetMillis
export const selectLastSync = (state: RootState) => state.timeSync.lastSync
export const selectSyncError = (state: RootState) => state.timeSync.syncError
export const getAccurateTimeMillis = (state: RootState) =>
  computeAccurateTimeMillis(Date.now(), selectTimeOffsetMillis(state))
export const getAccurateTime = (state: RootState) => new Date(getAccurateTimeMillis(state))

export default timeSyncSlice.reducer
