import { authApiHelper } from './api-helper-cviz'

class DecoderApi {
  async getIntersections({ token }): Promise<IntersectionReferenceData[]> {
    var response = await authApiHelper.invokeApi({
      path: '/intersection/list',
      token: token,
      failureMessage: 'Failed to retrieve intersection list',
      tag: 'intersection',
    })
    return response ?? []
  }

  async submitDecodeRequest({
    token,
    data,
    type,
  }: {
    token: string
    data: string
    type?: DECODER_MESSAGE_TYPE
  }): Promise<DecoderApiResponseGeneric | undefined> {
    var response = await authApiHelper.invokeApi({
      path: '/decoder/upload',
      token: token,
      method: 'POST',
      body: {
        asn1Message: data,
        type: type,
      },
      tag: 'intersection',
    })
    return response as DecoderApiResponseGeneric | undefined
  }
}

export default new DecoderApi()
