import reducer from './adminAddOrganizationSlice'
import {
  // async thunks
  addOrg,
  resetMsg,

  // reducers
  setSuccessMsg,

  // selectors
  selectLoading,
  selectSuccessMsg,
  selectErrorState,
  selectErrorMsg,
} from './adminAddOrganizationSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin add organization reducer', () => {
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
  const initialState: RootState['adminAddOrganization'] = {
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

  describe('addOrg', () => {
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
      let action = addOrg({ json, reset })

      apiHelper._postData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: 'Organization Creation is successful.' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddOrg,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(reset).toHaveBeenCalled()
      expect(dispatch).toHaveBeenCalledTimes(2 + 2)

      // Error Code Other
      dispatch = jest.fn()
      reset = jest.fn()
      action = addOrg({ json, reset })
      apiHelper._postData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddOrg,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)
      expect(reset).not.toHaveBeenCalled()
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddOrganization/addOrg/pending',
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
        type: 'adminAddOrganization/addOrg/fulfilled',
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
        type: 'adminAddOrganization/addOrg/fulfilled',
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
        type: 'adminAddOrganization/addOrg/rejected',
      })
      expect(state).toEqual({ ...initialState, loading, value: { ...initialState.value } })
    })
  })

  describe('resetMsg', () => {
    it('returns and calls the api correctly', async () => {
      let dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = resetMsg()

      global.setTimeout = jest.fn((cb) => cb()) as any
      try {
        await action(dispatch, getState, undefined)
        expect(setTimeout).toHaveBeenCalledTimes(1)
        expect(dispatch).toHaveBeenCalledTimes(1 + 2)
      } catch (e) {
        ;(global.setTimeout as any).mockClear()
        throw e
      }
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['adminAddOrganization'] = {
    loading: null,
    value: {
      successMsg: null,
      errorState: null,
      errorMsg: null,
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
  const state = { adminAddOrganization: initialState } as any

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')

    expect(selectSuccessMsg(state)).toEqual('successMsg')
    expect(selectErrorState(state)).toEqual('errorState')
    expect(selectErrorMsg(state)).toEqual('errorMsg')
  })
})
