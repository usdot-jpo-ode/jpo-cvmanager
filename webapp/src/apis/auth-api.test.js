import AuthApi from './auth-api'

beforeEach(() => {
  // if you have an existing `beforeEach` just add the following line to it
  fetchMock.doMock()
})

it('Test Auth login', () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  AuthApi.logIn('testToken').then((response) => {
    expect(response).toEqual(expectedResponse)
  })
})
