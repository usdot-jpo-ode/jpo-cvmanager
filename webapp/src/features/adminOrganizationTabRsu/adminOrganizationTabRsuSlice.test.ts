import reducer from './adminOrganizationTabRsuSlice'
import {
  // async thunks
  getRsuData,
  rsuDeleteSingle,
  rsuDeleteMultiple,
  rsuAddMultiple,
  refresh,

  // functions
  getRsuDataByIp,

  // reducers
  setSelectedRsuList,

  // selectors
  selectLoading,
  selectAvailableRsuList,
  selectSelectedRsuList,
} from './adminOrganizationTabRsuSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin organization tab RSU reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        availableRsuList: [],
        selectedRsuList: [],
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminOrganizationTabRsu'] = {
    loading: null,
    value: {
      availableRsuList: null,
      selectedRsuList: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getRsuData', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const orgName = 'orgName'
      const action = getRsuData(orgName)

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'data' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'data', orgName })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip: 'all' },
        tag: 'rsu',
      })

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminRsu,
        token: 'token',
        query_params: { rsu_ip: 'all' },
        tag: 'rsu',
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminOrganizationTabRsu/getRsuData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const orgName = 'org2'
      const data = {
        rsu_data: [
          {
            ip: '1.1.1.1',
            organizations: ['org1', 'org2'],
          },
        ],
      }
      let state = reducer(initialState, {
        type: 'adminOrganizationTabRsu/getRsuData/fulfilled',
        payload: { data, orgName, success: true },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, availableRsuList: [] },
      })

      // No matching organizations
      data['rsu_data'][0]['organizations'] = ['org1', 'org3']

      state = reducer(initialState, {
        type: 'adminOrganizationTabRsu/getRsuData/fulfilled',
        payload: { data, orgName, success: true },
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, availableRsuList: [{ id: 0, ip: '1.1.1.1' }] },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminOrganizationTabRsu/getRsuData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('rsuDeleteSingle', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rsu = { ip: '1.1.1.1' } as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      let rsuData = { rsu_data: { organizations: ['org1', 'org2'] } }

      let action = rsuDeleteSingle({ rsu, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ body: rsuData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
          url: EnvironmentVars.adminRsu,
          token: 'token',
          query_params: { rsu_ip: rsu.ip },
          tag: 'rsu',
        })
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
        expect(window.alert).not.toHaveBeenCalled()

        // Only 1 organization
        dispatch = jest.fn()
        rsuData = { rsu_data: { organizations: ['org1'] } }

        action = rsuDeleteSingle({ rsu, selectedOrg, selectedOrgEmail, updateTableData })

        apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        window.alert = jest.fn()
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove RSU 1.1.1.1 from selectedOrg because it must belong to at least one organization.'
        )
      } catch (e) {
        window.alert = jsdomAlert
        throw e
      }
    })
  })

  describe('rsuDeleteMultiple', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rows = [{ ip: '1.1.1.1' }, { ip: '1.1.1.2' }, { ip: '1.1.1.3' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      const rsuData = { rsu_data: { organizations: ['org1', 'org2', 'org3'] } }
      const invalidRsuData = { rsu_data: { organizations: ['org1'] } }

      let action = rsuDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })

      const jsdomAlert = window.alert
      try {
        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest
          .fn()
          .mockReturnValueOnce({ body: rsuData })
          .mockReturnValueOnce({ body: rsuData })
          .mockReturnValueOnce({ body: rsuData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(3)
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
        expect(window.alert).not.toHaveBeenCalled()

        // Only 1 organization
        dispatch = jest.fn()

        action = rsuDeleteMultiple({ rows, selectedOrg, selectedOrgEmail, updateTableData })

        window.alert = jest.fn()
        apiHelper._getDataWithCodes = jest
          .fn()
          .mockReturnValueOnce({ body: rsuData })
          .mockReturnValueOnce({ body: invalidRsuData })
          .mockReturnValueOnce({ body: invalidRsuData })
        await action(dispatch, getState, undefined)
        expect(apiHelper._getDataWithCodes).toHaveBeenCalledTimes(3)
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
        expect(window.alert).toHaveBeenCalledWith(
          'Cannot remove RSU(s) 1.1.1.2, 1.1.1.3 from selectedOrg because they must belong to at least one organization.'
        )
      } catch (e) {
        window.alert = jsdomAlert
        throw e
      }
    })
  })

  describe('rsuAddMultiple', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const rsuList = [{ ip: '1.1.1.1' }, { ip: '1.1.1.2' }, { ip: '1.1.1.3' }] as any
      const selectedOrg = 'selectedOrg'
      const selectedOrgEmail = 'name@email.com'
      const updateTableData = jest.fn()

      const action = rsuAddMultiple({ rsuList, selectedOrg, selectedOrgEmail, updateTableData })

      await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)
    })
  })

  describe('refresh', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const selectedOrg = 'selectedOrg'
      const updateTableData = jest.fn()

      const action = refresh({ selectedOrg, updateTableData })

      await action(dispatch, getState, undefined)
      expect(updateTableData).toHaveBeenCalledTimes(1)
      expect(updateTableData).toHaveBeenCalledWith(selectedOrg)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminOrganizationTabRsu'] = {
    loading: null,
    value: {
      availableRsuList: null,
      selectedRsuList: null,
    },
  }

  it('setSelectedRsuList reducer updates state correctly', async () => {
    const selectedRsuList = 'selectedRsuList'
    expect(reducer(initialState, setSelectedRsuList(selectedRsuList))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRsuList },
    })
  })
})

describe('functions', () => {
  it('getRsuDataByIp', async () => {
    const rsu_ip = '1.1.1.1'
    const token = 'token'
    apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ data: 'data' })
    const resp = await getRsuDataByIp(rsu_ip, token)
    expect(resp).toEqual({ data: 'data' })
    expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
      url: EnvironmentVars.adminRsu,
      token,
      query_params: { rsu_ip },
      tag: 'rsu',
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      availableRsuList: 'availableRsuList',
      selectedRsuList: 'selectedRsuList',
    },
  }
  const state = { adminOrganizationTabRsu: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectAvailableRsuList(state)).toEqual('availableRsuList')
    expect(selectSelectedRsuList(state)).toEqual('selectedRsuList')
  })
})
