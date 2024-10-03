# JPO-Conflictmonitor Components

This directory contains configuration files for the jpo-conflictmonitor, referenced in the docker-compose.yml. These files configure the kafka init container as well as the mongodb container.

## kafka

This directory contains kafka scripts, used for initializing the relevant kafka topics required for ConflictMonitor components. This script came from the JPO-ConflictMonitor repository, and has been modified to create additional topics required by the conflictvisualizer api.

## mongo

This directory contains scripts from the ConflictMonitor for initializing collections and indexes, as well as a mongodump dataset used for the visualizer. These files are referenced in the docker-compose.yml, and should not need to be modified for local development.
