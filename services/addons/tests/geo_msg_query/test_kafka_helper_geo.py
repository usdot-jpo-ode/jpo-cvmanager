from unittest.mock import patch, MagicMock
import os
from images.geo_msg_query import kafka_helper


@patch.dict(
    os.environ,
    {
        "KAFKA_BROKERS": "your_kafka_brokers",
        "CONFLUENT": "true",
        "CONFLUENT_KEY": "your_key",
        "CONFLUENT_SECRET": "your_secret",
    },
)
@patch("images.geo_msg_query.kafka_helper.KafkaConsumer")
def test_create_consumer_confluent_geo(mock_kafkaConsumer):
    mock_instance = MagicMock()
    mock_kafkaConsumer.return_value = mock_instance
    topic = "test-topic"
    result = kafka_helper.create_consumer(topic)

    assert result == mock_instance
    mock_kafkaConsumer.assert_called_once_with(
        topic,
        group_id=f"{topic}-geo-query",
        security_protocol="SASL_SSL",
        sasl_mechanism="PLAIN",
        sasl_plain_username="your_key",
        sasl_plain_password="your_secret",
        bootstrap_servers="your_kafka_brokers",
    )


@patch.dict(
    os.environ,
    {
        "KAFKA_BROKERS": "your_kafka_brokers",
        "CONFLUENT": "false",
    },
)
@patch("images.geo_msg_query.kafka_helper.KafkaConsumer")
def test_create_consumer_without_confluent_geo(mock_consumer):
    mock_instance = MagicMock()
    mock_consumer.return_value = mock_instance

    topic = "test-topic"
    result = kafka_helper.create_consumer(topic)

    assert result == mock_instance
    mock_consumer.assert_called_once_with(
        topic,
        group_id=f"{topic}-geo-query",
        bootstrap_servers="your_kafka_brokers",
    )
