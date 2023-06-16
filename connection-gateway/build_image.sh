#!/bin/bash

if [ -z $1 ]
then
  imageName=$(basename $(pwd))
else
  imageName="$1"
fi

# gets the project name from settings.gradle (needed to run the output of the installDir command)
projectName=$(cat settings.gradle* | grep rootProject.name | cut -d'=' -f2 | tr -d ' ' | tr -d "\"" | tr -d "'")

# build the jar here because of external dependencies (commons)
./gradlew installDist

echo "using image name $imageName"
docker build --build-arg projectName=$projectName -t $imageName .
