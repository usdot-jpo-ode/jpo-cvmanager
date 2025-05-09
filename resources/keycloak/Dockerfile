################## Install curl to enable health checks ##################
FROM registry.access.redhat.com/ubi9:9.2 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs curl jq \
    --releasever 9 --setopt install_weak_deps=false --nodocs -y; \
    dnf --installroot /mnt/rootfs clean all

################## Build custom user provider to .jar ##################
FROM maven:3.8-eclipse-temurin-21-alpine as jarbuilder

WORKDIR /home

COPY ./custom-user-provider/pom.xml ./custom-user-provider/
COPY ./custom-user-provider/src ./custom-user-provider/src

WORKDIR /home/custom-user-provider

RUN mvn clean package -DskipTests

################## Build custom user provider into keycloak ##################
FROM quay.io/keycloak/keycloak:26.0.5 as builder

ENV KC_DB=postgres

WORKDIR /opt/keycloak

COPY --from=jarbuilder /home/custom-user-provider/target/keycloak-custom-providers.jar /opt/keycloak/providers/

RUN /opt/keycloak/bin/kc.sh build

################## Integrate realm and theme ##################
FROM quay.io/keycloak/keycloak:26.0.5
ARG KEYCLOAK_LOGIN_THEME_NAME
COPY --from=ubi-micro-build /mnt/rootfs /
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY custom-welcome /opt/keycloak/themes/custom-welcome
COPY realm.json /opt/keycloak/data/import/realm.json
COPY ${KEYCLOAK_LOGIN_THEME_NAME} /opt/keycloak/providers/theme.jar

HEALTHCHECK --interval=5s --timeout=10s --start-period=30s --retries=30 \
CMD curl --fail http://localhost:8080/health || exit 1
