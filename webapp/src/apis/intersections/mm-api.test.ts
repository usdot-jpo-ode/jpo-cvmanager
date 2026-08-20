import { vi } from 'vitest'
import MessageMonitorApi from './mm-api'
import { authApiHelper } from './api-helper-cviz'

vi.mock('./api-helper-cviz', () => ({
  authApiHelper: {
    invokeApi: vi.fn(),
  },
}))

describe('MessageMonitorApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getIntersections returns intersections and calls endpoint', async () => {
    const mockResponse = [{ intersectionId: 12109 }]
    ;(authApiHelper.invokeApi as any).mockResolvedValue(mockResponse)

    const result = await MessageMonitorApi.getIntersections({ token: 'token', organization: 'org' } as any)

    expect(result).toEqual(mockResponse)
    expect(authApiHelper.invokeApi).toHaveBeenCalledWith({
      path: '/intersections',
      token: 'token',
      headers: { Organization: 'org' },
      failureMessage: 'Failed to retrieve intersection list',
      tag: 'intersection',
    })
  })

  it('getSpatMessagesWithLatest merges in-range and latest messages', async () => {
    const inRangeSpat = { id: 'in-range' }
    const latestSpat = { id: 'latest' }
    const spatSpy = vi.spyOn(MessageMonitorApi, 'getSpatMessages')
    spatSpy.mockResolvedValueOnce([latestSpat] as any)
    spatSpy.mockResolvedValueOnce([inRangeSpat, null] as any)

    const startTime = new Date('2026-01-01T00:00:00Z')
    const endTime = new Date('2026-01-01T00:05:00Z')

    const result = await MessageMonitorApi.getSpatMessagesWithLatest({
      token: 'token',
      intersectionId: 12109,
      startTime,
      endTime,
      compact: true,
    })

    expect(result).toEqual([inRangeSpat, latestSpat])
    expect(spatSpy).toHaveBeenCalledTimes(2)
    spatSpy.mockRestore()
  })

  it('getSpatMessages builds query params and returns content', async () => {
    const expected = [{ messageType: 'SPAT' }]
    ;(authApiHelper.invokeApi as any).mockResolvedValue({ content: expected })

    const startTime = new Date('2026-01-01T00:00:00Z')
    const endTime = new Date('2026-01-01T00:05:00Z')

    const result = await MessageMonitorApi.getSpatMessages({
      token: 'token',
      intersectionId: 12109,
      startTime,
      endTime,
      latest: true,
      compact: true,
    })

    expect(result).toEqual(expected)
    expect(authApiHelper.invokeApi).toHaveBeenCalledWith(
      expect.objectContaining({
        path: '/data/processed-spat',
        token: 'token',
        queryParams: {
          intersection_id: '12109',
          start_time_utc_millis: startTime.getTime().toString(),
          end_time_utc_millis: endTime.getTime().toString(),
          latest: 'true',
          compact: 'true',
        },
      })
    )
  })

  it('getMapMessages builds query params and returns content', async () => {
    const expected = [{ properties: { intersectionId: 12109 } }]
    ;(authApiHelper.invokeApi as any).mockResolvedValue({ content: expected })

    const startTime = new Date('2026-01-01T00:00:00Z')
    const endTime = new Date('2026-01-01T00:05:00Z')

    const result = await MessageMonitorApi.getMapMessages({
      token: 'token',
      intersectionId: 12109,
      startTime,
      endTime,
      latest: false,
    })

    expect(result).toEqual(expected)
    expect(authApiHelper.invokeApi).toHaveBeenCalledWith(
      expect.objectContaining({
        path: '/data/processed-map',
        token: 'token',
        queryParams: {
          intersection_id: '12109',
          start_time_utc_millis: startTime.getTime().toString(),
          end_time_utc_millis: endTime.getTime().toString(),
          latest: 'false',
        },
      })
    )
  })

  it('getBsmMessages builds query params and returns content', async () => {
    const expected = [{ properties: { messageType: 'BSM' } }]
    ;(authApiHelper.invokeApi as any).mockResolvedValue({ content: expected })

    const startTime = new Date('2026-01-01T00:00:00Z')
    const endTime = new Date('2026-01-01T00:05:00Z')

    const result = await MessageMonitorApi.getBsmMessages({
      token: 'token',
      vehicleId: '10.11.81.12',
      startTime,
      endTime,
      long: -105.0909,
      lat: 39.588,
      distance: 500,
    })

    expect(result).toEqual(expected)
    expect(authApiHelper.invokeApi).toHaveBeenCalledWith(
      expect.objectContaining({
        abortController: undefined,
        failureMessage: 'Failed to retrieve BSM messages',
        path: '/data/processed-bsm',
        token: 'token',
        tag: 'intersection',
        queryParams: {
          start_time_utc_millis: startTime.getTime().toString(),
          end_time_utc_millis: endTime.getTime().toString(),
          longitude: '-105.0909',
          latitude: '39.588',
          distance: '500',
          vehicle_id: '10.11.81.12',
        },
      })
    )
  })

  it('getMessageCount for bsm adds map-based coordinates and returns first count', async () => {
    ;(authApiHelper.invokeApi as any)
      .mockResolvedValueOnce({
        content: [
          {
            properties: {
              refPoint: { latitude: 39.5880413, longitude: -105.0908854 },
            },
          },
        ],
      })
      .mockResolvedValueOnce({ content: [42] })

    const startTime = new Date('2026-01-01T00:00:00Z')
    const endTime = new Date('2026-01-01T00:05:00Z')

    const result = await MessageMonitorApi.getMessageCount('token', 'bsm', 12109, startTime, endTime)

    expect(result).toBe(42)
    expect(authApiHelper.invokeApi).toHaveBeenNthCalledWith(
      2,
      expect.objectContaining({
        path: '/data/bsm/count',
        token: 'token',
        queryParams: {
          start_time_utc_millis: startTime.getTime().toString(),
          end_time_utc_millis: endTime.getTime().toString(),
          test: 'false',
          latitude: '39.5880413',
          longitude: '-105.0908854',
          distance: '500',
          intersection_id: '12109',
        },
      })
    )
  })
})
