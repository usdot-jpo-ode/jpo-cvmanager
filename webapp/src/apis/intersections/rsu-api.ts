import { authApiHelper } from './api-helper-cviz'

// TypeScript type for RSU State (should match your backend model)
export type RsuState = {
  timestamp: number
  intersectionID: string
  rsuIP: string
  temperature: number
  uptime: number
  mode: number
}

class RsuApi {
  // Fetch historical RSU states for a given RSU IP and time range
  async getHistoricalRsuStatus({
    token,
    rsuIp,
    startTime,
    endTime,
    abortController,
  }: {
    token: string
    rsuIp: string
    startTime: Date
    endTime: Date
    abortController?: AbortController
  }): Promise<RsuState[] | undefined> {
    const queryParams: Record<string, string> = {
      rsuIp,
      startTime: startTime.getTime().toString(),
      endTime: endTime.getTime().toString(),
    }

    const response = await authApiHelper.invokeApi({
      path: `/data/rsu-status/historical`,
      token,
      queryParams,
      abortController,
      failureMessage: 'Failed to fetch historical RSU status',
      tag: 'rsu',
    })

    return response
  }

  // Fetch the latest RSU state for a given RSU IP
  async getLatestRsuStatus({
    token,
    rsuIp,
    abortController,
  }: {
    token: string
    rsuIp: string
    abortController?: AbortController
  }): Promise<RsuState | undefined> {
    const queryParams: Record<string, string> = { rsuIp }

    const response = await authApiHelper.invokeApi({
      path: `/data/rsu-status/latest`,
      token,
      queryParams,
      abortController,
      failureMessage: 'Failed to fetch latest RSU status',
      tag: 'rsu',
    })

    return response
  }

  async getAggregatedRsuStatus({
    token,
    rsuIp,
    startTime,
    endTime,
    intervalMinutes,
    abortController,
  }: {
    token: string
    rsuIp: string
    startTime: Date
    endTime: Date
    intervalMinutes: number
    abortController?: AbortController
  }): Promise<RsuState[] | undefined> {
    const queryParams: Record<string, string> = {
      rsuIp,
      startTime: startTime.getTime().toString(),
      endTime: endTime.getTime().toString(),
      intervalMinutes: intervalMinutes.toString(),
    }

    const response = await authApiHelper.invokeApi({
      path: `/data/rsu-status/aggregated`,
      token,
      queryParams,
      abortController,
      failureMessage: 'Failed to fetch aggregated RSU status',
      tag: 'rsu',
    })

    return response
  }
}

export default new RsuApi()
