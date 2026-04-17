import fetchMock from 'jest-fetch-mock'
import { setupStore } from '../../store'
import { adminIntersectionApiSlice } from './adminIntersectionApiSlice'
import EnvironmentVars from '../../EnvironmentVars'

const BASE_URL = `${EnvironmentVars.CVIZ_API_SERVER_URL}/admin/intersections`

const mockUserState = {
  user: {
    value: {
      authLoginData: { token: 'test-token' },
      organization: { organization: 'test-org', role: 'admin' },
    },
  },
}

const mockIntersectionData = [
  {
    orig_intersection_id: '1001',
    intersection_id: '1001',
    ref_pt: { latitude: '39.7392', longitude: '-104.9903' },
    intersection_name: 'Test Intersection 1',
    origin_ip: '10.0.0.1',
    organizations: ['test-org'],
    rsus: ['10.0.0.10', '10.0.0.11'],
  },
  {
    orig_intersection_id: '1002',
    intersection_id: '1002',
    ref_pt: { latitude: '39.7500', longitude: '-105.0000' },
    intersection_name: 'Test Intersection 2',
    origin_ip: '10.0.0.2',
    organizations: ['test-org'],
    rsus: ['10.0.0.12'],
  },
]

// fetchBaseQuery passes a Request object to fetch, not (url, options)
function getRequest(callIndex = 0): Request {
  return fetchMock.mock.calls[callIndex][0] as Request
}

