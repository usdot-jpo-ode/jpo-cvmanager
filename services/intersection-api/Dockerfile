# === BUILDER IMAGE for ACM ===
FROM alpine:3.18 as builder
USER root
WORKDIR /asn1_codec

# Install build dependencies
RUN apk update && apk add --no-cache \
    bash \
    cmake \
    make \
    g++ \
    autoconf \
    automake \
    libtool \
    flex \
    bison \
    git \
    curl \
    openssl-dev \
    pkgconfig \
    zlib-dev \
    linux-headers \
    build-base \
    librdkafka \
    librdkafka-dev \
    asio-dev

# Install pugixml
COPY ./asn1_codec/pugixml /asn1_codec/pugixml
RUN cd /asn1_codec/pugixml && mkdir -p build && cd build && cmake .. && make && make install

# Build and install asn1c submodule. Ensure all scripts have the correct permissions - required for azure release pipeline
COPY ./asn1_codec/usdot-asn1c /asn1_codec/asn1c
RUN find /asn1_codec/asn1c -type f -name '*.pl' -exec chmod +x {} \; && \
    cd /asn1_codec/asn1c && \
    test -f configure || autoreconf -iv && ./configure && make && make install

# Make generated files available to the build & compile example
COPY ./asn1_codec/asn1c_combined /asn1_codec/asn1c_combined
RUN export LD_LIBRARY_PATH=/usr/local/lib && cd /asn1_codec/asn1c_combined && bash doIt.sh

# Clean up unused asn directories
RUN rm -rf /asn1_codec/asn1c_combined/j2735-asn-files \
           /asn1_codec/asn1c_combined/semi-asn-files \
           /asn1_codec/asn1c_combined/generated-files

# Copy source and config files
COPY ./asn1_codec/CMakeLists.txt /asn1_codec
COPY ./asn1_codec/config /asn1_codec/config
COPY ./asn1_codec/include /asn1_codec/include
COPY ./asn1_codec/src /asn1_codec/src
COPY ./asn1_codec/kafka-test /asn1_codec/kafka-test
COPY ./asn1_codec/unit-test-data /asn1_codec/unit-test-data
COPY ./asn1_codec/data /asn1_codec/data
COPY ./asn1_codec/run_acm.sh /asn1_codec

# Build ACM
RUN mkdir -p /build && cd /build && cmake /asn1_codec && make

# === BUILD IMAGE for Java ===
FROM eclipse-temurin:21-alpine as jbuilder

WORKDIR /home

ARG MAVEN_GITHUB_TOKEN
ARG MAVEN_GITHUB_ORG

ENV MAVEN_GITHUB_TOKEN=$MAVEN_GITHUB_TOKEN
ENV MAVEN_GITHUB_ORG=$MAVEN_GITHUB_ORG

RUN apk add --no-cache maven

COPY ./api/pom.xml ./intersection-api/
COPY ./api/settings.xml ./intersection-api/

WORKDIR /home/intersection-api
RUN mvn -s settings.xml dependency:resolve

COPY ./api/src ./src
RUN mvn -s settings.xml install -DskipTests

# === RUNTIME IMAGE for Java and ACM ===
# NOTE: This is alpine based to match the ACM image and allow for ARM64 support.
FROM eclipse-temurin:21-alpine

WORKDIR /home
USER root

# Install runtime dependencies
RUN apk update && apk add --no-cache \
    bash \
    librdkafka \
    librdkafka-dev \
    jemalloc

# Set jemalloc as the allocator
ENV LD_PRELOAD=/usr/lib/libjemalloc.so.2

# Copy the built Java artifacts
COPY --from=jbuilder /home/intersection-api/src/main/resources/application.yaml /home
COPY --from=jbuilder /home/intersection-api/src/main/resources/logback.xml /home
COPY --from=jbuilder /home/intersection-api/target/intersection-api.jar /home

# Copy ACM binary and required files
WORKDIR /asn1_codec
COPY --from=builder /asn1_codec /asn1_codec
COPY --from=builder /build /build

# copy test data. This changes frequently so keep it low in the file.
COPY ./asn1_codec/docker-test /asn1_codec/docker-test

# Back to home for entrypoint
WORKDIR /home

ENTRYPOINT ["java", \
    "-Djava.rmi.server.hostname=$DOCKER_HOST_IP", \
    "-Dcom.sun.management.jmxremote.port=9090", \
    "-Dcom.sun.management.jmxremote.rmi.port=9090", \
    "-Dcom.sun.management.jmxremote", \
    "-Dcom.sun.management.jmxremote.local.only=true", \
    "-Dcom.sun.management.jmxremote.authenticate=false", \
    "-Dcom.sun.management.jmxremote.ssl=false", \
    "-Dlogback.configurationFile=/home/logback.xml", \
    "-jar", \
    "/home/intersection-api.jar"]
