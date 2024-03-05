
type Intersection = {
  ingressLanes: Lane[]
  egressLanes: Lane[]
  stopLines: IntersectionLine[]
  startLines: IntersectionLine[]
  referencePoint: Coordinate
  intersectionId: number
  roadRegulatorId: number
  laneConnections: LaneConnection[]
  ingressLaneId: number
  egressLaneId: number
}