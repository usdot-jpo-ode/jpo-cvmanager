from google.cloud import secretmanager
import requests
import os
import json
import uuid
import logging


def create_secret(client, secret_id, parent):
    """Create a new GCP secret in GCP Secret Manager
    client: GCP Security Manager client
    secret_id: ID of the secret being created
    parent: GCP secret manager parent ID for the GCP project
    """
    client.create_secret(
        request={
            "parent": parent,
            "secret_id": secret_id,
            "secret": {"replication": {"automatic": {}}},
        }
    )
    logging.debug("New secret created")


def check_if_secret_exists(client, secret_id, parent):
    """Check if a secret exists in GCP Secret Manager
    client: GCP Security Manager client
    secret_id: ID of the secret being checked
    parent: GCP secret manager parent ID for the GCP project
    """
    for secret in client.list_secrets(
        request=secretmanager.ListSecretsRequest(parent=parent)
    ):
        # secret names are in the form of "projects/project_id/secrets/secret_id"
        if secret.name.split("/")[-1] == secret_id:
            logging.debug(f"Secret {secret_id} exists")
            return True
    return False


def get_latest_secret_version(client, secret_id, parent):
    """Get latest value of a secret from GCP Secret Manager
    client: GCP Security Manager client
    secret_id: ID for the secret being retrieved
    parent: GCP secret manager parent ID for the GCP project
    """
    response = client.access_secret_version(
        request={"name": f"{parent}/secrets/{secret_id}/versions/latest"}
    )
    return json.loads(response.payload.data.decode("UTF-8"))


def add_secret_version(client, secret_id, parent, data):
    """Add a new version to an existing secret
    client: GCP Security Manager client
    secret_id: ID for the secret
    parent: GCP secret manager parent ID for the GCP project
    data: String value for the new version of the secret
    """
    client.add_secret_version(
        request={
            "parent": f"{parent}/secrets/{secret_id}",
            "payload": {"data": str.encode(json.dumps(data))},
        }
    )
    logging.debug("New version added")


def get_token():
    client = secretmanager.SecretManagerServiceClient()
    secret_id = "iss-token-secret"
    parent = f"projects/{os.environ['PROJECT_ID']}"

    # Check to see if the GCP secret exists
    secret_exists = check_if_secret_exists(client, secret_id, parent)

    if secret_exists:
        # Grab the latest token data
        value = get_latest_secret_version(client, secret_id, parent)
        friendly_name = value["name"]
        token = value["token"]
        logging.debug(f"Received token: {friendly_name}")
    else:
        # If there is no available ISS token secret, create secret
        logging.debug("Secret does not exist, creating secret")
        create_secret(client, secret_id, parent)
        # Use environment variable for first run with new secret
        token = os.environ["ISS_API_KEY"]

    # Pull new ISS SCMS API token
    iss_base = os.environ["ISS_SCMS_TOKEN_REST_ENDPOINT"]

    # Create HTTP request headers
    iss_headers = {"x-api-key": token}

    # Create the POST body
    new_friendly_name = f"{os.environ['ISS_API_KEY_NAME']}_{str(uuid.uuid4())}"
    iss_post_body = {"friendlyName": new_friendly_name, "expireDays": 1}

    # Create new ISS SCMS API Token to ensure its freshness
    logging.debug("POST: " + iss_base)
    response = requests.post(iss_base, json=iss_post_body, headers=iss_headers)
    new_token = response.json()["Item"]
    logging.debug(f"Received new token: {new_friendly_name}")

    if secret_exists:
        # If exists, delete previous API key to prevent key clutter
        iss_delete_body = {"friendlyName": friendly_name}
        requests.delete(iss_base, json=iss_delete_body, headers=iss_headers)
        logging.debug(f"Old token has been deleted from ISS SCMS: {friendly_name}")

    version_data = {"name": new_friendly_name, "token": new_token}

    add_secret_version(client, secret_id, parent, version_data)

    return new_token
