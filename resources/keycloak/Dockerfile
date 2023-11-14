FROM registry.access.redhat.com/ubi9:9.2 AS ubi-micro-build
RUN mkdir -p /mnt/rootfs
RUN dnf install --installroot /mnt/rootfs curl jq \
    --releasever 9 --setopt install_weak_deps=false --nodocs -y; \
    dnf --installroot /mnt/rootfs clean all

FROM quay.io/keycloak/keycloak:21.1
ARG KEYCLOAK_LOGIN_THEME_NAME

COPY --from=ubi-micro-build /mnt/rootfs /
COPY realm.json /opt/keycloak/data/import/realm.json
COPY ${KEYCLOAK_LOGIN_THEME_NAME} /opt/keycloak/providers/theme.jar
HEALTHCHECK --interval=5s --timeout=10s --retries=20 \
CMD curl --fail http://localhost:8080/health || exit 1