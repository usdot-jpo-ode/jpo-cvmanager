import './Event.d.ts'
type SignalGroupAlignmentEvent = MessageMonitor.Event & {
  source: string
  timestamp: number
  spatSignalGroupIds: Set<Integer>
  mapSignalGroupIds: Set<Integer>
}
