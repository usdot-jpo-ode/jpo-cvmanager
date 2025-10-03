type DECODER_MESSAGE_TYPE = 'SPAT' | 'MAP' | 'BSM'

type DecoderApiResponseGeneric = {
  type: DECODER_MESSAGE_TYPE
  payload: any
  decodeTime: number
  decodeErrors: string
  asn1Text: string
  processedMap: ProcessedMap | undefined
  processedSpat: ProcessedSpat | undefined
  bsm: ProcessedBsmFeature | undefined
}

type DecoderApiResponseSpat = DecoderApiResponseGeneric & {
  type: 'SPAT'
  payload: ProcessedSpat
}
type DecoderApiResponseMap = DecoderApiResponseGeneric & {
  type: 'MAP'
  payload: ProcessedMap
}
type DecoderApiResponseBsm = DecoderApiResponseGeneric & {
  type: 'BSM'
  payload: ProcessedBsmFeature
}

type DECODER_PROGRESS_TYPE = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ERROR'

type DecoderDataEntry = {
  id: string
  type: DECODER_MESSAGE_TYPE
  status: DECODER_PROGRESS_TYPE
  selected: boolean
  isGreyedOut: boolean
  timestamp?: number | undefined
  text: string
  decodedResponse: DecoderApiResponseGeneric | undefined
}
