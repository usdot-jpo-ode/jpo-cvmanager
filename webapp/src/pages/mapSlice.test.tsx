import reducer, { selectViewState } from './mapSlice'
import { RootState } from '../store'

jest.mock('../EnvironmentVars', () => ({
  getMapboxInitViewState: jest.fn(() => ({
    latitude: 0,
    longitude: 0,
    zoom: 0,
  })),
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
          latitude: 0,
          longitude: 0,
          zoom: 0,
        },
        activeLayers: [],
      },
    })
  })
})

const mapState = { map: initialState } as any

it('selectors return the correct value', async () => {
  expect(selectViewState({ ...mapState })).toEqual(initialState.value.mapViewState)
})
