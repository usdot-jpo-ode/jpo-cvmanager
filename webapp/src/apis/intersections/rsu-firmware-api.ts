import { ApiMsgRespWithCodes, RsuUpgradeCheckPostBody, RsuUpgradePostBody } from '../../models/RsuApi'
import { authApiHelper } from './api-helper-cviz'

class RsuFirmwareApi {
  async postRsuUpgradeData(
    token: string,
    body: RsuUpgradePostBody | RsuUpgradeCheckPostBody,
    url_ext = ''
  ): Promise<ApiMsgRespWithCodes<any> | null> {
    const response = await authApiHelper.invokeApi({
      path: `/devices/rsus/upgrade${url_ext}`,
      method: 'POST',
      token,
      body,
      tag: 'rsu',
      toastOnFailure: false,
      returnErrorBody: true,
      failureMessage: 'Failed to submit RSU firmware upgrade request',
    })

    if (!response) {
      return null
    }

    if (response.__isErrorResponse) {
      return {
        body: response.body,
        status: response.status,
        message: response.body?.detail ?? `Request failed with status ${response.status}`,
      }
    }

    return {
      body: response,
      status: 200,
      message: response?.message,
    }
  }
}

export default new RsuFirmwareApi()
