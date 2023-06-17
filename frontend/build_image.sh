#!/bin/bash

if [ -z $1 ]
then
  imageName=$(basename $(pwd))
else
  imageName="$1"
fi

npm run build
echo "using image name $imageName"
docker build -t $imageName .
