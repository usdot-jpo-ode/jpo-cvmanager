/// <reference path="Event.d.ts" />
type IntersectionReferenceAlignmentEvent = MessageMonitor.Event & {
  source: str
  timestamp: number
  spatRegulatorIntersectionIds: Set<RegulatorIntersectionId>
  mapRegulatorIntersectionIds: Set<RegulatorIntersectionId>
}