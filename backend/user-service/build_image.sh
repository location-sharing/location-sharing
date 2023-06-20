#!/bin/bash

if [ -z $1 ]
then
  imageName=$(basename $(pwd))
else
  imageName="$1"
fi

# build the jar here because of external dependencies (commons)
./gradlew bootJar

echo "using image name $imageName"
docker build -t $imageName .
