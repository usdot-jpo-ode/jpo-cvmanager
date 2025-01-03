import reducer from './adminOrganizationTabIntersectionSlice'
import {
  // async thunks
  getIntersectionData,
  intersectionDeleteSingle,
  intersectionDeleteMultiple,
  intersectionAddMultiple,
  refresh,

  // functions
  getIntersectionDataById,

  // reducers
  setSelectedIntersectionList,

  // selectors
  selectLoading,
  selectAvailableIntersectionList,
  selectSelectedIntersectionList,
} from './adminOrganizationTabIntersectionSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin organization tab Intersection reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        availableIntersectionList: [],
        selectedIntersectionList: [],
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminOrganizationTabIntersection'] = {
    loading: null,
    value: {
      availableIntersectionList: null,
      selectedIntersectionList: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getIntersectionData', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const orgName = 'orgName'
      const action = getIntersectionData(orgName)

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'data', orgName })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id: 'all' },
      })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id: 'all' },
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminOrganizationTabIntersection/getIntersectionData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let orgName = 'org2'
      const data = {
        intersection_data: [
          {
            intersection_id: '1',
            organizations: ['org1', 'org2'],
          },
        ],
      }
      let state = reducer(initialState, {
        type: 'adminOrganizationTabIntersection/getIntersectionData/fulfilled',
        payload: { data, orgName, success: true },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, availableIntersectionList: [] },
      })

      // No matching organizations
      data['intersection_data'][0]['organizations'] = ['org1', 'org3']

      state = reducer(initialState, {
        type: 'adminOrganizationTabIntersection/getIntersectionData/fulfilled',
        payload: { data, orgName, success: true },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, availableIntersectionList: [{ id: 0, intersection_id: '1' }] },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminOrganizationTabIntersection/getIntersectionData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('intersectionDeleteSingle', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const intersection = { intersection_id: '1' } as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      let intersectionData = { intersection_data: { organizations: ['org1', 'org2'] } }

      let action = intersectionDeleteSingle({ intersection, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ body: intersectionData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
          url: EnvironmentVars.adminIntersection,
          token: 'token',
          query_params: { intersection_id: intersection.intersection_id },
        })
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
        expect(window.alert).not.toHaveBeenCalled()

        // Only 1 organization
        dispatch = jest.fn()
        intersectionData = { intersection_data: { organizations: ['org1'] } }

        action = intersectionDeleteSingle({ intersection, selectedOrg, selectedOrgEmail, updateTableData })

        apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        window.alert = jest.fn()
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove Intersection 1 from selectedOrg because it must belong to at least one organization.'
        )
      } catch (e) {
        window.alert = jsdomAlert
        throw e
      }
    })
  })

  describe('intersectionDeleteMultiple', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rows = [{ intersection_id: '1' }, { intersection_id: '2' }, { intersection_id: '3' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      const intersectionData = { intersection_data: { organizations: ['org1', 'org2', 'org3'] } }
      const invalidIntersectionData = { intersection_data: { organizations: ['org1'] } }

      let action = intersectionDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest
          .fn()
          .mockReturnValueOnce({ body: intersectionData })
          .mockReturnValueOnce({ body: intersectionData })
          .mockReturnValueOnce({ body: intersectionData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(3)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
        expect(window.alert).not.toHaveBeenCalled()

        // Only 1 organization
        dispatch = jest.fn()

        action = intersectionDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })

        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest
          .fn()
          .mockReturnValueOnce({ body: intersectionData })
          .mockReturnValueOnce({ body: invalidIntersectionData })
          .mockReturnValueOnce({ body: invalidIntersectionData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(3)
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove Intersection(s) 2, 3 from selectedOrg because they must belong to at least one organization.'
        )
      } catch (e) {
        window.alert = jsdomAlert
        throw e
      }
    })
  })

  describe('intersectionAddMultiple', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const intersectionList = [{ intersection_id: '1' }, { intersection_id: '2' }, { intersection_id: '3' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const fetchPatchOrganization = jest.fn()
      const updateTableData = jest.fn()

      let action = intersectionAddMultiple({ intersectionList, selectedOrg, selectedOrgEmail, updateTableData })

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)
    })
  })

  describe('refresh', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const selectedOrg = 'selectedOrg'
      const updateTableData = jest.fn()

      let action = refresh({ selectedOrg, updateTableData })

      await action(dispatch, getState, undefined)
      expect(updateTableData).toHaveBeenCalledTimes(1)
      expect(updateTableData).toHaveBeenCalledWith(selectedOrg)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminOrganizationTabIntersection'] = {
    loading: null,
    value: {
      availableIntersectionList: null,
      selectedIntersectionList: null,
    },
  }

  it('setSelectedIntersectionList reducer updates state correctly', async () => {
    const selectedIntersectionList = 'selectedIntersectionList'
    expect(reducer(initialState, setSelectedIntersectionList(selectedIntersectionList))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedIntersectionList },
    })
  })
})

describe('functions', () => {
  it('getIntersectionDataById', async () => {
    const intersection_id = '1'
    const token = 'token'
    apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ data: 'data' })
    const resp = await getIntersectionDataById(intersection_id, token)
    expect(resp).toEqual({ data: 'data' })
    expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
      url: EnvironmentVars.adminIntersection,
      token,
      query_params: { intersection_id },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      availableIntersectionList: 'availableIntersectionList',
      selectedIntersectionList: 'selectedIntersectionList',
    },
  }
  const state = { adminOrganizationTabIntersection: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectAvailableIntersectionList(state)).toEqual('availableIntersectionList')
    expect(selectSelectedIntersectionList(state)).toEqual('selectedIntersectionList')
  })
})
