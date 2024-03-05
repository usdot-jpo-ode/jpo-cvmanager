type SpatSignalGroups = {
  [datetime: number]: SpatSignalGroup[]
}

type SpatSignalGroup = {
  signalGroup: number
  state: SignalState
}

type SignalState =
  | null
  | 'UNAVAILABLE'
  | 'DARK'
  | 'STOP_THEN_PROCEED'
  | 'STOP_AND_REMAIN'
  | 'PRE_MOVEMENT'
  | 'PERMISSIVE_MOVEMENT_ALLOWED'
  | 'PROTECTED_MOVEMENT_ALLOWED'
  | 'PERMISSIVE_CLEARANCE'
  | 'PROTECTED_CLEARANCE'
  | 'CAUTION_CONFLICTING_TRAFFIC'

type SignalStateFeatureCollection = {
  type: 'FeatureCollection'
  features: SignalStateFeature[]
}

type SignalStateFeature = {
  type: 'Feature'
  properties: SignalStateProperties
  geometry: PointGemetry
}

type SignalStateProperties = {
  signalGroup: number
  intersectionId?: number
  signalState: SignalState
  orientation: number
}
