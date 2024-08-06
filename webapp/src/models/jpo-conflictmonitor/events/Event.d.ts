declare namespace MessageMonitor {
  type Event = {
    logger: Logger
    eventGeneratedAt: number
    eventType: string
    intersectionID: number
    roadRegulatorID: number
    source: string
  }
}
