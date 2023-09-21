FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs curl jq \
    --releasever 9 --setopt install_weak_deps=false --nodocs -y; \
    dnf --installroot /mnt/rootfs clean all

FROM node:16.14.2-alpine as keycloakify_builder
RUN yarn install 
RUN mkdir -p /home/keycloakify
COPY keycloakify/ /home/keycloakify
RUN yarn --cwd /home/keycloakify
RUN yarn --cwd /home/keycloakify build-keycloak-theme

FROM quay.io/keycloak/keycloak:21.1
COPY --from=ubi-micro-build /mnt/rootfs /
COPY --from=keycloakify_builder /home/keycloakify/build_keycloak/target/cv-manager-keycloak-theme-4.4.14.jar /opt/keycloak/providers/theme.jar
HEALTHCHECK --interval=5s --timeout=10s --retries=20 \
CMD curl --fail http://localhost:8080/health || exit 1