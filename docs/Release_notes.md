## JPO CV Manager Release Notes

## Version 2.0.0

This version introduces updates across both backend and frontend components, enhancing system performance, reliability, and user experience. This release includes a complete rework of SNMP message forwarding, upgraded authentication with Keycloak 26, and refactored Intersection API endpoints with the new reworked jpo-geojsonconverter's (3.1.0) processed message output. The update also streamlines standalone deployments and adds new interface improvements such as toast notifications and updated visualization tools. Numerous bug fixes, dependency cleanups, and UI refinements have also been made.

Enhancements in this release:

- [CDOT PR 127](https://github.com/CDOT-CV/jpo-cvmanager/pull/127): Enforcing Organization and Role API Permissions
- [CDOT PR 182](https://github.com/CDOT-CV/jpo-cvmanager/pull/182): Integrate jpo-utils Mongo Setup into CV Manager via Submodule
- [CDOT PR 183](https://github.com/CDOT-CV/jpo-cvmanager/pull/183): Standalone deployment updates
- [CDOT PR 201](https://github.com/CDOT-CV/jpo-cvmanager/pull/201): SNMP Message Forwarding Rework
- [CDOT PR 202](https://github.com/CDOT-CV/jpo-cvmanager/pull/202): Upgrading keycloak to version 26
- [CDOT PR 207](https://github.com/CDOT-CV/jpo-cvmanager/pull/207): Adding Intersection API global path prefix
- [CDOT PR 210](https://github.com/CDOT-CV/jpo-cvmanager/pull/210): Intersection API Endpoint Refactoring for Combination
- [CDOT PR 211](https://github.com/CDOT-CV/jpo-cvmanager/pull/211): Live Intersection Map Timing Rework
- [CDOT PR 212](https://github.com/CDOT-CV/jpo-cvmanager/pull/212): IAPI package renaming v2
- [CDOT PR 214](https://github.com/CDOT-CV/jpo-cvmanager/pull/214): SNMP-suggestions
- [CDOT PR 216](https://github.com/CDOT-CV/jpo-cvmanager/pull/216): Fix temporary 'object not found' issue on admin pages
- [CDOT PR 217](https://github.com/CDOT-CV/jpo-cvmanager/pull/217): Fix DisplayRsuErrors formatting for single RSU
- [CDOT PR 218](https://github.com/CDOT-CV/jpo-cvmanager/pull/218): Add Toast Notifications to User Settings Menu
- [CDOT PR 219](https://github.com/CDOT-CV/jpo-cvmanager/pull/219): Cleanup Webapp
- [CDOT PR 220](https://github.com/CDOT-CV/jpo-cvmanager/pull/220): Restricting CI sonar pipeline to only run on USDOT-JPO-ODE repos
- [CDOT PR 221](https://github.com/CDOT-CV/jpo-cvmanager/pull/221): Converting v2x Message Viewer Slider to MUI
- [CDOT PR 225](https://github.com/CDOT-CV/jpo-cvmanager/pull/225): Fixing Bugs 2025/07/03
- [CDOT PR 226](https://github.com/CDOT-CV/jpo-cvmanager/pull/226): Intersection API Unit Tests - Services
- [CDOT PR 227](https://github.com/CDOT-CV/jpo-cvmanager/pull/227): Intersection API Unit Tests - Controllers
- [CDOT PR 228](https://github.com/CDOT-CV/jpo-cvmanager/pull/228): Admin page object not found - using loading instead of unknownUser
- [CDOT PR 229](https://github.com/CDOT-CV/jpo-cvmanager/pull/229): Fixing Minor Intersection Viewer Bugs
- [CDOT PR 230](https://github.com/CDOT-CV/jpo-cvmanager/pull/230): Intersection API HAAS alert
- [CDOT PR 231](https://github.com/CDOT-CV/jpo-cvmanager/pull/231): Re-generating Sample Intersection Data
- [CDOT PR 232](https://github.com/CDOT-CV/jpo-cvmanager/pull/232): Removing Unused Webapp Dependencies
- [CDOT PR 235](https://github.com/CDOT-CV/jpo-cvmanager/pull/235): Bug fixes and improvements to ASN1 decoder page
- [CDOT PR 237](https://github.com/CDOT-CV/jpo-cvmanager/pull/237): Intersection API Unit Testing #3
- [CDOT PR 238](https://github.com/CDOT-CV/jpo-cvmanager/pull/238): Processed msg updates
- [CDOT PR 239](https://github.com/CDOT-CV/jpo-cvmanager/pull/239): Processed msg visualization updates
- [CDOT PR 241](https://github.com/CDOT-CV/jpo-cvmanager/pull/241): Max Files Change Limit
- [CDOT PR 242](https://github.com/CDOT-CV/jpo-cvmanager/pull/242): 2025/Q3 Bug Fixes

## Version 1.6.1

### **Summary**

In this hotfix, UX bugs have been resolved to ensure full feature functionality for the user.

Enhancements in this release:

- [CDOT PR 209](https://github.com/CDOT-CV/jpo-cvmanager/pull/209): Hotfix/1.6.1: Fix/map UI bugs

## Version 1.6.0

### **Summary**

This release delivers a set of targeted enhancements and stability improvements across multiple components. Notable changes include the extension of the CV Manager webapp capabilities with the intersection API configuration page, the integration of the Moove.AI dataset as a visualizable map layer, and rework to the CV Manager webapp UX. The CV Manager webapp has undergone a UX refactor for improved usability and consistency. Performance optimizations and bug fixes have been implemented across the CV Manager API, Intersection API, and addons, including implementing paging in the Intersection API. The CV Manager now supports configuring RSU message forwarding with security headers included through SNMP using NTCIP-1218.

Enhancements in this release:

- [CDOT PR 162](https://github.com/CDOT-CV/jpo-cvmanager/pull/162): Intersection API constructor injection
- [CDOT PR 163](https://github.com/CDOT-CV/jpo-cvmanager/pull/163): Adding Keycloak webapp environment variables
- [CDOT PR 167](https://github.com/CDOT-CV/jpo-cvmanager/pull/167): Fixing Intersection API auth errors
- [CDOT PR 171](https://github.com/CDOT-CV/jpo-cvmanager/pull/171): Individual profiles
- [CDOT PR 156](https://github.com/CDOT-CV/jpo-cvmanager/pull/156): Moove.AI dataset layer hard braking feature
- [CDOT PR 169](https://github.com/CDOT-CV/jpo-cvmanager/pull/169): Removing duplicate webapp Keycloak auth provider
- [CDOT PR 168](https://github.com/CDOT-CV/jpo-cvmanager/pull/168): Webapp logging reduction and Intersection API auth fix
- [CDOT PR 165](https://github.com/CDOT-CV/jpo-cvmanager/pull/165): Adding Intersection API pagination
- [CDOT PR 176](https://github.com/CDOT-CV/jpo-cvmanager/pull/176): Bug fixes for SNMP related features
- [CDOT PR 177](https://github.com/CDOT-CV/jpo-cvmanager/pull/177): Bug fix for user creation through the CV Manager
- [CDOT PR 184](https://github.com/CDOT-CV/jpo-cvmanager/pull/184): Modifying standalone deployment - adding endpoint/Keycloak/IP distinctions
- [CDOT PR 115](https://github.com/CDOT-CV/jpo-cvmanager/pull/115): Adds CIMMS configuration page to the CV Manager webapp
- [CDOT PR 186](https://github.com/CDOT-CV/jpo-cvmanager/pull/186): Improve Dockerfiles using best practices
- [CDOT PR 159](https://github.com/CDOT-CV/jpo-cvmanager/pull/159): Webapp header UI updates
- [CDOT PR 188](https://github.com/CDOT-CV/jpo-cvmanager/pull/188): Swapping to profiles for run-intersection option
- [CDOT PR 187](https://github.com/CDOT-CV/jpo-cvmanager/pull/187): Bug fix for Firmware Manager Upgrade Runner callback endpoint
- [CDOT PR 160](https://github.com/CDOT-CV/jpo-cvmanager/pull/160): Help / Contact Support UI updates
- [CDOT PR 185](https://github.com/CDOT-CV/jpo-cvmanager/pull/185): Intersection API PostgreSQL optimizations
- [CDOT PR 178](https://github.com/CDOT-CV/jpo-cvmanager/pull/178): Optimizing SPaT and MAP Intersection API queries
- [CDOT PR 193](https://github.com/CDOT-CV/jpo-cvmanager/pull/193): Intersection API Dockerfile updates to support ARM64 CPUs
- [CDOT PR 146](https://github.com/CDOT-CV/jpo-cvmanager/pull/146): Adding client side report generation
- [CDOT PR 195](https://github.com/CDOT-CV/jpo-cvmanager/pull/195): Make aggregation query implementations typesafe
- [CDOT PR 194](https://github.com/CDOT-CV/jpo-cvmanager/pull/194): Update jpo-utils submodule to use CDOT fork
- [CDOT PR 175](https://github.com/CDOT-CV/jpo-cvmanager/pull/175): Map page UX rework
- [CDOT PR 190](https://github.com/CDOT-CV/jpo-cvmanager/pull/190): Update V2X Message Viewer geometry generation from useEffect to useMemo
- [CDOT PR 164](https://github.com/CDOT-CV/jpo-cvmanager/pull/164): Intersection API MongoDB query limits
- [CDOT PR 180](https://github.com/CDOT-CV/jpo-cvmanager/pull/180): SNMP message forwarding with IEEE 1609.2 security headers
- [CDOT PR 192](https://github.com/CDOT-CV/jpo-cvmanager/pull/192): Removing road regulator ID from Intersection API and webapp
- [CDOT PR 191](https://github.com/CDOT-CV/jpo-cvmanager/pull/191): RSU Config Polygon Rendering Update
- [CDOT PR 158](https://github.com/CDOT-CV/jpo-cvmanager/pull/158): Admin & User settings UI rework
- [CDOT PR 174](https://github.com/CDOT-CV/jpo-cvmanager/pull/174): Intersection Map UI rework
- [CDOT PR 181](https://github.com/CDOT-CV/jpo-cvmanager/pull/181): Integrate jpo-utils Kafka setup into CV Manager via submodule
- [CDOT PR 196](https://github.com/CDOT-CV/jpo-cvmanager/pull/196): Fixing Q2 Testing Bugs
- [CDOT PR 198](https://github.com/CDOT-CV/jpo-cvmanager/pull/198): RSU popup display fixes
- [CDOT PR 199](https://github.com/CDOT-CV/jpo-cvmanager/pull/199): Improving Intersection Dashboard Assessment Plot Formatting
- [CDOT PR 206](https://github.com/CDOT-CV/jpo-cvmanager/pull/206): Adding additional required package to dockerfile
- [CDOT PR 204](https://github.com/CDOT-CV/jpo-cvmanager/pull/204): Prevent RSU selection during point selection
- [CDOT PR 189](https://github.com/CDOT-CV/jpo-cvmanager/pull/189): Update the collection and topic names based on the new event names
- [CDOT PR 205](https://github.com/CDOT-CV/jpo-cvmanager/pull/205): Upgrading Intersection API ODE Dependencies
- [CDOT PR 208](https://github.com/CDOT-CV/jpo-cvmanager/pull/208): Fixing Notification Dismissal Response

## Version 1.5.0

### **Summary**

This release introduces several enhancements and fixes aimed at improving functionality, reliability, and user experience. Key updates include the implementation of a custom Keycloak user provider for enhanced user management, improvements to firmware upgrade processes with refined tracking and scheduling, and the addition of an RSU errors page to support more efficient issue resolution. The intersection dashboard and related features have been optimized for better performance, and new tools, such as collapsible map menus and enhanced visualization capabilities, provide a more streamlined user experience. The Intersection API, which has been forked from the ConflictVisualizer API, is now integrated into the CV Manager codebase to facilitate future development. Additional updates include theme customization with MUI, resolution of DateTimePicker errors, improved email validation and security settings, updated architecture documentation, and the introduction of feature flags to increase configurability. System stability and compatibility have also been enhanced through various fixes, including updates to GitHub Actions.

Enhancements in this release:

- [CDOT PR 85](https://github.com/CDOT-CV/jpo-cvmanager/pull/85): Keycloak Custom User Provider
- [CDOT PR 97](https://github.com/CDOT-CV/jpo-cvmanager/pull/97): Rename 'snmp_version' column of rsus table
- [CDOT PR 103](https://github.com/CDOT-CV/jpo-cvmanager/pull/103): Fix Email TLS and Auth Defaults
- [CDOT PR 104](https://github.com/CDOT-CV/jpo-cvmanager/pull/104): RSU Errors Page
- [CDOT PR 105](https://github.com/CDOT-CV/jpo-cvmanager/pull/105): Feature/intersection tables
- [CDOT PR 106](https://github.com/CDOT-CV/jpo-cvmanager/pull/106): Feature/cimms decoder page
- [CDOT PR 107](https://github.com/CDOT-CV/jpo-cvmanager/pull/107): Firmware Manager bug fixes
- [CDOT PR 108](https://github.com/CDOT-CV/jpo-cvmanager/pull/108): Firmware Manager Upgrade Scheduler and Runner
- [CDOT PR 109](https://github.com/CDOT-CV/jpo-cvmanager/pull/109): Maximum Retry Limit for Firmware Upgrades
- [CDOT PR 112](https://github.com/CDOT-CV/jpo-cvmanager/pull/112): Simplified consecutive firmware upgrade failure count tracking
- [CDOT PR 113](https://github.com/CDOT-CV/jpo-cvmanager/pull/113): Collapsible Map Menu
- [CDOT PR 114](https://github.com/CDOT-CV/jpo-cvmanager/pull/114): Catching Intersection Data Upload Errors
- [CDOT PR 116](https://github.com/CDOT-CV/jpo-cvmanager/pull/116): Feature flags
- [CDOT PR 117](https://github.com/CDOT-CV/jpo-cvmanager/pull/117): OBU OTA Server FQDN Fix
- [CDOT PR 119](https://github.com/CDOT-CV/jpo-cvmanager/pull/119): Conflictvisualizer api integration
- [CDOT PR 120](https://github.com/CDOT-CV/jpo-cvmanager/pull/120): Fixing DateTimePicker errors
- [CDOT PR 121](https://github.com/CDOT-CV/jpo-cvmanager/pull/121): MUI Theming
- [CDOT PR 122](https://github.com/CDOT-CV/jpo-cvmanager/pull/122): Intersection dashboard hotfix
- [CDOT PR 123](https://github.com/CDOT-CV/jpo-cvmanager/pull/123): Remove RSU map info table
- [CDOT PR 124](https://github.com/CDOT-CV/jpo-cvmanager/pull/124): Architecture and Dataflow Flowchart
- [CDOT PR 125](https://github.com/CDOT-CV/jpo-cvmanager/pull/125): Keycloak User Provider Migration
- [CDOT PR 126](https://github.com/CDOT-CV/jpo-cvmanager/pull/126): Aborting Intersection Requests When Leaving the intersectionMap page
- [CDOT PR 128](https://github.com/CDOT-CV/jpo-cvmanager/pull/128): Allowing Consecutive WebSocket Reconnects
- [CDOT PR 130](https://github.com/CDOT-CV/jpo-cvmanager/pull/130): Fixing Pytest Warnings
- [CDOT PR 132](https://github.com/CDOT-CV/jpo-cvmanager/pull/132): Update Email Validation Logic
- [CDOT PR 136](https://github.com/CDOT-CV/jpo-cvmanager/pull/136): Normalizing line endings
- [CDOT PR 137](https://github.com/CDOT-CV/jpo-cvmanager/pull/137): Fixing CI Checks
- [CDOT PR 140](https://github.com/CDOT-CV/jpo-cvmanager/pull/140): Adding embedded mongo and postgres
- [CDOT PR 141](https://github.com/CDOT-CV/jpo-cvmanager/pull/141): Re-Adding RSU Online/SCMS Status Switch
- [CDOT PR 143](https://github.com/CDOT-CV/jpo-cvmanager/pull/143): Switching Zonky to use internal postgres instead of docker version
- [CDOT PR 150](https://github.com/CDOT-CV/jpo-cvmanager/pull/150): Geo Message Query Updates & Transfer to Processed BSM Data
- [CDOT PR 151](https://github.com/CDOT-CV/jpo-cvmanager/pull/151): Fixing Map Menu Bugs And Sample RSU Data
- [USDOT PR 34](https://github.com/usdot-jpo-ode/jpo-cvmanager/pull/34): Update GitHub Actions Third-Party Action Versions

Known issues/limitations:

- Cross-organization security restrictions have not been fully implemented or tested

## Version 1.4.0

### **Summary**

This release includes the integration of the jpo-conflictvisualizer's web application directly into the CV Manager web application, the addition of the Commsignia OBU OTA firmware server, customizable user and organization level emails, Material UI theming, and many smaller changes and bug fixes. The integration of the jpo-conflictvisualizer expands the set of required services to run the jpo-cvmanager so the docker-compose has been updated to support this. You can read more about the specifics of the jpo-conflict visualizer at its repository [here](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer). In the future, this documentation will be integrated into the jpo-cvmanager repository. The OBU OTA server is specifically developed for the Commsignia OBU OTA functionality and currently does not support other devices. The email customization menu is for user level emails only and organization level emails will override these.

Enhancements in this release:

- USDOT PR 22: Optional TLS/Authentication for emailer support.
- CDOT PR 101: Organization level emails override user email preferences and send emails to a specified group email. (Such as Google Groups)
- CDOT PR 100: Enhanced firmware manager logs for better RSU identification on failures.
- CDOT 99: PostgreSQL based logging for the OBU OTA firmware server for recording OBU requests to the server.
- CDOT PR 95: The CV Manager's web application Admin page's 'Add' and 'Edit' menus for RSUs, Users, and Organizations has been reworked as a dialog box using Material UI.
- CDOT PR 94: Material UI theming throughout the CV Manager web application.
- CDOT PR 93: CIMMS dashboard integration.
- CDOT PR 89: Update TIM message forwarding configurations to use the TX table instead of the RX table.
- CDOT PR 88: Reduces the ping requirement strictness for the ping checker in the rsu_status_check service due to false negatives during testing.
- CDOT PR 86: OBU OTA server bug fixes.
- CDOT PR 84: Adds logic for the iss_health_check to check for missing fields in the ISS response object.
- CDOT PR 83: CV Manager web application bug fix for correctly populating consecutive selected RSU/User/Organization from the Admin pin.
- CDOT PR 82: Adds toast notifications to the CV Manager web application.
- CDOT PR 81: Adds an email customization menu on the CV Manager web application and API to support allowing users to customize which kinds of emails to receive.
- CDOT PR 80: Resolve issue with the organization dropdown not affecting the web application display.
- CDOT PR 78: Upgrade Material UI version from v4 to v5.
- CDOT PR 77: Fixes the orphaned RSU and user bug that was possible to create from the CV Manager web application.
- CDOT PR 76: Add local mongoDB to the jpo-cvmanager repository.
- CDOT PR 75: OBU OTA server added as a service to support Commsignia OBU OTA firmware upgrades.
- CDOT PR 58: Native jpo-conflictvisualizer integration directly into the jpo-cvmanager web application.
- CDOT PR 90, 91, 92, 96, 98: Miscellaneous unit test and bug fixes.

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
