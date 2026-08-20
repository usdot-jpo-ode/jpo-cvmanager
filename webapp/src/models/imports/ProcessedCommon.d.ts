type ProcessedTransmissionState =
  | 'neutral'
  | 'park'
  | 'forwardGears'
  | 'reverseGears'
  | 'reserved1'
  | 'reserved2'
  | 'reserved3'
  | 'unavailable'

type ProcessedValidationMessage = {
  message: string
  jsonPath: string
  schemaPath: string
  exception?: string
}