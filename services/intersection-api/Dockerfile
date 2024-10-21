# === BUILDER IMAGE for ACM ===
# FROM alpine:3.12 as builder
FROM amazonlinux:2023 as builder
USER root
WORKDIR /asn1_codec
## add build dependencies
RUN yum install -y cmake g++ make bash automake libtool autoconf flex bison

# Install librdkafka from Confluent repo
RUN rpm --import https://packages.confluent.io/rpm/7.6/archive.key
COPY ./confluent.repo /etc/yum.repos.d
RUN yum clean all
RUN yum install -y librdkafka-devel

# Install pugixml
ADD ./asn1_codec/pugixml /asn1_codec/pugixml
RUN cd /asn1_codec/pugixml && mkdir -p build && cd build && cmake .. && make && make install

# Build and install asn1c submodule
ADD ./asn1_codec/usdot-asn1c /asn1_codec/asn1c
RUN cd asn1c && test -f configure || autoreconf -iv && ./configure && make && make install

# Make generated files available to the build & compile example
RUN export LD_LIBRARY_PATH=/usr/local/lib
ADD ./asn1_codec/asn1c_combined /asn1_codec/asn1c_combined
RUN cd /asn1_codec/asn1c_combined && bash doIt.sh

# Remove any lingering .asn files
RUN rm -rf /asn1c_codec/asn1c_combined/j2735-asn-files
RUN rm -rf /asn1c_codec/asn1c_combined/semi-asn-files

# Remove duplicate files
RUN rm -rf /asn1c_codec/asn1c_combined/generated-files

# add the source and build files
ADD ./asn1_codec/CMakeLists.txt /asn1_codec
ADD ./asn1_codec/config /asn1_codec/config
ADD ./asn1_codec/include /asn1_codec/include
ADD ./asn1_codec/src /asn1_codec/src
ADD ./asn1_codec/kafka-test /asn1_codec/kafka-test
ADD ./asn1_codec/unit-test-data /asn1_codec/unit-test-data
ADD ./asn1_codec/data /asn1_codec/data
ADD ./asn1_codec/run_acm.sh /asn1_codec
ADD ./asn1_codec/data /asn1_codec/data

# Build acm.
RUN mkdir -p /build && cd /build && cmake /asn1_codec && make


# # === Build image for Java ===
FROM maven:3.8-eclipse-temurin-21-alpine as jbuilder

WORKDIR /home

# Copy only the files needed to avoid putting all sorts of junk from your local env on to the image
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/pom.xml ./jpo-ode/
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-common/pom.xml ./jpo-ode/jpo-ode-common/
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-common/src ./jpo-ode/jpo-ode-common/src
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-plugins/pom.xml ./jpo-ode/jpo-ode-plugins/
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-plugins/src ./jpo-ode/jpo-ode-plugins/src
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-core/pom.xml ./jpo-ode/jpo-ode-core/
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-core/src ./jpo-ode/jpo-ode-core/src/
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-svcs/pom.xml ./jpo-ode/jpo-ode-svcs/
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-ode/jpo-ode-svcs/src ./jpo-ode/jpo-ode-svcs/src

COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-geojsonconverter/pom.xml ./jpo-geojsonconverter/
COPY ./jpo-conflictmonitor/jpo-geojsonconverter/jpo-geojsonconverter/src ./jpo-geojsonconverter/src

COPY ./jpo-conflictmonitor/jpo-conflictmonitor/pom.xml ./jpo-conflictmonitor/
COPY ./jpo-conflictmonitor/jpo-conflictmonitor/src ./jpo-conflictmonitor/src

COPY ./jpo-conflictvisualizer-api/pom.xml ./jpo-conflictvisualizer-api/
COPY ./jpo-conflictvisualizer-api/src ./jpo-conflictvisualizer-api/src

WORKDIR /home/jpo-ode

RUN mvn install -DskipTests

WORKDIR /home/jpo-geojsonconverter

RUN mvn clean install -DskipTests

WORKDIR /home/jpo-conflictmonitor

RUN mvn clean install -DskipTests

WORKDIR /home/jpo-conflictvisualizer-api

RUN mvn clean package -DskipTests
ENTRYPOINT ["tail", "-f", "/dev/null"]


# # === RUNTIME IMAGE for Java and ACM ===
# # Use Amazon Corretto Java on Amazon Linum 2023 to match the codec build env
# # FROM alpine:3.12
FROM amazoncorretto:21-al2023

WORKDIR /home

COPY --from=jbuilder /home/jpo-conflictvisualizer-api/src/main/resources/application.yaml /home
COPY --from=jbuilder /home/jpo-conflictvisualizer-api/src/main/resources/logback.xml /home
COPY --from=jbuilder /home/jpo-conflictvisualizer-api/target/jpo-conflictvisualizer-api.jar /home
# # Copy asn1_codec executable and test files
USER root
WORKDIR /asn1_codec



# # add runtime dependencies
RUN yum install -y bash


# Install librdkafka from Confluent repo
RUN rpm --import https://packages.confluent.io/rpm/7.6/archive.key
COPY ./confluent.repo /etc/yum.repos.d
RUN yum clean all
RUN yum install -y librdkafka-devel

# # copy the built files from the builder
COPY --from=builder /asn1_codec /asn1_codec
COPY --from=builder /build /build

# # Add test data. This changes frequently so keep it low in the file.
ADD ./asn1_codec/docker-test /asn1_codec/docker-test

# # Put workdir back to Java home
WORKDIR /home

# # ENTRYPOINT ["java", "-jar", "/home/asn1-codec-java.jar"]
# # #COPY cert.crt /home/cert.crt
# # #RUN keytool -import -trustcacerts -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit -noprompt -alias mycert -file cert.crt

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
    "/home/jpo-conflictvisualizer-api.jar"]