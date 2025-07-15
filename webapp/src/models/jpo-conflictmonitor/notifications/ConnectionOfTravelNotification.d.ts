import './Notification.d.ts'
type ConnectionOfTravelNotification = MessageMonitor.Notification & {
  assessment: ConnectionOfTravelAssessment
  ingressLane: number
  egressLane: number
}
