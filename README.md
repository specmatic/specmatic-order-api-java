# Specmatic Sample Client Application

![HTML client talks to client API which talks to backend api](specmatic-sample-architecture.svg)

BFF = Backend For Frontend, the API invoked by the HTTP calls in the client HTML page (Website UI).

This project contains the product API, which is used by a small ecommerce client application.

Here is the [contract](https://github.com/specmatic/specmatic-order-contracts/blob/main/io/specmatic/examples/store/openapi/api_order_v3.yaml) governing the interaction of the client with the product API.

The architecture diagram was created using the amazing free online SVG editor at [Vectr](https://vectr.com).

### How to run the application manually?

#### 1. Build the project using maven:

- For unix platform and powerShell:<br/>
```shell
./mvnw clean install
```

- For windows command prompt:<br/>
```shell
mvnw.cmd clean install
```

#### 2. Run the application using maven:

- For unix platform and powerShell:<br/>
```shell
./mvnw spring-boot:run
```

- For windows command prompt:<br/>
```shell
mvnw.cmd spring-boot:run
```

### How to test the application?

#### 1. Using maven:
- For unix platform and powerShell:<br/>
```shell
./mvnw test
```

- For windows command prompt:<br/>
```shell
mvnw.cmd test
```

#### 2. Using Docker Desktop:

##### 1. Run the application using maven:

- For unix platform and powerShell:<br/>
```shell
./mvnw spring-boot:run
```

- For windows command prompt:<br/>
```shell
mvnw.cmd spring-boot:run
```

##### 2. Run the contract tests using Docker:

- For unix platform and powerShell:<br/>
```shell
docker run --rm -v --network host "$(pwd)/specmatic.yaml:/usr/src/app/specmatic.yaml" -v "$(pwd)/build/reports/specmatic:/usr/src/app/build/reports/specmatic"  specmatic/specmatic test --port=8090
```

- For windows command prompt:<br/>
```shell
docker run --rm -v --network host "%cd%/specmatic.yaml:/usr/src/app/specmatic.yaml" -v "%cd%/build/reports/specmatic:/usr/src/app/build/reports/specmatic"  specmatic/specmatic test --port=8090
```
