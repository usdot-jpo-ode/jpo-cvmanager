FROM mongo:6

COPY a_init_replicas.js /docker-entrypoint-initdb.d/a_init_replicas.js
COPY b_create_indexes.js /docker-entrypoint-initdb.d/b_create_indexes.js

