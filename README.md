# Specmatic Sample: Spring Boot Order API

Table of Contents
<!-- TOC -->
* [Specmatic Sample: Spring Boot Order API](#specmatic-sample-spring-boot-order-api)
  * [Background](#background)
  * [Tech](#tech)
  * [Run Contract Tests](#run-contract-tests)
    * [1. Using Maven](#1-using-maven)
    * [3. Using Docker Desktop](#3-using-docker-desktop)
  * [For More Info](#for-more-info)
<!-- TOC -->

## Background
This sample project demonstrates how Specmatic can be used to contract test an API in isolation before integrating with consumers.

The interaction between Order API in this project and it's consumers is governed by this [OpenAPI specification.](https://github.com/specmatic/specmatic-order-contracts/blob/main/io/specmatic/examples/store/openapi/api_order_v3.yaml).

## Tech
1. Spring Boot service written in Java
2. Specmatic
3. Docker Desktop (for running contract tests with containers)
4. openjdk version 17.0.17

## Run Contract Tests
Specmatic contract tests use the [specmatic.yaml](specmatic.yaml) configuration file to start the required Specmatic stub before verifying the Order API.

### 1. Using Maven

For **Unix based systems** and **Windows PowerShell**:
```shell
./mvnw test -Dtest=ContractTest
```

For **Windows Command Prompt**:
```shell
mvnw.cmd test -Dtest=ContractTest
```

After the tests complete, view the report at [build/reports/specmatic/html/index.html](build/reports/specmatic/html/index.html).

#### Contract Tests Using TestContainers
 
1. Start docker locally

For **Unix based systems** and **Windows PowerShell**:
```shell
./mvnw test -Dtest=ContractTestUsingTestContainerTest
```

For **Windows Command Prompt**:
```shell
mvnw.cmd test -Dtest=ContractTestUsingTestContainerTest
```

### 3. Using Docker Desktop

For **Unix based systems** and **Windows PowerShell**:

```shell
# Start the service
./mvnw spring-boot:run
```

```shell
# Run the contract tests
docker run --rm --network host -v "$(pwd)/specmatic.yaml:/usr/src/app/specmatic.yaml" -v "$(pwd)/build/reports/specmatic:/usr/src/app/build/reports/specmatic" specmatic/specmatic test --port=8090
```
For mac user 
```shell
docker run --rm \
  -v "$(pwd)/specmatic.yaml:/usr/src/app/specmatic.yaml" \
  -v "$(pwd)/build/reports/specmatic:/usr/src/app/build/reports/specmatic" \
  specmatic/specmatic test \
  --host=host.docker.internal \
  --port=8090
```
For **Windows Command Prompt**:
```shell
# Start the service
mvnw.cmd spring-boot:run
```

```shell
# Run the contract tests
docker run --rm --network host -v "%cd%/specmatic.yaml:/usr/src/app/specmatic.yaml" -v "%cd%/build/reports/specmatic:/usr/src/app/build/reports/specmatic" specmatic/specmatic test --port=8090
```

## For More Info
* [Specmatic Website](https://specmatic.io)
* [Specmatic Documentation](https://docs.specmatic.io)
