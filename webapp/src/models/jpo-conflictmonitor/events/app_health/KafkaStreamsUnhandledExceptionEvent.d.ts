
type KafkaStreamsUnhandledExceptionEvent = KafkaStreamsEvent  & {
  exception: Throwable
}