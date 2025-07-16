import reducer from './mooveAiSlice'
import { MooveAiFeature } from '../models/moove-ai/MooveAiData'
import {
  // async thunks
  updateMooveAiData,

  // reducers
  clearMooveAiData,
  toggleMooveAiPointSelect,
  updateMooveAiPoints,

  // selectors
  selectLoading,
  selectMooveAiData,
  selectAddMooveAiPoint,
  selectMooveAiCoordinates,
  selectMooveAiFilter,
} from './mooveAiSlice'
import RsuApi from '../apis/rsu-api'
import { RootState } from '../store'

describe('mooveai reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual({
      loading: false,
      value: {
        mooveAiData: {
          type: 'FeatureCollection',
          features: [] as MooveAiFeature[],
        } as GeoJSON.FeatureCollection<GeoJSON.Geometry>,
        mooveAiCoordinates: [] as number[][],
        mooveAiFilter: false,
        addMooveAiPoint: false,
      },
    })
  })
})

describe('async thunks', () => {
  const initialState: RootState['mooveai'] = {
    loading: null,
    value: {
      mooveAiData: {
        type: 'FeatureCollection',
        features: [] as MooveAiFeature[],
      } as GeoJSON.FeatureCollection<GeoJSON.Geometry>,
      mooveAiCoordinates: [] as number[][],
      mooveAiFilter: false,
      addMooveAiPoint: false,
    },
  }

  beforeAll(() => {
    jest.mock('../apis/rsu-api')
  })

  afterAll(() => {
    jest.unmock('../apis/rsu-api')
  })

  describe('updateMooveAiData', () => {
    it('returns and calls the api correctly', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        mooveai: {
          value: {
            mooveAiCoordinates: [1, 2, 3],
          },
        },
      })
      const action = updateMooveAiData()

      RsuApi.postMooveAiData = jest.fn().mockReturnValue('mooveAiData')
      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual([])
      expect(RsuApi.postMooveAiData).toHaveBeenCalledWith(
        'token',
        JSON.stringify({
          geometry: [1, 2, 3],
        }),
        ''
      )
    })

    it('condition blocks execution', async () => {
      const dispatch = jest.fn()
      const getState = jest.fn().mockReturnValue({
        user: {
          value: {
            authLoginData: { token: 'token' },
          },
        },
        rsu: {
          value: {
            geometry: [1, 2],
          },
        },
      })
      const action = updateMooveAiData()

      RsuApi.postMooveAiData = jest.fn().mockReturnValue('mooveAiData')
      const resp = await action(dispatch, getState, undefined)
      expect(resp.payload).toEqual(undefined)
      expect(RsuApi.postMooveAiData).not.toHaveBeenCalled()
    })

    it('Updates the state correctly pending', async () => {
      const loading = true
      const addMooveAiPoint = false
      const state = reducer(
        {
          ...initialState,
          value: { ...initialState.value, addMooveAiPoint },
        },
        {
          type: 'mooveai/updateMooveAiData/pending',
        }
      )

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value, addMooveAiPoint },
      })
    })

    it('Updates the state correctly fulfilled', async () => {
      const loading = false
      const mooveAiData = []
      const mooveAiFilter = true
      const state = reducer(initialState, {
        type: 'mooveai/updateMooveAiData/fulfilled',
        payload: mooveAiData,
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: {
          ...initialState.value,
          mooveAiData: {
            ...initialState.value.mooveAiData,
            features: mooveAiData,
          },
          mooveAiFilter,
        },
      })
    })

    it('Updates the state correctly rejected', async () => {
      const loading = false
      const state = reducer(initialState, {
        type: 'mooveai/updateMooveAiData/rejected',
      })

      expect(state).toEqual({
        ...initialState,
        loading,
        value: { ...initialState.value },
      })
    })
  })
})

describe('reducers', () => {
  const initialState: RootState['mooveai'] = {
    loading: null,
    value: {
      mooveAiData: {
        type: 'FeatureCollection',
        features: [] as MooveAiFeature[],
      } as GeoJSON.FeatureCollection<GeoJSON.Geometry>,
      mooveAiCoordinates: [] as number[][],
      mooveAiFilter: true,
      addMooveAiPoint: false,
    },
  }

  it('clearMooveAiData reducer updates state correctly', async () => {
    const mooveAiFilter = false
    expect(reducer(initialState, clearMooveAiData())).toEqual({
      ...initialState,
      value: { ...initialState.value, mooveAiFilter },
    })
  })

  it('updateMooveAiPoints reducer updates state correctly', async () => {
    const mooveAiCoordinates = [
      [1, 2],
      [3, 4],
    ]
    expect(reducer(initialState, updateMooveAiPoints(mooveAiCoordinates))).toEqual({
      ...initialState,
      value: { ...initialState.value, mooveAiCoordinates },
    })
  })

  it('toggleMooveAiPointSelect reducer updates state correctly', async () => {
    const addMooveAiPoint = true
    expect(reducer(initialState, toggleMooveAiPointSelect())).toEqual({
      ...initialState,
      value: { ...initialState.value, addMooveAiPoint },
    })
  })
})

describe('selectors', () => {
  const initialState: RootState['mooveai'] = {
    loading: false,
    value: {
      mooveAiData: {
        type: 'FeatureCollection',
        features: [] as MooveAiFeature[],
      } as GeoJSON.FeatureCollection<GeoJSON.Geometry>,
      mooveAiCoordinates: [] as number[][],
      mooveAiFilter: true,
      addMooveAiPoint: false,
    },
  }
  const state = { mooveai: initialState }

  it('selectors return the correct value', async () => {
    expect(selectLoading(state as any)).toEqual(false)
    expect(selectMooveAiData(state as any)).toEqual({
      type: 'FeatureCollection',
      features: [] as MooveAiFeature[],
    } as GeoJSON.FeatureCollection<GeoJSON.Geometry>)
    expect(selectMooveAiCoordinates(state as any)).toEqual([])
    expect(selectMooveAiFilter(state as any)).toEqual(true)
    expect(selectAddMooveAiPoint(state as any)).toEqual(false)
  })
})
