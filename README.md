This mono repo has three modules
1. common-core
2. authentication-service
3. transaction-service

## common-core
This module contains the common code that is used by the other two modules. It contains the following:
- Kafka producer and consumer configurations
- Common models

## authentication-service
This module has driver authentication. No rest APIs

## transaction-service
This module has rest API to authenticate the driver. This integrates with authentication-service with kafka request-response pattern

## How to run

Prerequisite : Docker is needed

Run the start-script.sh

```bash
chmod +x start-script.sh
./start-script.sh
```
docker-compose.yml has zookeeper, kafka, transaction-service and authentication-service

Application dockers will take sometime startup as it is kafka to be in a healthy state

Curl to test the APIs

### Valid Driver Request

```bash
curl --location --request POST 'http://localhost:7001/transaction/authorize' \
--header 'Content-Type: application/json' \
--data-raw '{
    "station_uuid" : "08286161-4c11-406b-a089-ac2ecff507f1",
    "driver_info" : {
        "id": "6af827f3-a412-4868-bf93-b179d421b148"
    }
}'
```

### Unknown Driver Request

```bash
curl --location --request POST 'http://localhost:7001/transaction/authorize' \
--header 'Content-Type: application/json' \
--data-raw '{
    "station_uuid" : "08286161-4c11-406b-a089-ac2ecff507f1",
    "driver_info" : {
        "id": "6af827f3-a412-4868-bf93-b179d421b1481"
    }
}'
```