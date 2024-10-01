/// <reference path="Event.d.ts" />
type SignalStateEvent = MessageMonitor.Event & {
  timestamp: number
  ingressLane: number
  egressLane: number
  connectionID: number
  eventState: J2735MovementPhaseState
  vehicleID: string
  latitude: number
  longitude: number
  heading: number
  speed: number
  signalGroup: number
}
