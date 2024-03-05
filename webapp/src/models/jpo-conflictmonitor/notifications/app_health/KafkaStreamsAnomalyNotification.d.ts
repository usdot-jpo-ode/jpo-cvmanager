/// <reference path="Notification.d.ts" />
type KafkaStreamsAnomalyNotification = MessageMonitor.Notification  & {
  stateChange: KafkaStreamsStateChangeEvent
  exceptionEvent: KafkaStreamsUnhandledExceptionEvent
}