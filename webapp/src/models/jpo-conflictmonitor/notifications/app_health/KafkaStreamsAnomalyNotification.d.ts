type KafkaStreamsAnomalyNotification = MessageMonitor.Notification & {
  stateChange: KafkaStreamsStateChangeEvent
  exceptionEvent: KafkaStreamsUnhandledExceptionEvent
}
