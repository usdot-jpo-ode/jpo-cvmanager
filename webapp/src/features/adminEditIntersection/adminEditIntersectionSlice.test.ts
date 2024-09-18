import reducer from './adminEditIntersectionSlice'
import {
  // async thunks
  getIntersectionInfo,
  editIntersection,
  submitForm,

  // functions
  checkForm,
  updateJson,

  // reducers
  setSelectedOrganizations,
  setSelectedRsus,
  updateStates,

  // selectors
  selectLoading,
  selectApiData,
  selectOrganizations,
  selectSelectedOrganizations,
  selectRsus,
  selectSelectedRsus,
  selectSubmitAttempt,
} from './adminEditIntersectionSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin edit Intersection reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        apiData: {},
        organizations: [],
        selectedOrganizations: [],
        rsus: [],
        selectedRsus: [],
        submitAttempt: false,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminEditIntersection'] = {
    loading: null,
    value: {
      apiData: null,
      organizations: null,
      selectedOrganizations: null,
      rsus: null,
      selectedRsus: null,
      submitAttempt: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('getIntersectionInfo', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const intersection_id = '1'
      const action = getIntersectionInfo(intersection_id)

      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: '', data: 'body' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      dispatch = jest.fn()
      apiHelper._getDataWithCodes = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._getDataWithCodes).toHaveBeenCalledWith({
        url: EnvironmentVars.adminIntersection,
        token: 'token',
        query_params: { intersection_id },
        additional_headers: { 'Content-Type': 'application/json' },
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditIntersection/getIntersectionInfo/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const apiData = 'apiData' as any

      let state = reducer(
        { ...initialState, value: { ...initialState.value, apiData } },
        {
          type: 'adminEditIntersection/getIntersectionInfo/fulfilled',
          payload: { message: 'message', success: true, data: apiData },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, apiData },
      })

      // Error Case
      state = reducer(initialState, {
        type: 'adminEditIntersection/getIntersectionInfo/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditIntersection/getIntersectionInfo/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('editIntersection', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const json = { intersection_id: '1' } as any
      const action = editIntersection(json)

      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 200, message: 'message', body: 'body' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: true, message: 'Changes were successfully applied!' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminIntersection,
          token: 'token',
          query_params: { intersection_id: json.intersection_id },
          body: JSON.stringify(json),
        })
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }

      dispatch = jest.fn()
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: false, message: 'message' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminIntersection,
          token: 'token',
          query_params: { intersection_id: json.intersection_id },
          body: JSON.stringify(json),
        })
        expect(setTimeout).not.toHaveBeenCalled()
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditIntersection/editIntersection/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false

      let state = reducer(
        { ...initialState, value: { ...initialState.value } },
        {
          type: 'adminEditIntersection/editIntersection/fulfilled',
          payload: { message: 'message', success: true },
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })

      // Error Case
      state = reducer(initialState, {
        type: 'adminEditIntersection/editIntersection/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditIntersection/editIntersection/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('submitForm', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      let getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminEditIntersection: {
          value: {
            selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
            selectedRsus: [{ name: 'rsu1' }, { name: 'rsu2' }, { name: 'rsu3' }],
            apiData: {
              allowed_selections: {
                organizations: ['org1', 'org2', 'org4'],
                rsus: ['rsu1', 'rsu2', 'rsu4'],
              },
              intersection_data: {
                organizations: ['org2', 'org4'],
                rsus: ['rsu2', 'rsu4'],
              },
            },
          },
        },
      })
      const data = { data: 'data' } as any

      let action = submitForm(data)
      let resp = await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      // invalid checkForm
      dispatch = jest.fn()
      getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminEditIntersection: {
          value: {
            selectedOrganizations: [],
            selectedRsus: [],
          },
        },
      })
      action = submitForm(data)
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({
        message: 'Please fill out all required fields',
        submitAttempt: true,
        success: false,
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly fulfilled', async () => {
      const submitAttempt = 'submitAttempt'

      const state = reducer(initialState, {
        type: 'adminEditIntersection/submitForm/fulfilled',
        payload: { submitAttempt: 'submitAttempt' },
      })

      expect(state).toEqual({
        ...initialState,
        value: { ...initialState.value, submitAttempt },
      })
    })
  })
})

