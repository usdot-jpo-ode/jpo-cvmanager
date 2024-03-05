declare namespace MessageMonitor {
type Event = {
  logger: Logger
  eventGeneratedAt: number
  eventType: str
  intersectionID: number
  roadRegulatorID: number
  source: str
}
}