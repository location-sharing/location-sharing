helm update

version="v2.10.1"

helm install traefik traefik/traefik \
-n traefik-ingress \
--create-namespace \
--version "$version" \
-f traefik_values.yaml