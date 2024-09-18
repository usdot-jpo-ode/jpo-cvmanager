import reducer from './adminAddIntersectionSlice'
import {
  // async thunks
  getIntersectionCreationData,
  createIntersection,
  submitForm,

  // functions
  updateApiJson,
  checkForm,
  updateJson,

  // reducers
  updateSelectedOrganizations,
  updateSelectedRsus,
  resetForm,

  // selectors
  selectApiData,
  selectOrganizations,
  selectRsus,
  selectSelectedOrganizations,
  selectSelectedRsus,
  selectSubmitAttempt,
  selectLoading,
} from './adminAddIntersectionSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin add Intersection reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        apiData: {},
        selectedOrganizations: [],
        selectedRsus: [],
        submitAttempt: false,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminAddIntersection'] = {
    loading: null,
    value: {
      apiData: null,
      selectedOrganizations: null,
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

  describe('getIntersectionCreationData', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = getIntersectionCreationData()

      const apiJson = { data: 'data' }
      apiHelper._getData = jest.fn().mockReturnValue('_getData_response')
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(undefined)
      expect(apiHelper._getData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddIntersection,
        token: 'token',
        additional_headers: { 'Content-Type': 'application/json' },
      })
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddIntersection/getIntersectionCreationData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const apiData = 'apiData'
      const state = reducer(initialState, {
        type: 'adminAddIntersection/getIntersectionCreationData/fulfilled',
        payload: apiData,
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, apiData },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminAddIntersection/getIntersectionCreationData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('createIntersection', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const json = { data: 'data' } as any

      let reset = jest.fn()
      let action = createIntersection({ json, reset })
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._postData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: true, message: '' })
        expect(apiHelper._postData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminAddIntersection,
          token: 'token',
          body: JSON.stringify(json),
        })
        expect(dispatch).toHaveBeenCalledTimes(2 + 2)
        expect(reset).toHaveBeenCalledTimes(1)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }

      // Error Code Other
      dispatch = jest.fn()
      reset = jest.fn()
      action = createIntersection({ json, reset })
      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        apiHelper._postData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: false, message: 'message' })
        expect(apiHelper._postData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminAddIntersection,
          token: 'token',
          body: JSON.stringify(json),
        })
        expect(setTimeout).not.toHaveBeenCalled()
        expect(dispatch).toHaveBeenCalledTimes(0 + 2)
        expect(reset).not.toHaveBeenCalled()
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddIntersection/createIntersection/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false

      let state = reducer(initialState, {
        type: 'adminAddIntersection/createIntersection/fulfilled',
        payload: { message: 'message', success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })

      // Error Case

      state = reducer(initialState, {
        type: 'adminAddIntersection/createIntersection/fulfilled',
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
        type: 'adminAddIntersection/createIntersection/rejected',
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
        adminAddIntersection: {
          value: {
            selectedOrganizations: ['org1'],
            selectedRsus: ['rsu1'],
          },
        },
      })
      const data = { data: 'data' } as any

      let reset = jest.fn()
      let action = submitForm({ data, reset })
      let resp = await action(dispatch, getState, undefined)
      expect(dispatch).toHaveBeenCalledTimes(2)

      // invalid checkForm

      dispatch = jest.fn()
      getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        adminAddIntersection: {
          value: {
            selectedOrganizations: [],
            selectedRsus: [],
          },
        },
      })
      action = submitForm({ data, reset })
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
        type: 'adminAddIntersection/submitForm/fulfilled',
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
  it('updateApiJson', async () => {
    // write test for updateApiJson
    const apiJson = {
      organizations: ['org1', 'org2'],
      rsus: ['rsu1', 'rsu2'],
    }

    const expected = {
      organizations: [
        { id: 0, name: 'org1' },
        { id: 1, name: 'org2' },
      ],
      rsus: [
        { id: 0, name: 'rsu1' },
        { id: 1, name: 'rsu2' },
      ],
    }
    expect(updateApiJson(apiJson)).toEqual(expected)
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
      ref_pt: {
        latitude: '39.7392',
        longitude: '-104.9903',
      },
      intersection_name: 'a',
      intersection_id: '1',
    } as any

    const state = {
      value: {
        selectedOrganizations: [{ name: 'org1' }],
        selectedRsus: [{ name: 'rsu1' }],
      },
    } as any

    const expected = {
      ref_pt: {
        latitude: 39.7392,
        longitude: -104.9903,
      },
      intersection_id: 1,
      intersection_name: 'a',
      organizations: ['org1'],
      rsus: ['rsu1'],
    }

    expect(updateJson(data, state)).toEqual(expected)
  })
})

describe('reducers', () => {
  const initialState: RootState['adminAddIntersection'] = {
    loading: null,
    value: {
      apiData: null,
      selectedOrganizations: null,
      selectedRsus: null,
      submitAttempt: null,
    },
  }

  it('updateSelectedOrganizations reducer updates state correctly', async () => {
    const selectedOrganizations = [{ id: 1, name: 'selectedOrganizations' }]
    expect(reducer(initialState, updateSelectedOrganizations(selectedOrganizations))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedOrganizations },
    })
  })

  it('updateSelectedRsus reducer updates state correctly', async () => {
    const selectedRsus = [{ id: 1, name: 'selectedRsus' }]
    expect(reducer(initialState, updateSelectedRsus(selectedRsus))).toEqual({
      ...initialState,
      value: { ...initialState.value, selectedRsus },
    })
  })

  it('resetForm reducer updates state correctly', async () => {
    const selectedOrganizations = [] as any
    const selectedRsus = [] as any
    expect(reducer(initialState, resetForm(selectedOrganizations))).toEqual({
      ...initialState,
      value: {
        ...initialState.value,
        selectedOrganizations,
        selectedRsus,
      },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      apiData: {
        organizations: ['org1', 'org2'],
        rsus: ['rsu1', 'rsu2'],
      },
      selectedOrganizations: 'selectedOrganizations',
      selectedRsus: 'selectedRsus',
      submitAttempt: 'submitAttempt',
    },
  }
  const state = { adminAddIntersection: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectApiData(state)).toEqual(initialState.value.apiData)
    expect(selectOrganizations(state)).toEqual(initialState.value.apiData.organizations)
    expect(selectRsus(state)).toEqual(initialState.value.apiData.rsus)
    expect(selectSelectedOrganizations(state)).toEqual('selectedOrganizations')
    expect(selectSelectedRsus(state)).toEqual('selectedRsus')
    expect(selectSubmitAttempt(state)).toEqual('submitAttempt')
  })

  it('selectors return the correct value defaults', async () => {
    initialState.value.apiData = undefined
    expect(selectApiData(state)).toEqual(undefined)
    expect(selectOrganizations(state)).toEqual([])
    expect(selectRsus(state)).toEqual([])
  })
})
