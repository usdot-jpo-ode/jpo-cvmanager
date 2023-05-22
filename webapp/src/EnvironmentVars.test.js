import EnvironmentVars from './EnvironmentVars'

it('returns base api url', async () => {
  const url = EnvironmentVars.getBaseApiUrl()

  expect(url).not.toEqual(null)
})
