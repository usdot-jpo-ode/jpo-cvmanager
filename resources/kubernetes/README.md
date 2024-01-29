# Kubernetes Deployment Scripts

The CV Manager supports being hosted within a Kubernetes cluster which allows for better stability, smoother deployments and performance scaling. The YAML files within this directory provide a starting point to incorporate the CV Manager into your own Helm deployments in any local or cloud based Kubernetes environment.

## Requirements

The webapp and API both utilize a K8s Ingress to handle external access to the applications. These Ingress enforce HTTPS and host a ManagedCertificate that require a domain name and SSL policy that must be created and handled outside of the K8s templates provided here. These would be created by the cloud service being utilized or on your own if the CV Manager is being run in a local K8s solution.

The YAML files use GCP specific specifications for various values such as "networking.gke.io/managed-certificates". These values will not work on AWS and Azure but there should be equivalent fields that these specifications can be updated to if needing to deploy in another cloud environment.

The environment variables must be set according to the README documentation for each application. The iss-health-check application only supports GCP.

## Useful Links

- [Learn about and get started with Kubernetes](https://kubernetes.io/docs/tutorials/kubernetes-basics/)
- [Use Helm to help with Kubernetes deployments](https://helm.sh/)
- Cloud Kubernetes Solutions
  - [GCP Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine)
  - [AWS Elastic Kubernetes Service](https://aws.amazon.com/eks/)
  - [Azure Kubernetes Service](https://azure.microsoft.com/en-us/products/kubernetes-service)
