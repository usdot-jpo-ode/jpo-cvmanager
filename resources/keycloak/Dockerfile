@ -0,0 +1,36 @@
FROM quay.io/keycloak/keycloak:21.1 as builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

WORKDIR /opt/keycloak

COPY ./custom-user-provider/target/keycloak-custom-providers-0.1.0-SNAPSHOT.jar /opt/keycloak/providers/

# for demonstration purposes only, please make sure to use proper certificates in production instead
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:21.1
ARG KEYCLOAK_LOGIN_THEME_NAME
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY custom-welcome /opt/keycloak/themes/custom-welcome
COPY ${KEYCLOAK_LOGIN_THEME_NAME} /opt/keycloak/providers/theme.jar
COPY realm.json /opt/keycloak/data/import/realm.json

# change these values to point to a running postgres instance
ENV KC_DB=postgres
ENV KC_DB_URL=jdbc:postgresql://192.168.110.147/postgres?currentSchema=keycloak
ENV KC_DB_USERNAME=postgres
ENV KC_DB_PASSWORD=postgres
ENV KC_HOSTNAME=localhost
ENV KC_HOSTNAME=localhost
ENV KC_LOG_LEVEL=DEBUG

HEALTHCHECK --interval=5s --timeout=10s --retries=20 \
CMD curl --fail http://localhost:8080/health || exit 1
# ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]