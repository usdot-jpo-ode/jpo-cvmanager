/// <reference path="Event.d.ts" />
type SignalGroupAlignmentEvent = MessageMonitor.Event & {
  source: str
  timestamp: number
  spatSignalGroupIds: Set<Integer>
  mapSignalGroupIds: Set<Integer>
}