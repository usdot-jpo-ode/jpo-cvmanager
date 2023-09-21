import EnvironmentVars from './EnvironmentVars'

it('returns base api url', async () => {
  process.env.REACT_APP_ENV = 'other'
  expect(EnvironmentVars.getBaseApiUrl()).not.toEqual(null)

  process.env.REACT_APP_ENV = 'dev'
  expect(EnvironmentVars.getBaseApiUrl()).not.toEqual(null)

  process.env.REACT_APP_ENV = 'test'
  expect(EnvironmentVars.getBaseApiUrl()).not.toEqual(null)

  process.env.REACT_APP_ENV = 'prod'
  expect(EnvironmentVars.getBaseApiUrl()).not.toEqual(null)
})