describe('adminIntersectionApiSlice', () => {
  beforeEach(() => {
    fetchMock.resetMocks()
  })

  const createStoreWithAuth = () => setupStore(mockUserState)

  describe('getIntersections', () => {
    it('sends request with correct URL and auth headers', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: mockIntersectionData }))

      await store.dispatch(adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org'))

      expect(fetchMock).toHaveBeenCalledTimes(1)
      const request = getRequest()
      expect(request.url).toBe(`${BASE_URL}`)
      expect(request.headers.get('Authorization')).toBe('Bearer test-token')
      expect(request.headers.get('Organization')).toBe('test-org')
      expect(request.method).toBe('GET')
    })

    it('returns intersection data on success', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: mockIntersectionData }))

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org')
      )

      expect(result.data).toEqual({ intersection_data: mockIntersectionData })
    })

    it('returns error on network failure', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ message: 'Internal Server Error' }), { status: 500 })

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org')
      )

      expect(result.error).toBeDefined()
      expect(result.error).toHaveProperty('status', 500)
    })
  })

  describe('getIntersection', () => {
    const singleIntersectionResponse = {
      intersection_data: {
        intersection_id: '1001',
        intersection_name: 'Test Intersection 1',
        origin_ip: '10.0.0.1',
        ref_pt: { latitude: '39.7392', longitude: '-104.9903' },
        organizations: ['test-org'],
        rsus: ['10.0.0.10'],
      },
      allowed_selections: {
        organizations: ['test-org', 'other-org'],
        rsus: ['10.0.0.10', '10.0.0.11', '10.0.0.12'],
      },
    }

    it('sends request with intersection ID in URL', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify(singleIntersectionResponse))

      await store.dispatch(adminIntersectionApiSlice.endpoints.getIntersection.initiate('1001'))

      expect(getRequest().url).toBe(`${BASE_URL}/1001`)
    })

    it('returns intersection data with allowed selections', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify(singleIntersectionResponse))

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.getIntersection.initiate('1001')
      )

      expect(result.data).toEqual(singleIntersectionResponse)
    })
  })

  describe('getIntersectionAllowedSelections', () => {
    const allowedSelectionsResponse = {
      organizations: ['org-a', 'org-b'],
      rsus: ['10.0.0.1', '10.0.0.2'],
    }

    it('sends request to allowed-selections endpoint', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify(allowedSelectionsResponse))

      await store.dispatch(
        adminIntersectionApiSlice.endpoints.getIntersectionAllowedSelections.initiate()
      )

      expect(getRequest().url).toBe(`${BASE_URL}/allowed-selections`)
    })

    it('returns organizations and RSUs', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify(allowedSelectionsResponse))

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.getIntersectionAllowedSelections.initiate()
      )

      expect(result.data).toEqual(allowedSelectionsResponse)
    })
  })

  describe('createIntersection', () => {
    const createBody = {
      orig_intersection_id: '2001',
      intersection_id: '2001',
      ref_pt: { latitude: '40.0', longitude: '-105.0' },
      intersection_name: 'New Intersection',
      origin_ip: '10.0.0.5',
      organizations: ['test-org'],
      rsus: ['10.0.0.20'],
    }

    it('sends POST request with correct body', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ success: true, message: 'Created' }))

      await store.dispatch(
        adminIntersectionApiSlice.endpoints.createIntersection.initiate(createBody)
      )

      const request = getRequest()
      expect(request.url).toBe(`${BASE_URL}`)
      expect(request.method).toBe('POST')
      const body = await request.json()
      expect(body).toEqual(createBody)
    })

    it('returns success on 200 response', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce('', { status: 200 })

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.createIntersection.initiate(createBody)
      )

      expect('data' in result).toBe(true)
    })
  })

  describe('patchIntersection', () => {
    const patchBody = {
      intersection_id: '1001',
      orig_intersection_id: '1001',
      ref_pt: { latitude: '39.7392', longitude: '-104.9903' },
      intersection_name: 'Updated Intersection',
      origin_ip: '10.0.0.1',
      organizations_to_add: ['new-org'],
      organizations_to_remove: [],
      rsus_to_add: ['10.0.0.15'],
      rsus_to_remove: ['10.0.0.11'],
    }

    it('sends PATCH request with correct body', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ success: true, message: 'Updated' }))

      await store.dispatch(
        adminIntersectionApiSlice.endpoints.patchIntersection.initiate(patchBody)
      )

      const request = getRequest()
      expect(request.url).toBe(`${BASE_URL}`)
      expect(request.method).toBe('PATCH')
      const body = await request.json()
      expect(body).toEqual(patchBody)
    })

    it('returns success response', async () => {
      const store = createStoreWithAuth()
      const responseBody = { success: true, message: 'Updated' }
      fetchMock.mockResponseOnce(JSON.stringify(responseBody))

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.patchIntersection.initiate(patchBody)
      )

      expect('data' in result).toBe(true)
      if ('data' in result) {
        expect(result.data).toEqual(responseBody)
      }
    })
  })

  describe('deleteIntersection', () => {
    it('sends DELETE request with intersection ID in URL', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ success: true, message: 'Deleted' }))

      await store.dispatch(
        adminIntersectionApiSlice.endpoints.deleteIntersection.initiate('1001')
      )

      const request = getRequest()
      expect(request.url).toBe(`${BASE_URL}/1001`)
      expect(request.method).toBe('DELETE')
    })

    it('returns success response', async () => {
      const store = createStoreWithAuth()
      const responseBody = { success: true, message: 'Deleted' }
      fetchMock.mockResponseOnce(JSON.stringify(responseBody))

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.deleteIntersection.initiate('1001')
      )

      expect('data' in result).toBe(true)
      if ('data' in result) {
        expect(result.data).toEqual(responseBody)
      }
    })

    it('returns error on failure', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ message: 'Not found' }), { status: 404 })

      const result = await store.dispatch(
        adminIntersectionApiSlice.endpoints.deleteIntersection.initiate('9999')
      )

      expect('error' in result).toBe(true)
      if ('error' in result) {
        expect(result.error).toHaveProperty('status', 404)
      }
    })
  })

  describe('cache tag invalidation', () => {
    it('getIntersections provides tags for each intersection and LIST', async () => {
      const store = createStoreWithAuth()
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: mockIntersectionData }))

      await store.dispatch(adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org'))

      const state = store.getState()
      const tags = state.adminIntersectionApi.provided

      expect(tags.AdminIntersection).toBeDefined()
      expect(tags.AdminIntersection?.['1001']).toBeDefined()
      expect(tags.AdminIntersection?.['1002']).toBeDefined()
      expect(tags.AdminIntersection?.LIST).toBeDefined()
    })

    it('deleteIntersection invalidates the deleted intersection and LIST', async () => {
      const store = createStoreWithAuth()

      // Populate cache
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: mockIntersectionData }))
      await store.dispatch(adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org'))

      // Delete intersection — triggers refetch of invalidated queries
      fetchMock.mockResponseOnce(JSON.stringify({ success: true, message: 'Deleted' }))
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: [] }))
      await store.dispatch(adminIntersectionApiSlice.endpoints.deleteIntersection.initiate('1001'))

      // Should have made 3 fetch calls: initial query, delete, refetch after invalidation
      expect(fetchMock).toHaveBeenCalledTimes(3)
    })

    it('createIntersection invalidates LIST to trigger refetch', async () => {
      const store = createStoreWithAuth()

      // Populate cache
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: mockIntersectionData }))
      await store.dispatch(adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org'))

      // Create intersection — triggers refetch of LIST
      fetchMock.mockResponseOnce(JSON.stringify({ success: true, message: 'Created' }))
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: [...mockIntersectionData] }))
      const createBody = {
        orig_intersection_id: '2001',
        intersection_id: '2001',
        ref_pt: { latitude: '40.0', longitude: '-105.0' },
        organizations: ['test-org'],
        rsus: ['10.0.0.20'],
      }
      await store.dispatch(adminIntersectionApiSlice.endpoints.createIntersection.initiate(createBody))

      expect(fetchMock).toHaveBeenCalledTimes(3)
    })

    it('patchIntersection invalidates the patched intersection and LIST', async () => {
      const store = createStoreWithAuth()

      // Populate cache
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: mockIntersectionData }))
      await store.dispatch(adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org'))

      // Patch intersection — triggers refetch
      fetchMock.mockResponseOnce(JSON.stringify({ success: true, message: 'Updated' }))
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: mockIntersectionData }))
      const patchBody = {
        intersection_id: '1001',
        orig_intersection_id: '1001',
        ref_pt: { latitude: '39.7392', longitude: '-104.9903' },
        organizations_to_add: [],
        organizations_to_remove: [],
        rsus_to_add: [],
        rsus_to_remove: [],
      }
      await store.dispatch(adminIntersectionApiSlice.endpoints.patchIntersection.initiate(patchBody))

      expect(fetchMock).toHaveBeenCalledTimes(3)
    })
  })

  describe('prepareHeaders', () => {
    it('does not set auth headers when no token is present', async () => {
      const store = setupStore({
        user: {
          value: {
            authLoginData: { token: undefined },
            organization: { organization: 'test-org' },
          },
        },
      })
      fetchMock.mockResponseOnce(JSON.stringify({ intersection_data: [] }))

      await store.dispatch(adminIntersectionApiSlice.endpoints.getIntersections.initiate('test-org'))

      const request = getRequest()
      expect(request.headers.has('Authorization')).toBe(false)
    })
  })
})
