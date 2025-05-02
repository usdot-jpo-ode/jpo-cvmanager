declare namespace MessageMonitor {
  type Event = {
    logger: any
    eventGeneratedAt: number
    eventType: string
    intersectionID: number
    source: string
  }
}
