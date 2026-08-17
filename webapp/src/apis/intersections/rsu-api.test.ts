import RsuApi from './rsu-api'

beforeEach(() => {
  fetchMock.mockClear()
  fetchMock.doMock()
})

it('Test getHistoricalRsuStatus', async () => {
  const expectedResponse = [
    {
      timestamp: 1717622387534,
      intersectionID: '1234',
      rsuIP: '10.0.0.1',
      temperature: 37,
      uptime: 1294615,
      mode: 4,
    },
  ]

  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  const startTime = new Date('2025-06-05T21:00:00Z')
  const endTime = new Date('2025-06-05T23:00:00Z')

  const result = await RsuApi.getHistoricalRsuStatus({
    token: 'testToken',
    rsuIp: '10.0.0.1',
    startTime,
    endTime,
  })

  expect(result).toEqual(expectedResponse)
  expect(fetchMock.mock.calls[0][0]).toContain('rsu-status/historical')
  expect(fetchMock.mock.calls[0][0]).toContain('rsuIp=10.0.0.1')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'Bearer testToken' })
})

it('Test getHistoricalRsuStatus with abort controller', async () => {
  const expectedResponse = [
    {
      timestamp: 1717622387534,
      intersectionID: '1234',
      rsuIP: '10.0.0.1',
      temperature: 37,
      uptime: 1294615,
      mode: 4,
    },
  ]

  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  const abortController = new AbortController()
  const startTime = new Date('2025-06-05T21:00:00Z')
  const endTime = new Date('2025-06-05T23:00:00Z')

  await RsuApi.getHistoricalRsuStatus({
    token: 'testToken',
    rsuIp: '10.0.0.1',
    startTime,
    endTime,
    abortController,
  })

  expect(fetchMock.mock.calls[0][1].signal).toBe(abortController.signal)
})

it('Test getLatestRsuStatus', async () => {
  const expectedResponse = {
    timestamp: 1717622387534,
    intersectionID: '1234',
    rsuIP: '10.0.0.1',
    temperature: 37,
    uptime: 1294615,
    mode: 4,
  }

  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  const result = await RsuApi.getLatestRsuStatus({
    token: 'testToken',
    rsuIp: '10.0.0.1',
  })

  expect(result).toEqual(expectedResponse)
  expect(fetchMock.mock.calls[0][0]).toContain('rsu-status/latest')
  expect(fetchMock.mock.calls[0][0]).toContain('rsuIp=10.0.0.1')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'Bearer testToken' })
})

it('Test getLatestRsuStatus with abort controller', async () => {
  const expectedResponse = {
    timestamp: 1717622387534,
    intersectionID: '1234',
    rsuIP: '10.0.0.1',
    temperature: 37,
    uptime: 1294615,
    mode: 4,
  }

  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  const abortController = new AbortController()

  await RsuApi.getLatestRsuStatus({
    token: 'testToken',
    rsuIp: '10.0.0.1',
    abortController,
  })

  expect(fetchMock.mock.calls[0][1].signal).toBe(abortController.signal)
})

it('Test getAggregatedRsuStatus', async () => {
  const expectedResponse = [
    {
      timestamp: 1717622387534,
      intersectionID: '1234',
      rsuIP: '10.0.0.1',
      temperature: 37,
      uptime: 1294615,
      mode: 4,
    },
  ]

  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  const startTime = new Date('2025-06-05T21:00:00Z')
  const endTime = new Date('2025-06-06T21:00:00Z')
  const intervalMinutes = 60

  const result = await RsuApi.getAggregatedRsuStatus({
    token: 'testToken',
    rsuIp: '10.0.0.1',
    startTime,
    endTime,
    intervalMinutes,
  })

  expect(result).toEqual(expectedResponse)
  expect(fetchMock.mock.calls[0][0]).toContain('rsu-status/aggregated')
  expect(fetchMock.mock.calls[0][0]).toContain('rsuIp=10.0.0.1')
  expect(fetchMock.mock.calls[0][1].method).toBe('GET')
  expect(fetchMock.mock.calls[0][1].headers).toStrictEqual({ Authorization: 'Bearer testToken' })
})

it('Test getAggregatedRsuStatus with abort controller', async () => {
  const expectedResponse = [
    {
      timestamp: 1717622387534,
      intersectionID: '1234',
      rsuIP: '10.0.0.1',
      temperature: 37,
      uptime: 1294615,
      mode: 4,
    },
  ]

  fetchMock.mockResponseOnce(JSON.stringify(expectedResponse))

  const abortController = new AbortController()
  const startTime = new Date('2025-06-05T21:00:00Z')
  const endTime = new Date('2025-06-06T21:00:00Z')

  await RsuApi.getAggregatedRsuStatus({
    token: 'testToken',
    rsuIp: '10.0.0.1',
    startTime,
    endTime,
    intervalMinutes: 60,
    abortController,
  })

  expect(fetchMock.mock.calls[0][1].signal).toBe(abortController.signal)
})
