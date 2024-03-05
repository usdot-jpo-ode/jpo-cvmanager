
type KafkaStreamsStateChangeEvent = KafkaStreamsEvent  & {
  newState: str
  oldState: str
}