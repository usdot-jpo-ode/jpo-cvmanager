import AuthApi from './auth-api'

beforeEach(() => {
  fetchMock.mockClear()
  fetchMock.doMock()
})

it('Test Auth login', async () => {
  const expectedFetchResponse = { content: 'content' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedFetchResponse))

  const expectedResponse = { json: expectedFetchResponse, status: 200 }

  const response = await AuthApi.logIn('testToken')
  expect(response).toEqual(expectedResponse)
})
