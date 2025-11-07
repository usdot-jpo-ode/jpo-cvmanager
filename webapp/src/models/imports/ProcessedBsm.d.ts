type BsmFeatureCollection = {
  type: 'FeatureCollection'
  features: ProcessedBsmFeature[]
}

type ProcessedBsmFeature = {
  type: 'Feature'
  geometry: {
    type: 'Point'
    coordinates: [number, number]
  }
  properties: ProcessedBsmProperties
}

type ProcessedBsmProperties = {
  schemaVersion: number
  messageType: 'BSM'
  odeReceivedAt: string
  odeReceivedAtEpochSeconds: number
  timeStamp: string
  validationMessages: {
    message: string
    jsonPath: string
    schemaPath: string
  }[]
  accelSet: {
    accelLat: number
    accelLong: number
    accelVert: number
    accelYaw: number
  }
  accuracy: {
    semiMajor: number
    semiMinor: number
  }
  brakes: {
    wheelBrakes: {
      unavailable: boolean
      leftFront: boolean
      leftRear: boolean
      rightFront: boolean
      rightRear: boolean
    }
    traction: string
    abs: string
    scs: string
    brakeBoost: string
    auxBrakes: string
  }
  heading: number
  id: string
  msgCnt: number
  secMark: number
  size: {
    width: number
    length: number
  }
  speed: number
  transmission: string
}
