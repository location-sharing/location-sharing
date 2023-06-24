#!/bin/bash


if [ $# -ne 1 ]
then
  echo "usage: $0 <registry_name>"
  exit 1
fi

registry=$1

name="frontend"

npm run build
echo "using image name $imageName"
docker build -t $name .

full_name="${registry}/location-sharing-app_${name}"
docker tag $name "$full_name"
docker push "$full_name"
