import RsuApi from './rsu-api'
import { authApiHelper } from './api-helper-cviz'

// Mock the api-helper-cviz module
jest.mock('./api-helper-cviz', () => ({
  authApiHelper: {
    invokeApi: jest.fn(),
  },
}))

const mockInvokeApi = authApiHelper.invokeApi as jest.MockedFunction<typeof authApiHelper.invokeApi>

beforeEach(() => {
  mockInvokeApi.mockClear()
})

describe('RsuApi', () => {
  describe('getHistoricalRsuStatus', () => {
    it('should call invokeApi with correct parameters and return response', async () => {
      const expectedResponse = [
        {
          timestamp: 1717622387534,
          intersectionID: '1234',
          rsuIP: '10.0.0.1',
          temperature: 37,
          uptime: 1294615,
          mode: 4,
        },
        {
          timestamp: 1717622447534,
          intersectionID: '1234',
          rsuIP: '10.0.0.1',
          temperature: 38,
          uptime: 1294675,
          mode: 4,
        },
      ]

      mockInvokeApi.mockResolvedValue(expectedResponse)

      const startTime = new Date('2025-06-05T21:00:00Z')
      const endTime = new Date('2025-06-05T23:00:00Z')

      const result = await RsuApi.getHistoricalRsuStatus({
        token: 'testToken',
        rsuIp: '10.0.0.1',
        startTime,
        endTime,
      })

      expect(result).toEqual(expectedResponse)
      expect(mockInvokeApi).toHaveBeenCalledTimes(1)
      expect(mockInvokeApi).toHaveBeenCalledWith({
        path: '/data/rsu-status/historical',
        token: 'testToken',
        queryParams: {
          rsuIp: '10.0.0.1',
          startTime: startTime.getTime().toString(),
          endTime: endTime.getTime().toString(),
        },
        abortController: undefined,
        failureMessage: 'Failed to fetch historical RSU status',
        tag: 'rsu',
      })
    })

    it('should pass abort controller when provided', async () => {
      const expectedResponse = []
      mockInvokeApi.mockResolvedValue(expectedResponse)

      const abortController = new AbortController()
      const startTime = new Date('2025-06-05T21:00:00Z')
      const endTime = new Date('2025-06-05T23:00:00Z')

      await RsuApi.getHistoricalRsuStatus({
        token: 'testToken',
        rsuIp: '10.0.0.1',
        startTime,
        endTime,
        abortController,
      })

      expect(mockInvokeApi).toHaveBeenCalledWith(
        expect.objectContaining({
          abortController,
        })
      )
    })
  })

  describe('getLatestRsuStatus', () => {
    it('should call invokeApi with correct parameters and return response', async () => {
      const expectedResponse = {
        timestamp: 1717622387534,
        intersectionID: '1234',
        rsuIP: '10.0.0.1',
        temperature: 37,
        uptime: 1294615,
        mode: 4,
      }

      mockInvokeApi.mockResolvedValue(expectedResponse)

      const result = await RsuApi.getLatestRsuStatus({
        token: 'testToken',
        rsuIp: '10.0.0.1',
      })

      expect(result).toEqual(expectedResponse)
      expect(mockInvokeApi).toHaveBeenCalledTimes(1)
      expect(mockInvokeApi).toHaveBeenCalledWith({
        path: '/data/rsu-status/latest',
        token: 'testToken',
        queryParams: {
          rsuIp: '10.0.0.1',
        },
        abortController: undefined,
        failureMessage: 'Failed to fetch latest RSU status',
        tag: 'rsu',
      })
    })

    it('should pass abort controller when provided', async () => {
      const expectedResponse = {
        timestamp: 1717622387534,
        intersectionID: '1234',
        rsuIP: '10.0.0.1',
        temperature: 37,
        uptime: 1294615,
        mode: 4,
      }

      mockInvokeApi.mockResolvedValue(expectedResponse)

      const abortController = new AbortController()

      await RsuApi.getLatestRsuStatus({
        token: 'testToken',
        rsuIp: '10.0.0.1',
        abortController,
      })

      expect(mockInvokeApi).toHaveBeenCalledWith(
        expect.objectContaining({
          abortController,
        })
      )
    })
  })

  describe('getAggregatedRsuStatus', () => {
    it('should call invokeApi with correct parameters and return response', async () => {
      const expectedResponse = [
        {
          timestamp: 1717622387534,
          intersectionID: '1234',
          rsuIP: '10.0.0.1',
          temperature: 37,
          uptime: 1294615,
          mode: 4,
        },
        {
          timestamp: 1717626000000,
          intersectionID: '1234',
          rsuIP: '10.0.0.1',
          temperature: 39,
          uptime: 1298228,
          mode: 4,
        },
      ]

      mockInvokeApi.mockResolvedValue(expectedResponse)

      const startTime = new Date('2025-06-05T21:00:00Z')
      const endTime = new Date('2025-06-06T21:00:00Z')
      const intervalMinutes = 60

      const result = await RsuApi.getAggregatedRsuStatus({
        token: 'testToken',
        rsuIp: '10.0.0.1',
        startTime,
        endTime,
        intervalMinutes,
      })

      expect(result).toEqual(expectedResponse)
      expect(mockInvokeApi).toHaveBeenCalledTimes(1)
      expect(mockInvokeApi).toHaveBeenCalledWith({
        path: '/data/rsu-status/aggregated',
        token: 'testToken',
        queryParams: {
          rsuIp: '10.0.0.1',
          startTime: startTime.getTime().toString(),
          endTime: endTime.getTime().toString(),
          intervalMinutes: intervalMinutes.toString(),
        },
        abortController: undefined,
        failureMessage: 'Failed to fetch aggregated RSU status',
        tag: 'rsu',
      })
    })

    it('should pass abort controller when provided', async () => {
      const expectedResponse = []
      mockInvokeApi.mockResolvedValue(expectedResponse)

      const abortController = new AbortController()
      const startTime = new Date('2025-06-05T21:00:00Z')
      const endTime = new Date('2025-06-06T21:00:00Z')

      await RsuApi.getAggregatedRsuStatus({
        token: 'testToken',
        rsuIp: '10.0.0.1',
        startTime,
        endTime,
        intervalMinutes: 60,
        abortController,
      })

      expect(mockInvokeApi).toHaveBeenCalledWith(
        expect.objectContaining({
          abortController,
        })
      )
    })
  })
})
