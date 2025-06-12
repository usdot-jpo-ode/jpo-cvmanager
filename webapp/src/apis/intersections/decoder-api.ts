import { authApiHelper } from './api-helper-cviz'

class DecoderApi {
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
      path: 'asn1/decoder/raw',
      token: token,
      method: 'POST',
      body: {
        asn1Message: data,
        type: type,
      },
      tag: 'intersection',
      abortController,
    })
    return response as DecoderApiResponseGeneric | undefined
  }
}

export default new DecoderApi()
