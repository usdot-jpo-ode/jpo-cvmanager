import { authApiHelper } from './api-helper-cviz'

class DecoderApi {
  async getIntersections({
    token,
    abortController,
  }: {
    token: string
    abortController?: AbortController
  }): Promise<IntersectionReferenceData[]> {
    var response = await authApiHelper.invokeApi({
      path: '/intersection/list',
      token: token,
      abortController,
      failureMessage: 'Failed to retrieve intersection list',
    })
    return response ?? []
  }

  async submitDecodeRequest({
    token,
    data,
    type,
    abortController,
  }: {
    token: string
    data: string
    type?: DECODER_MESSAGE_TYPE
    abortController?: AbortController
  }): Promise<DecoderApiResponseGeneric | undefined> {
    var response = await authApiHelper.invokeApi({
      path: '/decoder/upload',
      token: token,
      method: 'POST',
      body: {
        asn1Message: data,
        type: type,
      },
      abortController,
    })
    return response as DecoderApiResponseGeneric | undefined
  }
}

export default new DecoderApi()
