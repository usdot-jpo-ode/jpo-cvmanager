# === BUILDER IMAGE for ACM ===
FROM amazonlinux:2023 as builder
USER root
WORKDIR /asn1_codec
## install build dependencies
RUN yum install -y cmake g++ make bash automake libtool autoconf flex bison

# Install librdkafka from Confluent repo
COPY ./confluent.repo /etc/yum.repos.d
RUN rpm --import https://packages.confluent.io/rpm/7.6/archive.key && yum clean all && yum install -y librdkafka-devel

# Install pugixml
COPY ./asn1_codec/pugixml /asn1_codec/pugixml
RUN cd /asn1_codec/pugixml && mkdir -p build && cd build && cmake .. && make && make install

# Build and install asn1c submodule. Ensure all scripts have the correct permissions - required for azure release pipeline
COPY ./asn1_codec/usdot-asn1c /asn1_codec/asn1c
RUN find /asn1_codec/asn1c -type f -name '*.pl' -exec chmod +x {} \; && cd asn1c && test -f configure || autoreconf -iv && ./configure && make && make install

# Make generated files available to the build & compile example
COPY ./asn1_codec/asn1c_combined /asn1_codec/asn1c_combined
RUN export LD_LIBRARY_PATH=/usr/local/lib && cd /asn1_codec/asn1c_combined && bash doIt.sh

# Remove any lingering .asn and duplicate files
RUN rm -rf /asn1c_codec/asn1c_combined/j2735-asn-files && rm -rf /asn1c_codec/asn1c_combined/semi-asn-files && rm -rf /asn1c_codec/asn1c_combined/generated-files

# copy the source and build files
COPY ./asn1_codec/CMakeLists.txt /asn1_codec
COPY ./asn1_codec/config /asn1_codec/config
COPY ./asn1_codec/include /asn1_codec/include
COPY ./asn1_codec/src /asn1_codec/src
COPY ./asn1_codec/kafka-test /asn1_codec/kafka-test
COPY ./asn1_codec/unit-test-data /asn1_codec/unit-test-data
COPY ./asn1_codec/data /asn1_codec/data
COPY ./asn1_codec/run_acm.sh /asn1_codec
COPY ./asn1_codec/data /asn1_codec/data

# Build acm.
RUN mkdir -p /build && cd /build && cmake /asn1_codec && make


# # === Build image for Java ===
FROM maven:3.8-eclipse-temurin-21-alpine as jbuilder

WORKDIR /home

ARG MAVEN_GITHUB_TOKEN
ARG MAVEN_GITHUB_ORG

ENV MAVEN_GITHUB_TOKEN=$MAVEN_GITHUB_TOKEN
ENV MAVEN_GITHUB_ORG=$MAVEN_GITHUB_ORG

COPY ./api/pom.xml ./intersection-api/
COPY ./api/settings.xml ./intersection-api/

WORKDIR /home/intersection-api
RUN mvn -s settings.xml dependency:resolve

COPY ./api/src ./src
RUN mvn -s settings.xml install -DskipTests

# # === RUNTIME IMAGE for Java and ACM ===
# # Use Amazon Corretto Java on Amazon Linum 2023 to match the codec build env
# # FROM alpine:3.12
FROM amazoncorretto:21-al2023

WORKDIR /home

COPY --from=jbuilder /home/intersection-api/src/main/resources/application.yaml /home
COPY --from=jbuilder /home/intersection-api/src/main/resources/logback.xml /home
COPY --from=jbuilder /home/intersection-api/target/intersection-api.jar /home
# # Copy asn1_codec executable and test files
USER root
WORKDIR /asn1_codec


# # install runtime dependencies
RUN yum install -y bash


# Install librdkafka from Confluent repo
COPY ./confluent.repo /etc/yum.repos.d
RUN rpm --import https://packages.confluent.io/rpm/7.6/archive.key && yum clean all && yum install -y librdkafka-devel

# # copy the built files from the builder
COPY --from=builder /asn1_codec /asn1_codec
COPY --from=builder /build /build

# # copy test data. This changes frequently so keep it low in the file.
COPY ./asn1_codec/docker-test /asn1_codec/docker-test

# # Put workdir back to Java home
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