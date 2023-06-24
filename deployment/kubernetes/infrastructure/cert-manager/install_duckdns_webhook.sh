#!/bin/bash

# UNUSED IN CURRENT CONFIG

if [ $# -ne 2 ]
then
  echo "usage: $0 <duckdns_api_token> <email>"
  exit 1
fi

token=$1
email=$2


# install a duckdns dns resolver
# helm repo add ebrianne.github.io https://ebrianne.github.io/helm-charts
helm repo update 
helm install cert-manager-webhook-duckdns \
  --namespace cert-manager \
  --set duckdns.token=$token \
  --set clusterIssuer.production.create=false \
  --set clusterIssuer.staging.create=true \
  --set clusterIssuer.email=$email \
  --set logLevel=5 \
  ebrianne.github.io/cert-manager-webhook-duckdns