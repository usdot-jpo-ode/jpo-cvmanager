/// <reference path="Event.d.ts" />
type StopLineStopEvent = MessageMonitor.Event & {
  source: str
  ingressLane: number
  egressLane: number
  connectionID: number
  initialEventState: J2735MovementPhaseState
  initialTimestamp: number
  finalEventState: J2735MovementPhaseState
  finalTimestamp: number
  vehicleID: str
  latitude: number
  longitude: number
  heading: number
  signalGroup: number
  timeStoppedDuringRed: number
  timeStoppedDuringYellow: number
  timeStoppedDuringGreen: number
}