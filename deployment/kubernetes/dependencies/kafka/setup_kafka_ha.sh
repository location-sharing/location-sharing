#!/bin/bash

# https://github.com/bitnami/charts/tree/main/bitnami/kafka
# see chart-kafka version mappings
version="22.1.6"

helm install kafka-ha oci://registry-1.docker.io/bitnamicharts/kafka \
-n kafka \
--create-namespace \
--version "$version" \
-f kafka_helm_values.yaml

# connection string, on the specified port
# kafka-ha.kafka.svc.cluster.local:9092

kubectl apply -f kafka-ui.yaml