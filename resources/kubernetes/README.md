# Kubernetes Deployment Scripts

The CV Manager supports being hosted within a Kubernetes cluster which allows for better stability, smoother deployments and performance scaling. The YAML files within this directory provide a starting point to incorporate the CV Manager into your own Helm deployments in any local or cloud based Kubernetes environment.

## Requirements

The webapp and API both utilize a K8s Ingress to handle external access to the applications. These Ingress enforce HTTPS and host a ManagedCertificate that require a domain name and SSL policy that must be created and handled outside of the K8s templates provided here. These would be created by the cloud service being utilized or on your own if the CV Manager is being run in a local K8s solution.

The YAML files use GCP specific specifications for various values such as "networking.gke.io/managed-certificates". These values will not work on AWS and Azure but there should be equivalent fields that these specifications can be updated to if needing to deploy in another cloud environment.

The environment variables must be set according to the README documentation for each application. The iss-health-check application supports GCP or postgres for storing keys. The environment variables for the iss-health-check application must be set according to the README documentation for the iss-health-check application.

## Database Migrations (Flyway)

Schema initialization and migrations are managed by Flyway via `cv-manager-flyway.yaml`. This replaces the old `pg-init-tables` ConfigMap approach, which only ran on a fresh (empty) database and contained a stale schema missing many tables.

The Flyway Job uses a custom Docker image built from `resources/db/Dockerfile`. This image bundles the versioned migration SQL files directly — `R__sample_data.sql` is excluded because the Dockerfile copies only `V*.sql` files.

**Building and pushing the image:**

CI publishes this image automatically. On every merge to `develop` or `cdot-release*`, the `build_flyway_image` workflow job builds and pushes to:

```
ghcr.io/<github-org>/cvmanager-flyway:sha-<short-sha>
ghcr.io/<github-org>/cvmanager-flyway:<branch-name>
```

To deploy a migration set, copy the `sha-<short-sha>` tag from the CI run that corresponds to the commit you want, then update the `image:` field in `cv-manager-flyway.yaml` and re-apply the Job.

To build and push manually (e.g. from a fork without CI configured):

```sh
docker build -f resources/db/Dockerfile -t ghcr.io/<your-org>/cvmanager-flyway:<tag> resources/db/
docker push ghcr.io/<your-org>/cvmanager-flyway:<tag>
```

Update the `image:` field in `cv-manager-flyway.yaml` to match the pushed tag before deploying.

**Deployment order:**

1. Apply Postgres and the Flyway Job together:
   ```sh
   kubectl apply -f cv-manager-postgres.yaml
   kubectl apply -f cv-manager-flyway.yaml
   ```
2. Wait for the Job to complete:
   ```sh
   kubectl wait --for=condition=complete job/cv-manager-flyway-migrate --timeout=120s
   ```
3. Apply remaining services. The `cv-manager-api` Deployment includes an init container that polls `flyway_schema_history` and will not start until migrations succeed.

**Adding a new migration:**

1. Create `V{N}__description.sql` in `resources/db/migration/` following the naming conventions in [`resources/db/README.md`](../db/README.md).
2. Merge to `develop` or `cdot-release*`. CI rebuilds and pushes the image automatically (the Dockerfile `COPY migration/V*.sql` glob picks up new files). To build manually, see the instructions above.
3. Update the `image:` tag in `cv-manager-flyway.yaml`, delete the old Job, and re-apply:
   ```sh
   kubectl delete job cv-manager-flyway-migrate
   kubectl apply -f cv-manager-flyway.yaml
   ```

## Useful Links

- [Learn about and get started with Kubernetes](https://kubernetes.io/docs/tutorials/kubernetes-basics/)
- [Use Helm to help with Kubernetes deployments](https://helm.sh/)
- Cloud Kubernetes Solutions
  - [GCP Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine)
  - [AWS Elastic Kubernetes Service](https://aws.amazon.com/eks/)
  - [Azure Kubernetes Service](https://azure.microsoft.com/en-us/products/kubernetes-service)
