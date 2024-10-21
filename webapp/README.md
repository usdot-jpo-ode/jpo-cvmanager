# CV Manager Web Application

This is a web application that is made with React JS that is a front-end for interfacing with CDOT RSUs. The code for the application is being hosted in a public repository to allow other CV projects to utilize. Automated deployment of the application is not included within the project.

## Required Tools For Running The CV Manager React Webapp

- Mapbox Access Token
  - Create account at https://www.mapbox.com/
  - An access token will be provided on the account page once the account has been created
  - Put the access key in the "sample.env.local" file for REACT_APP_MAPBOX_TOKEN and rename the file ".env.development.local"
- npm
  - Download instructions: https://docs.npmjs.com/downloading-and-installing-node-js-and-npm
- Nodejs
  - Download instructions: https://nodejs.org/en/download/
- jpo-conflictmonitor (required to have data for the conflict visualizer)
  - Repository: https://github.com/usdot-jpo-ode/jpo-conflictmonitor
  - Follow the instructions in the README to build/deploy the tool

## Building The Application

1. Setup environment variables
   - The project includes a sample environment variable file in [sample.env.local](sample.env.local) for running the application locally.
   - Use the dev, test and prod environment var files for deploying in different environments. The commented out environment variables are required.
2. Build the application
   - Building for local: `npm run build`
   - Building for specific environment: `npm run build:dev`
   - Build for all environments: `npm run build:all`

## Editing Mapbox Style

### Edit Styles

1. Login to https://www.mapbox.com/ and go to https://studio.mapbox.com/
2. Select "Upload Style"
3. Locate the 'cdot-web-app/style/style.json' and upload it
4. Now you can make edits

### Save Styles

1. Once you are done editing, save the style
2. Click share style
3. Download zip
4. Paste the new "style.json" inside the zip in 'cdot-web-app/style/'

To use a new style, the style URL from Mapbox Studio must be pasted in the ".env.local" file for REACT_APP_MAPBOX_STYLE.

## Google Cloud Storage (GCS) Web Hosting: Hosting the CV Manager React Webapp

1. Download the project locally using `git clone https://github.com/CDOT-CV/RSU_Management.git`
2. Navigate to the directory `cdot-web-app` within a command line or shell with npm installed
3. Install all of the dependencies for the project: `npm install`
4. Feel free to verify if the project runs locally: `npm start`
5. Build the project as a deployable build: `npm run build`
6. Create a GCP Cloud Storage bucket. Making the bucket's access rules secure is highly recommended but not required.
7. Upload the entire `build/` directory located at `RSU_Management/cdot-web-app/build/`
8. Upload `RSU_Management/cdot-web-app/app.yaml` separately to the root of the bucket. The only two things in the root will be this file and the build directory.
9. Open the GCP dashboard shell from the top right corner of the screen on the GCP dashboard.
10. Select the GCP project you created the bucket in `gcloud config set project [PROJECT_ID]`
11. Create a directory where the website will host its files from: `mkdir rsu_manager`
12. Sync the files from the bucket to this directory: `gsutil rsync -r gs://your-bucket-name rsu_manager/`
13. Change the active directory to the new directory: `cd rsu_manager`
14. Start the web server hosting: `gcloud app deploy`
15. This will take a few minutes but once it is done, it will display the URL for the hosted site.
16. Navigate to the URL to see the now running CV Manager Webapp.

## Redux Toolkit

Re-factoring RSU manager to utilize Redux Toolkit for state management

- Created individual slices for sets of unique data. Each slice has its own state, actions, and reducers.
  - Async Thunks receive actiond and make data requests
- Convert class components to functional components
- Create User and localstorage managers - managers.js
  - Hold methods for changing organization, validating loginData, and reading/updating localstorage
-

1. userSlice
   - Store user and authentication data. Token, authLoginData
   - Update localstorage on login/logout
   - References
     - App.js - Validate login in Tabs
     - Header.js - Support login, logout, and change organization
     - Configure.js - validate role
     - Map.js - read organization
     - configSlice.js - read access token and organization for requests
     - rsuSlice.js - read access token and organization for requests
     - wzdxSlice.js - read access token and organization for requests
