import reducer from './wzdxSlice'
import {
  // async thunks
  getWzdxData,

  // selectors
  selectLoading,
  selectWzdxData,
} from './wzdxSlice'
import RsuApi from '../apis/rsu-api'

describe('wzdx reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: { type: 'FeatureCollection', features: [] },
    })
  })
})

describe('async thunks', () => {
  const initialState = {
    loading: null,
    value: null,
  }

  beforeAll(() => {
    jest.mock('../apis/rsu-api.js')
  })

  afterAll(() => {
    jest.unmock('../apis/rsu-api.js')
  })

  describe('getWzdxData', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
      })
      const action = getWzdxData()

      const data = 'data'
      RsuApi.getWzdxData = jest.fn().mockReturnValue(data)
      let resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(data)
      expect(RsuApi.getWzdxData).toHaveBeenCalledWith('token')
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const state = reducer(initialState, {
        type: 'wzdx/getWzdxData/pending',
      })
      expect(state).toEqual({
        ...initialState,
        loading,
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const value = 'wzdxData'
      const state = reducer(initialState, {
        type: 'wzdx/getWzdxData/fulfilled',
        payload: value,
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value,
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'wzdx/getWzdxData/rejected',
      })
      expect(state).toEqual({ ...initialState, loading })
    })
  })
})

describe('selectors', () => {
  const initialState = {
    loading: 'loading',
    value: 'wzdxData',
  }
  const state = { wzdx: initialState }

  it('selectors return the correct value', async () => {
    expect(selectLoading(state)).toEqual('loading')
    expect(selectWzdxData(state)).toEqual('wzdxData')
  })
})
