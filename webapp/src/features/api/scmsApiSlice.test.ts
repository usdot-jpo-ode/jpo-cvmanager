import fetchMock from 'jest-fetch-mock'
import { beforeEach, describe, expect, it } from 'vitest'
import { setupStore } from '../../store'
import EnvironmentVars from '../../EnvironmentVars'
import { formatScmsExpiration, scmsApiSlice } from './scmsApiSlice'

const BASE_URL = `${EnvironmentVars.CVIZ_API_SERVER_URL}/devices/scms`

const mockUserState = {
  user: {
    value: {
      authLoginData: { token: 'test-token' },
      organization: { organization: 'test-org', role: 'admin' },
    },
  },
}

function getRequest(callIndex = 0): Request {
  return fetchMock.mock.calls[callIndex][0] as Request
}

describe('scmsApiSlice', () => {
  beforeEach(() => {
    fetchMock.resetMocks()
  })

  const createStoreWithAuth = () => setupStore(mockUserState)

  describe('getScmsStatus', () => {
    const healthResponse = {
      scmsHealthByIp: {
        '10.0.0.10': { health: true, expiration: '2026-04-10T13:28:01Z' },
        '10.0.0.11': { health: false, expiration: '2026-01-01T00:00:00Z' },
        '10.0.0.12': null,
      },
    }

    it('sends GET request to /status with auth and organization headers', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify(healthResponse))

      await store.dispatch(scmsApiSlice.endpoints.getScmsStatus.initiate('test-org'))

      expect(fetchMock).toHaveBeenCalledTimes(1)
      const request = getRequest()
      expect(request.url).toBe(`${BASE_URL}/status`)
      expect(request.method).toBe('GET')
      expect(request.headers.get('Authorization')).toBe('Bearer test-token')
      expect(request.headers.get('Organization')).toBe('test-org')
    })

    it('unwraps scmsHealthByIp from the response', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify(healthResponse))

      const result = await store.dispatch(
        scmsApiSlice.endpoints.getScmsStatus.initiate('test-org')
      )

      expect(result.data).toEqual(healthResponse.scmsHealthByIp)
    })

    it('returns an error without throwing on 5xx response', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ message: 'oops' }), { status: 500 })

      const result = await store.dispatch(
        scmsApiSlice.endpoints.getScmsStatus.initiate('test-org')
      )

      expect(result.error).toBeDefined()
      expect(result.error).toHaveProperty('status', 500)
    })

    it('returns an error without throwing on 4xx response', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ message: 'forbidden' }), { status: 403 })

      const result = await store.dispatch(
        scmsApiSlice.endpoints.getScmsStatus.initiate('test-org')
      )

      expect(result.error).toHaveProperty('status', 403)
    })

    it('does not set Authorization header when token is absent', async () => {
      const store = setupStore({
        user: {
          value: {
            authLoginData: { token: undefined },
            organization: { organization: 'test-org' },
          },
        },
      })
      fetchMock.mockResponseOnce(JSON.stringify({ scmsHealthByIp: {} }))

      await store.dispatch(scmsApiSlice.endpoints.getScmsStatus.initiate('test-org'))

      const request = getRequest()
      expect(request.headers.has('Authorization')).toBe(false)
      expect(request.headers.get('Organization')).toBe('test-org')
    })
  })
})

describe('formatScmsExpiration', () => {
  it('returns empty string for null', () => {
    expect(formatScmsExpiration(null)).toBe('')
  })

  it('returns empty string for undefined', () => {
    expect(formatScmsExpiration(undefined)).toBe('')
  })

  it('returns empty string for empty string', () => {
    expect(formatScmsExpiration('')).toBe('')
  })

  it('formats a valid ISO-8601 timestamp as MM/DD/YYYY hh:mm:ss AM/PM', () => {
    const result = formatScmsExpiration('2026-04-10T13:28:01Z')
    // Accept locale/timezone variation but verify the shape is present.
    expect(result).toMatch(/^\d{2}\/\d{2}\/\d{4},?\s+\d{2}:\d{2}:\d{2}\s?(AM|PM)$/)
  })

  it('returns "Invalid Date" (native Date behavior) for malformed input', () => {
    // Document the current implementation behavior — new Date('not-a-date') produces an
    // Invalid Date whose toLocaleString returns "Invalid Date".
    expect(formatScmsExpiration('not-a-date')).toBe('Invalid Date')
  })
})
