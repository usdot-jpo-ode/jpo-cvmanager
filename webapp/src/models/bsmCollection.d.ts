type BsmFeatureCollection = {
  type: 'FeatureCollection'
  features: BsmFeature[]
}

type BsmFeature = {
  type: 'Feature'
  properties: J2735BsmCoreData & bsmReceivedAt
  geometry: PointGemetry
}

type PointGemetry = {
  type: 'Point'
  coordinates: number[]
}

type bsmReceivedAt = {
  odeReceivedAt: number
}
