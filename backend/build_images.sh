#!/bin/bash

if [ $# -ne 1 ]
then
  echo "usage: $0 <registry_name>"
  exit 1
fi

registry=$1

connGw="connection-gateway"
userSvc="user-service"
groupSvc="group-service"
notifSvc="notification-service"

all="$connGw $userSvc $groupSvc $notifSvc"

docker build -t $connGw ./connection-gateway/
docker build -t $userSvc ./user-service/
docker build -t $groupSvc ./group-service/ 
docker build -t $notifSvc ./notification-service/

for i in $all
do
  full_name="${registry}/location-sharing-app_${i}"
  docker tag "$i" "$full_name"
  docker push "$full_name"
done