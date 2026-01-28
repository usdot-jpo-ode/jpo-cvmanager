import EnvironmentVars from './EnvironmentVars'

it('returns mapbox initial view state', () => {
  process.env.VITE_MAPBOX_INIT_LATITUDE = '12.34'
  process.env.VITE_MAPBOX_INIT_LONGITUDE = '56.78'
  process.env.VITE_MAPBOX_INIT_ZOOM = '9.0'
  const expectedViewState = {
    latitude: 12.34,
    longitude: 56.78,
    zoom: 9.0,
  }
  expect(EnvironmentVars.getMapboxInitViewState()).toEqual(expectedViewState)
})
