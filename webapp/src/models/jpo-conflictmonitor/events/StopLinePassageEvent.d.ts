import './Event.d.ts'
type StopLinePassageEvent = MessageMonitor.Event & {
  source: string
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