2. rsuSlice
   - Main CV data - RSU, BSM, SSM, SRM
   - References
     - App.js - Read loading, getRsuData
     - Menu.js - updateRowData, change message type
     - RsuRebootMenu.js - read selected RSU IP
     - SnmpsetMenu.js - read selected RSU IP/manufacturer
     - SnmpwalkMenu.js - read selected RSU IP/manufacturer
     - MsgMap.js - Read and update BSM data
     - Configure.js - Read selected RSU IP/manufacturer
     - HeatMap.js - Read RSU counts/status
     - Map.js - Read map data, update display data
     - RsuMapView.js - Read rsu map data, toggleMapDisplay
3. configSlice
   - RSU Configuration
   - References
     - RsuRebootMenu.js - Reboot RSU
     - SnmpsetMenu.js - read/write SNMP Set data
     - SnmpwalkMenu.js - read/refresh SNMP Forward data
4. wzdxSlice
   - WZDx data
   - References
     - WzdxMap.js - read and load WZDx data

## ConflictVisualier Integration

The usdot [jpo-conflictvisualizer](https://github.com/usdot-jpo-ode/jpo-conflictvisualizer) is a tool that can be used to visualize the conflicts between Basic Safety Messages (BSMs), Signal Phase and Timing (SPaT) messages, and MAP messages. The CV Manager Web Application has been integrated with the ConflictVisualizer tool to allow users to view data directly from a [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor) instance. This integration currently requires an additional jpo-conflictvisualizer api to be deployed alongside the jpo-cvmanager api. This allows the webapp to make authenticated requests to the jpo-conflictvisualizer api to retrieve the conflict monitor data.

### Changes Made

The conflictvisualizer API is now integrated into the cvmanager as the intersection-api.

These changes were tested running locally in docker. These changes require an intersection-api to be running, and to be connected to the cvmanager keycloak server (and cvmanager keycloak realm). This API also requires the jpo-conflictmonitor and jpo-geojsonconverter to be running, so that there is data available. Once the jpo-conflictmonitor, jpo-geojsonconverter, and jpo-ode, then a jpo-conflictvisualizer api should be deployed, which should be modified to authenticate with the cvmanager keycloak realm (see the conflictvisualizer-map-page branch), and the port should be specified in the environment file (REACT_APP_CVIZ_API_SERVER_URL). Once all of these components are deployed, then the cvmanager webapp can be run!

## Unit Testing

The unit tests for this repository are written using Jest. These tests can be run with the following command:

```
npm run test
```

This command is smart, and will only re-run out of date tests. It will continually re-run any updated tests, until the command is ended.
If you would like to re-run all tests, simply press 'a' after the previous set of tests have completed.

Many of the UI components are being tested using Snapshots. These snapshots are saved versions of the rendered HTML (and are tracked in GIT). Each time one of these tests is run, it generated a current snapshot of what the page looks like now, and compares it to the saved snapshot. If there are any discrepencies, those are shown in the terminal. These tests are incredibly useful for catching and identifying unintended changes.

If you would like to update all snapshots to the current values, simply press 'u' after running the desired tests.

### Coverage

To generate the coverage report, use the following command:

```
npm run test:coverage
```

This will print a summary of the code coverage across the application. If you would like to see more information, and a breakdown of exactly which lines are covered for each code file, open the coverage report [coverage/lcov-report/index.html](./coverage/lcov-report/index.html) in a browser.

### Slice Tests

For many of the slice unit tests (for example [adminEditOrganizationSlice.test.js](./src/features/adminEditOrganization/adminEditOrganizationSlice.test.js#80)), the dispatch method is checked for the number of expected calls. When an async thunk is tested, the dispatch method is called twice by the async thunk, even when it is not explicitly called within the thunk. Therefore, when the number of dispatch calls are being tested, this is written as n + 2, where n is the expected manual calls and 2 is the number of default calls.
