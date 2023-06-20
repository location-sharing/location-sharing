#!/bin/sh

# remove necessary zookeper environment variable
sed -i '/KAFKA_ZOOKEEPER_CONNECT/d' /etc/confluent/docker/configure

# remove checking if zookeper is ready
sed -i 's/cub zk-ready/echo ignore zk-ready/' /etc/confluent/docker/ensure

# KRaft-required: format storage with new cluster id 
echo "kafka-storage format --ignore-formatted -t NqnEdODVKkiLTfJvqd1uqQ== -c /etc/kafka/kafka.properties" >> /etc/confluent/docker/ensure
