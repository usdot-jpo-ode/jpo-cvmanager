import GoogleAuthApi from './google-auth-api'

beforeEach(() => {
  // if you have an existing `beforeEach` just add the following line to it
  fetchMock.doMock()
})

it('Test Google Auth login', () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  GoogleAuthApi.logIn('testToken').then((response) => {
    expect(response).toEqual(expectedResponse)
  })
})
