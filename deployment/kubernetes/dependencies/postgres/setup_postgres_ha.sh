#!/bin/bash

# https://github.com/bitnami/charts/tree/main/bitnami/postgresql-ha

# see chart - postgres version pairs before (this installs postgres v15.3.0)
version="11.7.6"

helm install postgres-ha oci://registry-1.docker.io/bitnamicharts/postgresql-ha \
-n postgres \
--create-namespace \
--version "$version" \
-f postgres_helm_values.yaml

# connection string will be, on the specified port
# postgres-ha-postgresql-ha-pgpool.postgres.svc.cluster.local:5432

kubectl apply -f pgadmin.yaml
