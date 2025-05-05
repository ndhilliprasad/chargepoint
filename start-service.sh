#!/bin/sh
./gradlew clean build

docker build -f authentication-service/Dockerfile -t authentication-service:1.2 authentication-service
docker build -f transaction-service/Dockerfile -t transaction-service:1.2 transaction-service

docker-compose down -v
docker-compose up -d
