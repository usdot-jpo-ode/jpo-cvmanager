# Intersection API Swagger Documentation

Static documentation for the intersection api can be accessed by opening the docs.html page in a browser.

When running the intersection-api, live documentation can be access at [http://localhost:8089/swagger-ui/index.html](http://localhost:8089/swagger-ui/index.html)

## Retrieving the openapi.json

Static documentation can be viewed through opening the static page [docs.html](docs.html) in a browser, and is generated through downloading the openapi.json from the api docs page: [http://localhost:8089/swagger-ui/index.html](http://localhost:8089/swagger-ui/index.html), and pressing "/v3/api-docs" right below the OpenAPI definition title at the top

## Update docs.html

To update the docs.html with the new swagger json, simply copy the openapi.json contents into spec of the the window.onload function, then re-load the docs.html page

**IMPORTANT** Change the version of the openapi json from 3.0.1 to 3.0.0

## YAML

To convert the json to yaml, the json into [editor.swagger.io](https://editor.swagger.io/) and copy out the converted yaml file
