import './Notification.d.ts'
type LaneDirectionOfTravelNotification = MessageMonitor.Notification & {
  assessment: LaneDirectionOfTravelAssessment
}
