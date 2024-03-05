
type LaneConnection = {
  referencePoint: OdePosition3D
  ingress: J2735GenericLane
  egress: J2735GenericLane
  ingressPath: number[][]
  connectingPath: number[][]
  egressPath: number[][]
  geometryFactory: GeometryFactory
  signalGroup: number
  interpolationPoints: number
  DEFAULT_INTERPOLATION_POINTS: number
}