describe('functions', () => {
  it('checkForm selectedOrganizations', async () => {
    expect(
      checkForm({
        value: {
          selectedOrganizations: [],
          selectedRsus: [],
        },
      } as any)
    ).toEqual(false)
  })

  it('checkForm all invalid', async () => {
    expect(
      checkForm({
        value: {
          selectedOrganizations: [],
          selectedRsus: [],
        },
      } as any)
    ).toEqual(false)
  })

  it('checkForm all valid', async () => {
    expect(
      checkForm({
        value: {
          selectedOrganizations: ['org1'],
          selectedRsus: ['rsu1'],
        },
      } as any)
    ).toEqual(true)
  })

  it('updateJson', async () => {
    const data = {
      intersection_name: 'a',
    } as any
    const state = {
      value: {
        apiData: {
          allowed_selections: {
            organizations: ['org1', 'org2', 'org4'],
            rsus: ['rsu1', 'rsu2', 'rsu4'],
          },
          intersection_data: {
            organizations: ['org2', 'org4'],
            rsus: ['rsu2', 'rsu4'],
          },
        },
        selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
        selectedRsus: [{ name: 'rsu1' }, { name: 'rsu2' }, { name: 'rsu3' }],
      },
    } as any

    const expected = {
      intersection_name: 'a',
      organizations_to_add: ['org1'],
      organizations_to_remove: ['org4'],
      rsus_to_add: ['rsu1'],
      rsus_to_remove: ['rsu4'],
    }

    expect(updateJson(data, state)).toEqual(expected)
  })

  it('updateJson selectedRoute Other', async () => {
    const data = {
      intersection_name: 'a',
    } as any
    const state = {
      value: {
        apiData: {
          allowed_selections: {
            organizations: ['org1', 'org2', 'org4'],
            rsus: ['rsu1', 'rsu2', 'rsu4'],
          },
          intersection_data: {
            organizations: ['org2', 'org4'],
            rsus: ['rsu2', 'rsu4'],
          },
        },
        selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }, { name: 'org3' }],
        selectedRsus: [{ name: 'rsu1' }, { name: 'rsu2' }, { name: 'rsu3' }],
      },
    } as any

    const expected = {
      intersection_name: 'a',
      organizations_to_add: ['org1'],
      organizations_to_remove: ['org4'],
      rsus_to_add: ['rsu1'],
      rsus_to_remove: ['rsu4'],
    }

    expect(updateJson(data, state)).toEqual(expected)
  })
})

describe('reducers', () => {
  const initialState: RootState['adminEditIntersection'] = {
    loading: null,
    value: {
      apiData: null,
      organizations: null,
      selectedOrganizations: null,
      rsus: null,
      selectedRsus: null,
      submitAttempt: null,
    },
  }

  it('setSelectedOrganizations reducer updates state correctly', async () => {
    const selectedOrganizations = 'selectedOrganizations'
    expect(reducer(initialState, setSelectedOrganizations(selectedOrganizations))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedOrganizations },
    })
  })

  it('setSelectedRsus reducer updates state correctly', async () => {
    const selectedRsus = 'selectedRsus'
    expect(reducer(initialState, setSelectedRsus(selectedRsus))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRsus },
    })
  })

  it('updateStates', async () => {
    // write test for updateApiJson
    const apiData = {
      allowed_selections: {
        organizations: ['org1', 'org2'],
        rsus: ['rsu1', 'rsu2'],
      },
      intersection_data: {
        organizations: ['org1', 'org2'],
        rsus: ['rsu1', 'rsu2'],
      },
    } as any

    const values = {
      organizations: [{ name: 'org1' }, { name: 'org2' }],
      rsus: [{ name: 'rsu1' }, { name: 'rsu2' }],
      selectedOrganizations: [{ name: 'org1' }, { name: 'org2' }],
      selectedRsus: [{ name: 'rsu1' }, { name: 'rsu2' }],
    }
    expect(reducer(initialState, updateStates(apiData))).toEqual({
      ...initialState,
      value: { ...initialState.value, ...values, apiData },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      apiData: 'apiData',
      organizations: 'organizations',
      selectedOrganizations: 'selectedOrganizations',
      rsus: 'rsus',
      selectedRsus: 'selectedRsus',
      submitAttempt: 'submitAttempt',
    },
  }
  const state = { adminEditIntersection: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectApiData(state)).toEqual('apiData')
    expect(selectOrganizations(state)).toEqual('organizations')
    expect(selectSelectedOrganizations(state)).toEqual('selectedOrganizations')
    expect(selectRsus(state)).toEqual('rsus')
    expect(selectSelectedRsus(state)).toEqual('selectedRsus')
    expect(selectSubmitAttempt(state)).toEqual('submitAttempt')
  })
})
