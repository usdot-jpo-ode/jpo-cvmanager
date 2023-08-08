import GoogleAuthApi from './google-auth-api'

beforeEach(() => {
  fetchMock.mockClear()
  fetchMock.doMock()
})

it('Test Google Auth login', () => {
  const testData = { data: 'Test JSON' }
  const expectedResponse = { json: { data: 'Test JSON' }, status: 200}
  fetchMock.mockResponseOnce(JSON.stringify(testData))

  GoogleAuthApi.logIn('testToken').then((response) => {
    expect(response).toEqual(expectedResponse)
  })
})
