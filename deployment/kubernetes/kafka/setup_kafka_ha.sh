#!/bin/bash

kubectl create ns kafka

helm install kafka-ha oci://registry-1.docker.io/bitnamicharts/kafka \
-n kafka \
-f kafka_helm_values.yaml

# connection string, on the specified port
# kafka-ha.kafka.svc.cluster.local:9092

kubectl apply -f kafka-ui.yaml