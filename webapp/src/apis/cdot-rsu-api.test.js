import CdotApi from './cdot-rsu-api'
import EnvironmentVars from '../EnvironmentVars'
import ApiHelper from './api-helper'

beforeEach(() => {
  fetchMock.mockClear()
  fetchMock.doMock()
  EnvironmentVars.rsuInfoEndpoint = 'REACT_APP_ENV/rsuinfo'
  EnvironmentVars.rsuOnlineEndpoint = 'REACT_APP_ENV/rsu-online-status'
  EnvironmentVars.rsuCountsEndpoint = 'REACT_APP_ENV/rsucounts'
  EnvironmentVars.rsuCommandEndpoint = 'REACT_APP_ENV/rsu-command'
  EnvironmentVars.wzdxEndpoint = 'REACT_APP_ENV/wzdx-feed'
  EnvironmentVars.rsuMapInfoEndpoint = 'REACT_APP_ENV/rsu-map-info'
  EnvironmentVars.bsmDataEndpoint = 'REACT_APP_ENV/rsu-bsm-data'
  EnvironmentVars.issScmsStatusEndpoint = 'REACT_APP_ENV/iss-scms-status'
  EnvironmentVars.ssmSrmEndpoint = 'REACT_APP_ENV/rsu-ssm-srm-data'
  EnvironmentVars.googleAuthEndpoint = 'REACT_APP_ENV/rsu-google-auth'
  EnvironmentVars.adminAddRsu = 'REACT_APP_ENV/admin-new-rsu'
  EnvironmentVars.adminRsu = 'REACT_APP_ENV/admin-rsu'
  EnvironmentVars.adminAddUser = 'REACT_APP_ENV/admin-new-user'
  EnvironmentVars.adminUser = 'REACT_APP_ENV/admin-user'
  EnvironmentVars.adminAddOrg = 'REACT_APP_ENV/admin-new-org'
  EnvironmentVars.adminOrg = 'REACT_APP_ENV/admin-org'
})

it('Test apiHelper mock', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuInfo('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuInfoEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuInfo', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuInfo('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuInfoEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuInfo With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuInfo('testToken', 'testOrg', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuInfoEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuOnline', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuOnline('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuOnlineEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuOnline With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuOnline('testToken', 'testOrg', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuOnlineEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuCounts', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuCounts('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuCountsEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuCounts With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuCounts('testToken', 'testOrg', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuCountsEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuGoogleAuth', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuGoogleAuth('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.googleAuthEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuGoogleAuth With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuGoogleAuth('testToken', 'testOrg', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.googleAuthEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuCommand', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuCommand('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuCommandEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuCommand With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuCommand('testToken', 'testOrg', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuCommandEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuMapInfo', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuMapInfo('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuMapInfoEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getRsuMapInfo With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getRsuMapInfo('testToken', 'testOrg', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuMapInfoEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getSsmSrmData', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getSsmSrmData('testToken')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.ssmSrmEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken' })
})

it('Test getSsmSrmData With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getSsmSrmData('testToken', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.ssmSrmEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken' })
})

it('Test getIssScmsStatus', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getIssScmsStatus('testToken', 'testOrg')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.issScmsStatusEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getIssScmsStatus With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getIssScmsStatus('testToken', 'testOrg', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.issScmsStatusEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken', Organization: 'testOrg' })
})

it('Test getWzdxData', async () => {
  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getWzdxData('testToken')
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.wzdxEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken' })
})

it('Test getWzdxData With Params', async () => {
  // Set url_ext and query_params
  const url_ext = 'url_ext'
  const query_params = { query_param: 'test' }

  const expectedResponse = { data: 'Test JSON' }
  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))
  const actualResponse = await CdotApi.getWzdxData('testToken', url_ext, query_params)
  expect(actualResponse).toEqual(expectedResponse)

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.wzdxEndpoint + url_ext + '?query_param=test')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'testToken' })
})

it('Test postBsmData', async () => {
  const body = {
    data: 'Test JSON',
  }
  fetchMock.mockResponseOnce(JSON.stringify(body))
  const actualResponse = await CdotApi.postBsmData('testToken', body)
  expect(actualResponse).toEqual({
    body: body,
    message: undefined,
    status: 200,
  })

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.bsmDataEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('POST')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({
    Authorization: 'testToken',
    'Content-Type': 'application/json',
  })
})

it('Test postBsmData With Params', async () => {
  // Set url_ext
  const url_ext = 'url_ext'
  const body = {
    data: 'Test JSON',
  }

  fetchMock.mockResponseOnce(JSON.stringify(body))
  const actualResponse = await CdotApi.postBsmData('testToken', body, url_ext)
  expect(actualResponse).toEqual({
    body: body,
    message: undefined,
    status: 200,
  })

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.bsmDataEndpoint + url_ext)
  expect(fetchMock.mock.calls[0][1].method).toBe('POST')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({
    Authorization: 'testToken',
    'Content-Type': 'application/json',
  })
})

it('Test postRsuData', async () => {
  const body = {
    data: 'Test JSON',
  }

  fetchMock.mockResponseOnce(JSON.stringify(body))
  const actualResponse = await CdotApi.postRsuData('testToken', 'testOrg', body)
  expect(actualResponse).toEqual({
    body: body,
    message: undefined,
    status: 200,
  })

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuCommandEndpoint)
  expect(fetchMock.mock.calls[0][1].method).toBe('POST')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({
    Authorization: 'testToken',
    'Content-Type': 'application/json',
    Organization: 'testOrg',
  })
})

it('Test postRsuData With Params', async () => {
  // Set url_ext
  const url_ext = 'url_ext'
  const body = {
    data: 'Test JSON',
  }

  fetchMock.mockResponseOnce(JSON.stringify(body))
  const actualResponse = await CdotApi.postRsuData('testToken', 'testOrg', body, url_ext)
  expect(actualResponse).toEqual({
    body: body,
    message: undefined,
    status: 200,
  })

  expect(fetchMock.mock.calls[0][0]).toBe(EnvironmentVars.rsuCommandEndpoint + url_ext)
  expect(fetchMock.mock.calls[0][1].method).toBe('POST')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({
    Authorization: 'testToken',
    'Content-Type': 'application/json',
    Organization: 'testOrg',
  })
})
