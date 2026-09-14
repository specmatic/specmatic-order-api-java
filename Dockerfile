FROM amazoncorretto:25.0.4-alpine

RUN apk add --no-cache git curl bash jq && \
    rm -rf /var/cache/apk/*

SHELL ["/bin/bash", "-c"]

WORKDIR /app

EXPOSE 8090

COPY target/specmatic-order-api-1.0-SNAPSHOT.jar /app/order-api.jar

CMD java -jar order-api.jar
