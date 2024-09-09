/// <reference path="Event.d.ts" />
type TimeChangeDetailsEvent = MessageMonitor.Event & {
  signalGroup: number
  firstSpatTimestamp: number
  secondSpatTimestamp: number
  firstTimeMarkType: string
  secondTimeMarkType: string
  firstConflictingTimemark: number
  secondConflictingTimemark: number
  firstState: J2735MovementPhaseState
  secondState: J2735MovementPhaseState
  firstConflictingUtcTimestamp: number
  secondConflictingUtcTimestamp: number
  source: string
}
