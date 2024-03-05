
type LaneDirectionOfTravelAssessmentGroup = {
  laneID: number
  segmentID: number
  inToleranceEvents: number
  outOfToleranceEvents: number
  medianInToleranceHeading: number
  medianInToleranceCenterlineDistance: number
  medianHeading: number
  medianCenterlineDistance: number
  expectedHeading: number
  tolerance: number
  distanceFromCenterlineTolerance: number
}