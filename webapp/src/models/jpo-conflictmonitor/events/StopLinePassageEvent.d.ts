/// <reference path="Event.d.ts" />
type StopLinePassageEvent = MessageMonitor.Event  & {
  source: str
  timestamp: number
  ingressLane: number
  egressLane: number
  connectionID: number
  eventState: J2735MovementPhaseState
  vehicleID: str
  latitude: number
  longitude: number
  heading: number
  speed: number
  signalGroup: number
}