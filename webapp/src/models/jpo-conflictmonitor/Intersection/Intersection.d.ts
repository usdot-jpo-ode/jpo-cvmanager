type Intersection = {
  ingressLanes: Lane[]
  egressLanes: Lane[]
  stopLines: IntersectionLine[]
  startLines: IntersectionLine[]
  referencePoint: Coordinate
  intersectionId: number
  laneConnections: LaneConnection[]
  ingressLaneId: number
  egressLaneId: number
}
