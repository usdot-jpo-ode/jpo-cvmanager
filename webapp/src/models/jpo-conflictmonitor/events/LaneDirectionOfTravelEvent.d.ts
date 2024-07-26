/// <reference path="Event.d.ts" />
type LaneDirectionOfTravelEvent = MessageMonitor.Event & {
  timestamp: number
  laneID: number
  laneSegmentNumber: number
  laneSegmentInitialLatitude: number
  laneSegmentInitialLongitude: number
  laneSegmentFinalLatitude: number
  laneSegmentFinalLongitude: number
  expectedHeading: number
  medianVehicleHeading: number
  medianDistanceFromCenterline: number
  aggregateBSMCount: number
  source: str
}