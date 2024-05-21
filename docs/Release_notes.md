## JPO CV Manager Release Notes

## Version 1.3.0

### **Summary**

This release includes MongoDB support, integration with the Conflict Visualizer, WCAG web application accessibility support, and many more features. The CV Manager MongoDB support uses the existing MongoDB deployment originally created for the [Conflict Visualizer](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer). The CV Manager and its services use existing collections and even creates its own to display processed V2X information on the now WCAG compliant web application. This includes CV counts, V2X data visualization, and the fully integrated Conflict Visualizer. Other useful changes have been made to the Firmware Manager, CV Counter, and the new RSU Status Checker services. Read the enhancements list for more details.

Enhancements in this release:

- PR69: Keycloak token refresh timer increased to reduce the frequency of site refreshes.
- PR67: Daily aggregate CV counts to improve CV Manager count query performance in MongoDB.
- PR66: Email alerts on firmware manager fail cases.
- PR62: 'Contact Support' button now present on the 'Help' page.
- PR61: CV Manager SNMP configurations now pull from PostgreSQL instead of directly from RSUs for performance.
- PR60: PSM message visualization support and changing 'BSM Visualizer' to 'V2X Visualizer'.
- PR59: CV Manager support for TIM messages.
- PR57: Firmware Manager upgrade queue for handling excessive numbers of simultaneous upgrades.
- PR56: Firmware Manager post-upgrade bash script support.
- PR54: CV Manager full support of MongoDB instead of GCP BigQuery.
- PR52: Rework the existing counter to utilize MongoDB.
- PR51: RSU vendor filter added to the CV Manager web application.
- PR45-50: Updates to visual elements of the CV Manager web application to meet WCAG standard requirements for accessibility.
- PR44: Project updated to fully support Python 3.12.2.
- PR42: Adds support for a unique encryption SNMP password separate from the authentication password.
- PR38: RSU status check service added to perform regular, automated pings and SNMP message forwarding configuration checks on RSUs within PostgreSQL.
- PR37: URL page routing for the CV Manager web application.
- PR36: Keycloak realm updates to support the Conflict Visualizer realm within the same Keycloak deployment as the CV Manager.
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
