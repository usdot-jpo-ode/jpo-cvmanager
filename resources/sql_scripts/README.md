# PostgreSQL SQL Scripts

The CV Manager expects most of the data it utilizes to be stored in a PostgreSQL database. This PostgreSQL database can be hosted anywhere as long as proper networking rules have been configured. The tables of the database must be created using the provided SQL script to ensure the CV Manager will function properly.

## CVManager_CreateTables.sql

This is the SQL script to create all of the tables required by the CV Manager. This script is referenced in the [Postgres K8 Deployment](/resources/kubernetes/cv-manager-postgres.yaml) as a Config Map and must be updated if the CVManager_CreateTables.sql script is altered.

### manufacturers

Tracks different RSU and OBU manufacturers your deployment will need to support. Currently "Commsignia", "Kapsch" and "Yunex" are the only RSU manufacturers that have been tested with the CV Manager.

### rsu_models

Tracks different RSU models. Requires a foreign key to associate each model to a manufacturer. This is used for display purposes and to identify available firmware upgrades.

### firmware_images

Tracks known and available RSU firmware images. Requires a foreign key to associate each image to a RSU model. Includes other information necessary for the CV Manager API to properly retrieve the firmware image and install scripts.

### firmware_upgrade_rules

Contains a series of foreign keys specifying which previous firmware images are allowed to upgrade to a specified firmware image. This prevents the CV Manager from attempting to install a firmware image on a RSU that is multiple versions behind and might require an inbetween upgrade first.

### rsu_credentials

Tracks RSU credentials that are utilized by RSUs in the CV Manager's deployed environment. These are the same as SSH credentials. This allows for the CV Manager API to perform reboots and firmware upgrades on RSUs. A unique nickname is required to allow for the credentials to be referenced without sending confidential data over public network.

### snmp_credentials

Tracks RSU SNMP credentials that are utilized by RSUs in the CV Manager's deployed environment. This allows for the CV Manager API to perform SNMP configurations for message forwarding. A unique nickname is required to allow for the credentials to be referenced without sending confidential data over public network.

### snmp_protocols

Tracks RSU SNMP protocol version that is utilized by RSUs in the CV Manager's deployed environment. This allows for the CV Manager API to perform SNMP configurations for message forwarding. A unique nickname is required to allow for the credentials to be referenced without sending version data over public network.

### rsus

Tracks all RSUs in the CV Manager's environment. Each RSU in this table will be visually displayed on the CV Manager's map. Currently primary_route is not a table of its own so it is tracked here as well.

### ping

RSU ping data is tracked here. This ping data should only be the last 24 hours of ping data or the most recent record an RSU has been online. If this table is allowed to become too large, it can cause some slower load times for the CV Manager.

This table is populated with ping results that can be collected through a Zabbix server or an automated ping script.

### roles

Tracks the available user roles that users can be assigned within an organization.

Required roles the CV Manager expects: 'admin', 'operator' and 'user'.

### users

Tracks the specified users that are allowed to access the CV Manager. Currently requires a Google accounts. Users that are designated as a super user can access the admin page to the CV Manager.

### organizations

Tracks the organizations. Organizations are entities that users and RSUs can be added to. A user is only allowed to access and manipulate RSUs that are within their same organization.

### user_organization

The many-to-many relationship table to track user assignments to organizations. Users are allowed to be a part of multiple organizations.

### rsu_organization

The many-to-many relationship table to track RSU assignments to organizations. RSUs are allowed to be a part of multiple organizations.

### scms_health

Tracks the ISS SCMS health of each RSU. Similar to the ping table.

This table is populated by querying the ISS SCMS API endpoints every 6 hours. Requires an ISS SCMS API service agreement with ISS.
