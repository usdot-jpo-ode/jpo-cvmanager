import './Event.d.ts'
type IntersectionReferenceAlignmentEvent = MessageMonitor.Event & {
  source: string
  timestamp: number
  spatRegulatorIntersectionIds: Set<RegulatorIntersectionId>
  mapRegulatorIntersectionIds: Set<RegulatorIntersectionId>
}
