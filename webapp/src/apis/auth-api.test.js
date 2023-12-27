import AuthApi from './auth-api'
import EnvironmentVars from '../EnvironmentVars'

it('Test AuthApi logIn method', async () => {
  const testToken = 'testToken'
  const expectedFetchResponse = { content: 'content' }
  const expectedResponse = { json: expectedFetchResponse, status: 200 }

  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve(expectedFetchResponse),
      status: 200,
    })
  )

  const response = await AuthApi.logIn(testToken)

  expect(global.fetch).toHaveBeenCalledTimes(1)
  expect(global.fetch).toHaveBeenCalledWith(EnvironmentVars.authEndpoint, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      Authorization: testToken,
    },
  })
  expect(response).toEqual(expectedResponse)
})

it('Test AuthApi logIn method with non-200 response', async () => {
  const testToken = 'testToken'
  const expectedFetchResponse = { error: 'Unauthorized' }
  const expectedResponse = { json: expectedFetchResponse, status: 401 }

  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve(expectedFetchResponse),
      status: 401,
    })
  )

  try {
    const response = await AuthApi.logIn(testToken)
  } catch (error) {
    expect(error).toEqual(expectedResponse)
  }

  expect(global.fetch).toHaveBeenCalledTimes(1)
  expect(global.fetch).toHaveBeenCalledWith(EnvironmentVars.authEndpoint, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      Authorization: testToken,
    },
  })
})
