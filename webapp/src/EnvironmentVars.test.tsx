import EnvironmentVars from './EnvironmentVars'

it('returns message types', () => {
  process.env.REACT_APP_COUNT_MESSAGE_TYPES = 'type1, type2, type3'
  const expectedMessageTypes = ['type1', 'type2', 'type3']
  expect(EnvironmentVars.getMessageTypes()).toEqual(expectedMessageTypes)
})

it('returns mapbox initial view state', () => {
  process.env.REACT_APP_MAPBOX_INIT_LATITUDE = '12.34'
  process.env.REACT_APP_MAPBOX_INIT_LONGITUDE = '56.78'
  process.env.REACT_APP_MAPBOX_INIT_ZOOM = '9.0'
  const expectedViewState = {
    latitude: 12.34,
    longitude: 56.78,
    zoom: 9.0,
  }
  expect(EnvironmentVars.getMapboxInitViewState()).toEqual(expectedViewState)
})
