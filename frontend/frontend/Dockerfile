FROM caddy:2.7-builder AS builder

RUN xcaddy build \
    --with github.com/caddy-dns/duckdns

FROM caddy:2.7
COPY --from=builder /usr/bin/caddy /usr/bin/caddy
RUN mkdir -p /srv
COPY ./Caddyfile /etc/caddy/Caddyfile
COPY ./build/ /srv/
