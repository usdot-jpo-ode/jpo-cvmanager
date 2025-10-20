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

const TIME_SERVER_URL_UTC = 'https://timeapi.io/api/Time/current/zone?timeZone=Etc/UTC'

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
  const initialState = {
    timeOffsetMillis: 0,
    lastSync: null,
  }

  beforeAll(() => {
    global.fetch = jest.fn()
  })

  afterAll(() => {
    jest.restoreAllMocks()
  })

  it('syncTimeOffset should synchronize time offset (mocked fetch)', async () => {
    const mockServerTime = '2025-10-20T21:28:30.0960336'
    const mockResponse = {
      year: 2025,
      month: 10,
      day: 20,
      hour: 21,
      minute: 28,
      seconds: 30,
      milliSeconds: 96,
      dateTime: '2025-10-20T21:28:30.0960336',
      date: '10/20/2025',
      time: '21:28',
      timeZone: 'Etc/UTC',
      dayOfWeek: 'Monday',
      dstActive: false,
    } // Mock fetch response
    ;(global.fetch as jest.Mock).mockResolvedValueOnce({
      json: jest.fn().mockResolvedValueOnce(mockResponse),
    })

    const dispatch = jest.fn()
    const getState = jest.fn()
    const action = syncTimeOffset()

    const start = Date.now()
    const result = await action(dispatch, getState, undefined)
    const end = Date.now()

    const serverTime = new Date(mockServerTime).getTime()
    const rtt = end - start
    const expectedOffset = serverTime + rtt / 2 - Date.now()

    expect(result.payload).toBeCloseTo(expectedOffset, -2) // Allow slight timing differences
    expect(global.fetch).toHaveBeenCalledWith(TIME_SERVER_URL_UTC)
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
