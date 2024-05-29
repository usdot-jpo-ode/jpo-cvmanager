FROM confluentinc/cp-kafka-connect:6.1.9

COPY connect_start.sh /scripts/connect_start.sh

RUN confluent-hub install --no-prompt mongodb/kafka-connect-mongodb:1.8.0
RUN confluent-hub install --no-prompt confluentinc/connect-transforms:1.4.3