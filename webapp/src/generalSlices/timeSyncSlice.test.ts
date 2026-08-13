import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import reducer, {
  // async thunks
  syncTimeOffset,

  // selectors
  selectTimeOffsetMillis,
  selectLastSync,

  // utility functions
  computeAccurateTimeMillis,
  getAccurateTimeMillis,

  // actions
  setTimeOffset,
} from './timeSyncSlice'

// Mock EnvironmentVars
vi.mock('../EnvironmentVars', () => ({
  default: {
    timeSyncEndpoint: 'http://localhost:8089/timesync/utc-millis',
  },
}))

describe('timeSync reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      timeOffsetMillis: 0,
      lastSync: null,
    })
  })
})

describe('utility functions', () => {
  it('computeAccurateTimeMillis should compute accurate time correctly', () => {
    const utcMillis = 1690000000000 // Example UTC timestamp
    const timeOffsetMillis = 5000 // Example offset
    const accurateTime = computeAccurateTimeMillis(utcMillis, timeOffsetMillis)
    expect(accurateTime).toBe(1690000005000) // UTC + offset
  })

  it('getAccurateTimeMillis should compute accurate current time correctly', () => {
    const timeOffsetMillis = 5000 // Example offset
    const now = Date.now()
    const accurateTime = getAccurateTimeMillis(timeOffsetMillis)
    expect(accurateTime).toBeGreaterThanOrEqual(now + timeOffsetMillis)
    expect(accurateTime).toBeLessThanOrEqual(now + timeOffsetMillis + 10) // Allow slight timing differences
  })
})

describe('reducers', () => {
  const initialState = {
    timeOffsetMillis: 0,
    lastSync: null,
  }

  it('setTimeOffset should update timeOffsetMillis and lastSync', () => {
    const timeOffsetMillis = 5000
    const action = setTimeOffset(timeOffsetMillis)
    const state = reducer(initialState, action)

    expect(state.timeOffsetMillis).toBe(timeOffsetMillis)
    expect(new Date(state.lastSync!).getTime()).toBeLessThanOrEqual(Date.now())
  })
})

describe('async thunks', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    global.fetch = vi.fn()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('syncTimeOffset should synchronize time offset (mocked fetch)', async () => {
    const startTime = 1_000
    const endTime = 1_040
    const currentTime = 1_040
    const serverTime = 2_000

    ;(global.fetch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      json: vi.fn().mockResolvedValueOnce(serverTime),
    })
    vi.spyOn(Date, 'now')
      .mockReturnValueOnce(startTime)
      .mockReturnValueOnce(endTime)
      .mockReturnValueOnce(currentTime)

    const dispatch = vi.fn()
    const getState = vi.fn()
    const action = syncTimeOffset()

    const result = await action(dispatch, getState, undefined)

    const expectedOffset = serverTime - currentTime - Math.floor((endTime - startTime) / 2)

    expect(result.payload).toBe(expectedOffset)
    expect(global.fetch).toHaveBeenCalledWith('http://localhost:8089/timesync/utc-millis')
  })

  it('syncTimeOffset fulfilled should update timeOffsetMillis and lastSync via extraReducers', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-08-13T12:00:00.000Z'))

    const fulfilledAction = syncTimeOffset.fulfilled(940, 'request-id', undefined)
    const state = reducer(
      {
        timeOffsetMillis: 0,
        lastSync: null,
      },
      fulfilledAction
    )

    expect(state.timeOffsetMillis).toBe(940)
    expect(state.lastSync).toBe('2026-08-13T12:00:00.000Z')

    vi.useRealTimers()
  })
})

describe('selectors', () => {
  const initialState = {
    timeOffsetMillis: 5000,
    lastSync: '2024-08-20T16:17:03.056Z',
  }
  const state = { timeSync: initialState }

  it('selectTimeOffsetMillis should return the correct time offset', () => {
    expect(selectTimeOffsetMillis(state as any)).toBe(5000)
  })

  it('selectLastSync should return the correct last sync time', () => {
    expect(selectLastSync(state as any)).toBe('2024-08-20T16:17:03.056Z')
  })
})
