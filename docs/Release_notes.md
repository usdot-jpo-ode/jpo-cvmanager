## JPO CV Manager Release Notes

## Version 1.3.0

### **Summary**
This release includes enhanced MongoDB support, replacing GCP BigQuery in the CV Manager and integrating with the existing [Conflict Visualizer](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer) MongoDB deployment. The web application now meets WCAG accessibility standards, featuring improved V2X data visualization and CV counts. Key updates include a daily aggregate of CV counts for better MongoDB query performance, Keycloak token refresh optimization, SNMP configurations pulled from PostgreSQL, support for PSM and TIM messages and new services like the RSU Status Checker. Additional enhancements include email alerts for firmware manager failures, a 'Contact Support' button on the 'Help' page and a filter for RSU vendors. The project now fully supports Python 3.12.2, includes various bug fixes and introduces several performance improvements across different modules.

Enhancements in this release:

- CDOT PR 69: Keycloak token refresh timer increased to reduce the frequency of site refreshes.
- CDOT PR 67: Daily aggregate CV counts to improve CV Manager count query performance in MongoDB.
- CDOT PR 66: Email alerts on firmware manager fail cases.
- CDOT PR 62: 'Contact Support' button now present on the 'Help' page.
- CDOT PR 61: CV Manager SNMP configurations now pull from PostgreSQL instead of directly from RSUs for performance.
- CDOT PR 60: PSM message visualization support and changing 'BSM Visualizer' to 'V2X Visualizer'.
- CDOT PR 59: CV Manager support for TIM messages.
- CDOT PR 57: Firmware Manager upgrade queue for handling excessive numbers of simultaneous upgrades.
- CDOT PR 56: Firmware Manager post-upgrade bash script support.
- CDOT PR 54: CV Manager full support of MongoDB instead of GCP BigQuery.
- CDOT PR 52: Rework the existing counter to utilize MongoDB.
- CDOT PR 51: RSU vendor filter added to the CV Manager web application.
- CDOT PRs 45-50: Updates to visual elements of the CV Manager web application to meet WCAG standard requirements for accessibility.
- CDOT PR 44: Project updated to fully support Python 3.12.2.
- CDOT PR 42: Adds support for a unique encryption SNMP password separate from the authentication password.
- CDOT PR 38: RSU status check service added to perform regular, automated pings and SNMP message forwarding configuration checks on RSUs within PostgreSQL.
- CDOT PR 37: URL page routing for the CV Manager web application.
- CDOT PR 36: Keycloak realm updates to support the Conflict Visualizer realm within the same Keycloak deployment as the CV Manager.
- Additional bug fixes

## Version 1.2.0

### **Summary**

This release includes Keycloak authentication support, a reworked automated firmware upgrade service, and many more minor security and bug fixes. The Keycloak authentication allows users who do not want to use Google as an authentication provider to still use the CV Manager and offers native login refresh token support. The Firmware Manager can be deployed alongside the CV Manager to handle requested firmware upgrades - the PostgreSQL table is used to define the RSUs to monitor and what upgrade rules exist. The web application project has also undergone a refactor to Typescript to make way for a future merging of the jpo-conflictvisualizer with the jpo-cvmanager.

Enhancements in this release:

- PR31: Security added to web application for securing the state.
- PR28: Timezone dependency removed for unit tests.
- PR26: Kubernetes ConfigMap added for the PostgreSQL deployment to inject table creation script on initial runtime.
- PR25: Added a Python Black formatter to the project.
- PR24: SNMP version is now used for generating SNMP commands instead of RSU vendor name.
- PR23: A new service has been added, the Firmware Manager. Used for maintaining and performing firmware upgrades for RSUs in the CV Manager. Commsignia and Yunex support.
- PR22: Refactored React.js web application to be in Typescript instead of Javascript.
- PR21: Add SSH and RSU reboot functionality to the jpo-cvmanager API.
- PR20: Changed Python Kafka library across the project to Confluent's for better support and library maintenance.
- PR19: Allow rsu-ping-fetch to be built as rsu-pinger which will ping RSUs on its own and collect online statuses without the use of Zabbix.
- PR12: Keycloak authentication integrated. Google authentication still supported through Keycloak but removed as the primary authenticator.

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
