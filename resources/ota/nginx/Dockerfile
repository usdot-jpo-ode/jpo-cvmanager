FROM nginx:1.25.4-alpine

RUN apk update && apk add \
    apache2-utils \
    jq \
    openssl \
    bash

CMD ["nginx", "-g", "daemon off;"]