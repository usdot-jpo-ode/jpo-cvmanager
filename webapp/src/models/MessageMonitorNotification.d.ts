type MessageMonitorNotification =
  | IntersectionReferenceAlignmentNotification
  | SignalGroupAlignmentNotification
  | SpatBroadcastRateNotification
  | MapBroadcastRateNotification
  | ConnectionOfTravelNotification
  | SignalStateConflictNotification
  | TimeChangeDetailsNotification

// TODO: COnsider adding the following notification types:
//   | SpatMinimumDataNotification
//   | MapMinimumDataNotification
//   | StopLineStopNotification
//   | LaneDirectionOfTravelAssessmentNotification
