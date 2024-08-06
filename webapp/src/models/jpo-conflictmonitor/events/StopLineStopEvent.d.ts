/// <reference path="Event.d.ts" />
type StopLineStopEvent = MessageMonitor.Event & {
  source: string
  ingressLane: number
  egressLane: number
  connectionID: number
  initialEventState: J2735MovementPhaseState
  initialTimestamp: number
  finalEventState: J2735MovementPhaseState
  finalTimestamp: number
  vehicleID: string
  latitude: number
  longitude: number
  heading: number
  signalGroup: number
  timeStoppedDuringRed: number
  timeStoppedDuringYellow: number
  timeStoppedDuringGreen: number
}
