type GraphArrayDataType = {
  id: number
  ConnectionOfTravelEventCount: number
  IntersectionReferenceAlignmentEventCount: number
  LaneDirectionOfTravelEventCount: number
  ProcessingTimePeriodCount: number
  SignalGroupAlignmentEventCount: number
  SignalStateConflictEventCount: number
  SignalStateEventCount: number
  SignalStateStopEventCount: number
  TimeChangeDetailsEventCount: number
  MapMinimumDataEventCount: number
  SpatMinimumDataEventCount: number
  MapBroadcastRateEventCount: number
  SpatBroadcastRateEventCount: number
}

type EVENT_TYPES =
  | 'ALL'
  | 'ConnectionOfTravelEvent'
  | 'IntersectionReferenceAlignmentEvent'
  | 'LaneDirectionOfTravelEvent'
  | 'ProcessingTimePeriod'
  | 'SignalGroupAlignmentEvent'
  | 'SignalStateConflictEvent'
  | 'SignalStateEvent'
  | 'SignalStateStopEvent'
  | 'TimeChangeDetailsEvent'
  | 'MapMinimumDataEvent'
  | 'SpatMinimumDataEvent'
  | 'MapBroadcastRateEvent'
  | 'SpatBroadcastRateEvent'
