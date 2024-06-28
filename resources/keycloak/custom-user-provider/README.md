### Relevant Articles:

- [Using Custom User Providers with Keycloak](https://www.baeldung.com/java-keycloak-custom-user-providers)
- [Custom Protocol Mapper with Keycloak](https://www.baeldung.com/keycloak-custom-protocol-mapper)

```
mvn package
docker build -t keycloak-custom .
docker run -p 8084:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin keycloak-custom start-dev
```
