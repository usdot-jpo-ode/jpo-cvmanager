import reducer from './adminAddOrganizationSlice'
import {
  // async thunks
  addOrg,

  // selectors
  selectLoading,
} from './adminAddOrganizationSlice'
import apiHelper from '../../apis/api-helper'
import EnvironmentVars from '../../EnvironmentVars'
import { RootState } from '../../store'

describe('admin add organization reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['adminAddOrganization'] = {
    loading: null,
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
      let action = addOrg({ json })

      apiHelper._postData = jest.fn().mockReturnValue({ status: 200, message: 'message' })
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: true, message: 'Organization Creation is successful.' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddOrg,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(dispatch).toHaveBeenCalledTimes(1 + 2)

      // Error Code Other
      dispatch = jest.fn()
      action = addOrg({ json })
      apiHelper._postData = jest.fn().mockReturnValue({ status: 500, message: 'message' })
      resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual({ success: false, message: 'message' })
      expect(apiHelper._postData).toHaveBeenCalledWith({
        url: EnvironmentVars.adminAddOrg,
        token: 'token',
        body: JSON.stringify(json),
      })
      expect(dispatch).toHaveBeenCalledTimes(0 + 2)
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'adminAddOrganization/addOrg/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false

      let state = reducer(initialState, {
        type: 'adminAddOrganization/addOrg/fulfilled',
        payload: { message: 'message', success: true },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
      })

      // Error Case

      state = reducer(initialState, {
        type: 'adminAddOrganization/addOrg/fulfilled',
        payload: { message: 'message', success: false },
      })

      expect(state).toEqual({
        ...initialState,
        loading,
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'adminAddOrganization/addOrg/rejected',
      })
      expect(state).toEqual({ ...initialState, loading })
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
  })
})
