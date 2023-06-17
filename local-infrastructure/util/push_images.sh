#!/bin/bash

if [ $# -lt 3 ]
then
  echo "usage: $0 <registry_name> <image_prefix> <images>"
  exit 1
fi

registry=$1
prefix=$2
shift 2

echo "$registry"
echo "$prefix"

for image in $@
do
  full_name=${registry}/${prefix}_${image}
  echo "using full name $full_name"
  docker tag $image $full_name
  docker push $full_name
done
