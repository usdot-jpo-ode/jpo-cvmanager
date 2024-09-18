FROM maven:3.8-eclipse-temurin-21-alpine as jarbuilder

WORKDIR /home

COPY ./custom-user-provider/pom.xml ./custom-user-provider/
COPY ./custom-user-provider/src ./custom-user-provider/src

WORKDIR /home/custom-user-provider

RUN mvn clean package -DskipTests

FROM quay.io/keycloak/keycloak:21.1 as builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

WORKDIR /opt/keycloak

COPY --from=jarbuilder /home/custom-user-provider/target/keycloak-custom-providers.jar /opt/keycloak/providers/

# for demonstration purposes only, please make sure to use proper certificates in production instead
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:21.1
ARG KEYCLOAK_LOGIN_THEME_NAME
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY custom-welcome /opt/keycloak/themes/custom-welcome
COPY realm.json /opt/keycloak/data/import/realm.json
COPY ${KEYCLOAK_LOGIN_THEME_NAME} /opt/keycloak/providers/theme.jar

HEALTHCHECK --interval=5s --timeout=10s --retries=30 \
CMD curl --fail http://localhost:8080/health || exit 1
