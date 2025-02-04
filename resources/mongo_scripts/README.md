# Mongo Scripts

This directory contains MongoDB configuration scripts, used in the docker-compose-intersection.yml mongodb_container service, as well as the docker-compose-mongo.yml cvmanager_mongo_setup service. These scripts set up indexes (normal and TTL), volume sizes, and initial configuration of the database.

## jpo-utils

These are expected to be phased out and replaced by the [jpo-utils](https://github.com/usdot-jpo-ode/jpo-utils) repository, which contains more generic and configurable scripts for setting up and configuration a mongodb instance.
