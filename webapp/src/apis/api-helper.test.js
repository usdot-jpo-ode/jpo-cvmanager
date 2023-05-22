import ApiHelper from './api-helper'

beforeEach(() => {
  // if you have an existing `beforeEach` just add the following line to it
  fetchMock.doMock()
})

it('Test fetch request', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  const actualResponse = await ApiHelper._getData({ url: 'https://test.com', token: 'testToken' })

  expect(actualResponse).toEqual(expectedResponse)
})
