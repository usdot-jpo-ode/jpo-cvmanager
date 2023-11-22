## JPO CV Manager Release Notes

## Version 1.1.0

### **Summary**

This release includes multiple new features as well as full Kubernetes support for the CV Manager. These features include a new contact support menu that can be used to request access to the CV Manager and the ability to track SNMP specification for each RSU in postgreSQL. Utilize the iss-health-checker addon to populate SCMS related status information to the CV Manager. Lastly, deploy the full CV Manager environment in a Kubernetes cluster with the new K8s YAML deployment files.

Enhancements in this release:

- PR18: Repository Python rework to consolidate Python code.
- PR17: Full Kubernetes support with YAML files.
- PR15: Resolve bug with SNMP indexing.
- PR13: WZDx popup size reduction.
- PR11: New addon application for ISS SCMS health checking.
- PR10: Ping purger added to rsu-ping-fetch addon to purge older ping data.
- PR9: SNMP version support. Tracks which SNMP specification each RSU uses.
- PR7: Contact support menu added to the CV Manager webapp.

## Version 1.0.0

### **Summary**

The first release for the jpo-cvmanager, version 1.0.0, includes an operational web application frontend with a backend API that supports Google OAuth authentication. Using the CV Manager, a user will be able to manage their deployed CV RSUs through an interactive, graphical user interface using Mapbox. This includes the main map menu (for viewing device statuses, configuring SNMP message forwarding, and visualizing BSMs and WZDx data), an administration menu (for adding, removing and modifying device information), and a help menu. Additional deployments are bundled with the CV Manager repository to get a user started with the PostgreSQL database and collecting RSU online status from a Zabbix server. Read more about the jpo-cvmanager in the [main README](../README.md).

Enhancements in this release:

- PR6: Add addon microservices that allow the CV Manager to display useful metrics for CV environments. (count_metric, rsu_ping_fetch and bsm_query)
- PR5: Introduce unit tests for the React web application.
- PR4: Integrate the RSU configuration menu into the main map menu using geo-fences for multi-RSU configurations.
- PR3: Create a local PostgreSQL solution to remove the dependency of GCP Cloud SQL from the API.
- PR2: Combines the map page, configuration page, heatmap, BSM visualization and WZDx page into a single page for streamlining the user experience.
- PR1: Initial CV Manager application and API developed by CDOT. Includes Mapbox mapping solution, configuration page, admin page, and Google OAuth2.0 support for user authentication.
