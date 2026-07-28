import reducer, { selectViewState } from './mapSlice'
import { RootState } from '../store'
import { vi } from 'vitest'

vi.mock('../EnvironmentVars', () => ({
  default: {
    getMapboxInitViewState: vi.fn(() => ({
      latitude: 0,
      longitude: 0,
      zoom: 0,
    })),
  },
}))

vi.mock('../feature-flags', () => ({
  evaluateFeatureFlags: vi.fn(() => false),
}))

const initialState: RootState['map'] = {
  value: {
    mapViewState: {
      latitude: 0,
      longitude: 0,
      zoom: 0,
    },
    activeLayers: [],
  },
}

describe('mapSlice reducer', () => {
  it('should handle initial state', () => {
    expect(reducer(undefined, { type: 'unknown' })).toEqual(initialState)
  })
})

describe('setMapViewState', () => {
  it('should set the mapViewState', () => {
    const previousState = {
      value: {
        mapViewState: {
          latitude: 0,
          longitude: 0,
          zoom: 0,
        },
        activeLayers: [],
      },
    }
    const action = {
      type: 'map/setMapViewState',
      payload: {
        latitude: 1,
        longitude: 1,
        zoom: 1,
      },
    }
    const newState = reducer(previousState, action)
    expect(newState).toEqual({
      value: {
        mapViewState: {
          latitude: 1,
          longitude: 1,
          zoom: 1,
        },
        activeLayers: [],
      },
    })
  })
})

const mapState = { map: initialState } as RootState

it('selectors return the correct value', async () => {
  expect(selectViewState(mapState)).toEqual(initialState.value.mapViewState)
})
