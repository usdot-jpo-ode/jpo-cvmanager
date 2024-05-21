type KafkaStreamsStateChangeEvent = KafkaStreamsEvent & {
  newState: string
  oldState: string
}
