#!/bin/bash

# install cert-manager
# helm repo add jetstack https://charts.jetstack.io
helm repo update

# see https://cert-manager.io/docs/installation/supported-releases/
version='v1.12.0'

helm install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version "$version" \
  --set 'extraArgs={--dns01-recursive-nameservers=8.8.8.8:53\,1.1.1.1:53}' \
  --set installCRDs=true

