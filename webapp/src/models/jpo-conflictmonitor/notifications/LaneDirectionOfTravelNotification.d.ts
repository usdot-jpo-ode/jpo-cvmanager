/// <reference path="Notification.d.ts" />
type LaneDirectionOfTravelNotification = MessageMonitor.Notification  & {
  assessment: LaneDirectionOfTravelAssessment
}