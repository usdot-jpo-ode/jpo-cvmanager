type BroadcastRateEvent = {
  topicName: string
  source: string
  timePeriod: ProcessingTimePeriod
  numberOfMessages: number
}
