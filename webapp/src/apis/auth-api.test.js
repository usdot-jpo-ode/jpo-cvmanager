import AuthApi from './auth-api'

beforeEach(() => {
  fetchMock.mockClear()
  fetchMock.doMock()
})

it('Test Auth login', () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  AuthApi.logIn('testToken').then((response) => {
    expect(response).toEqual(expectedResponse)
  })
})
