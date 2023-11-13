import reducer from './adminEditOrganizationSlice'
import {
  // async thunks
  editOrganization,

  // functions
  updateStates,
  createJsonBody,

  // reducers
  setSuccessMsg,

  // selectors
  selectSuccessMsg,
  selectErrorState,
  selectErrorMsg,
  selectLoading,
} from './adminEditOrganizationSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'

describe('admin add User reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        successMsg: '',
        errorState: false,
        errorMsg: '',
      },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: null,
    value: {
      successMsg: null,
      errorState: null,
      errorMsg: null,
    },
  }

  beforeAll(() => {
    jest.mock('../../apis/api-helper')
  })

  afterAll(() => {
    jest.unmock('../../apis/api-helper')
  })

  describe('editOrganization', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const json = { name: 'orgName' }
      const selectedOrg = 'selectedOrg'
      let setValue = jest.fn()
      let updateOrganizationData = jest.fn()
      const action = editOrganization({ json, selectedOrg, setValue, updateOrganizationData })

      global.setTimeout = jest.fn((cb) => cb())
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: true, message: 'Changes were successfully applied!' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminOrg,
          token: 'token',
          body: JSON.stringify(createJsonBody(json, selectedOrg)),
        })
        expect(global.setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
        expect(updateOrganizationData).toHaveBeenCalledTimes(1)
        expect(setValue).toHaveBeenCalledTimes(2)
        expect(setValue).toHaveBeenCalledWith('orig_name', 'orgName')
        expect(setValue).toHaveBeenCalledWith('name', 'orgName')
      } catch (e) {
        global.setTimeout.mockClear()
        throw e
      }

      dispatch = jest.fn()
      setValue = jest.fn()
      updateOrganizationData = jest.fn()
      global.setTimeout = jest.fn((cb) => cb())
      try {
        apiHelper._patchData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
        let resp = await action(dispatch, getState, undefined)
        expect(resp.payload).toEqual({ success: false, message: 'message' })
        expect(apiHelper._patchData).toHaveBeenCalledWith({
          url: EnvironmentVars.adminOrg,
          token: 'token',
          body: JSON.stringify(createJsonBody(json, selectedOrg)),
        })
        expect(global.setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
        expect(setValue).not.toHaveBeenCalled()
        expect(updateOrganizationData).not.toHaveBeenCalled()
      } catch (e) {
        global.setTimeout.mockClear()
        throw e
      }
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      let successMsg = 'message'
      let errorMsg = ''
      let errorState = false

      let state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/fulfilled',
        payload: { message: 'message', success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })

      // Error Case
      successMsg = ''
      errorMsg = 'message'
      errorState = true

      state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, successMsg, errorMsg, errorState },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminEditOrganization/editOrganization/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })
})

describe('functions', () => {
  it('updateStates', async () => {
    const setValue = jest.fn()
    const selectedOrgName = 'selectedOrgName'

    updateStates(setValue, selectedOrgName)

    expect(setValue).toHaveBeenCalledTimes(2)
    expect(setValue).toHaveBeenCalledWith('orig_name', selectedOrgName)
    expect(setValue).toHaveBeenCalledWith('name', selectedOrgName)
  })

  it('createJsonBody', async () => {
    const data = { name: 'name' }
    const selectedOrg = 'selectedOrg'

    expect(createJsonBody(data, selectedOrg)).toEqual({
      orig_name: selectedOrg,
      name: data.name,
      users_to_add: [],
      users_to_modify: [],
      users_to_remove: [],
      rsus_to_add: [],
      rsus_to_remove: [],
    })
  })
})

describe('reducers', () => {
  const initialState = {
    loading: null,
    value: {
      selectedRsu: null,
    },
  }

  it('setSuccessMsg reducer updates state correctly', async () => {
    const successMsg = 'successMsg'
    expect(reducer(initialState, setSuccessMsg(successMsg))).toEqual({
      ...initialState,
      value: { ...initialState.value, successMsg },
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: {
      successMsg: 'successMsg',
      errorState: 'errorState',
      errorMsg: 'errorMsg',
    },
  }
  const state = { adminEditOrganization: initialState }

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectSuccessMsg(state)).toEqual('successMsg')
    expect(selectErrorState(state)).toEqual('errorState')
    expect(selectErrorMsg(state)).toEqual('errorMsg')
  })
})
