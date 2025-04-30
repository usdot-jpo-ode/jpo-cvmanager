import { HaasWebsocketLocationResponse } from '../../models/haas/HaasWebsocketLocation'
import { authApiHelper } from './api-helper-cviz'

class HaasApi {
  async getHaasLocationData({
    token,
    query_params,
    abortController,
  }: {
    token: string
    query_params?: Record<string, string>
    abortController?: AbortController
  }): Promise<HaasWebsocketLocationResponse> {
    var response = await authApiHelper.invokeApi({
      path: '/haas/locations',
      token: token,
      queryParams: query_params,
      abortController,
      failureMessage: 'Failed to retrieve haas location data',
      tag: 'haas',
    })
    return response ?? []
  }
}

export default new HaasApi()
