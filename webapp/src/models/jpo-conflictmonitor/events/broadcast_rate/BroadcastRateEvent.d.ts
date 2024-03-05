
type BroadcastRateEvent = {
  topicName: str
  source: str
  timePeriod: ProcessingTimePeriod
  numberOfMessages: number
}