#!/bin/bash

kubectl create ns postgres

helm install postgres-ha oci://registry-1.docker.io/bitnamicharts/postgresql-ha \
-n postgres \
-f postgres_helm_values.yaml

# connection string will be, on the specified port
# postgres-ha-postgresql-ha-pgpool.postgres.svc.cluster.local:5432

kubectl apply -f pgadmin.yaml
