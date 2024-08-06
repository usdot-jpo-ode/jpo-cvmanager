/// <reference path="Event.d.ts" />
type ConnectionOfTravelEvent = MessageMonitor.Event & {
  timestamp: number
  ingressLaneID: number
  egressLaneID: number
  source: string
  connectionID: number
}
