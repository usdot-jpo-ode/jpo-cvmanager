# Build React App With Node 
FROM node:18-alpine as builder

WORKDIR /app
COPY . .

ENV BUILD_PATH "./build"

ARG API_URI
ENV REACT_APP_GATEWAY_BASE_URL $API_URI

ARG MAPBOX_TOKEN
ENV REACT_APP_MAPBOX_TOKEN $MAPBOX_TOKEN

ARG KEYCLOAK_HOST_URL
ENV REACT_APP_KEYCLOAK_URL $KEYCLOAK_HOST_URL

RUN npm ci
RUN npm run build

# Serve On NGINX
FROM nginx:1.19.0
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=builder /app/build .
ENTRYPOINT ["nginx", "-g", "daemon off;"]