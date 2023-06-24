#!/bin/bash

./connection-gateway/gradlew :connection-gateway:installDist
./notification-service/gradlew :notification-service:bootJar
./user-service/gradlew :user-service:bootJar
./group-service/gradlew :group-service:bootJar