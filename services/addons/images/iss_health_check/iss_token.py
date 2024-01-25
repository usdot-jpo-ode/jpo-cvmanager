from google.cloud import secretmanager
import common.pgquery as pgquery
import requests
import os
import json
import uuid
import logging

# Get storage type from environment variable
def get_storage_type():
    """Get the storage type for the ISS SCMS API token
    """
    if os.environ["STORAGE_TYPE"] == "gcp":
        return "gcp"
    elif os.environ["STORAGE_TYPE"] == "postgres":
        return "postgres"
    else:
        # default to gcp
        logging.debug("STORAGE_TYPE environment variable not set, defaulting to gcp")
        return "gcp"


# GCP Secret Manager functions
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


# Postgres functions
def check_if_data_exists(table_name):
    """Check if data exists in the table
    table_name: name of the table
    """
    # create the query
    query = f"SELECT * FROM {table_name}"
    # execute the query
    data = pgquery.query_db(query)
    # check if data exists
    if len(data) > 0:
        return True
    else:
        return False


def get_latest_data(table_name):
    """Get latest value of a token from the table
    table_name: name of the table
    """
    # create the query
    query = f"SELECT * FROM {table_name} ORDER BY iss_key_id DESC LIMIT 1"
    # execute the query
    data = pgquery.query_db(query)
    # return the data
    toReturn = {}
    toReturn["id"] = data[0][0] # id
    toReturn["name"] = data[0][1] # common_name
    toReturn["token"] = data[0][2] # token
    logging.debug(f"Received token: {toReturn['name']} with id {toReturn['id']}")
    return toReturn


def add_data(table_name, common_name, token):
    """Add a new token to the table
    table_name: name of the table
    data: String value for the new token
    """
    # create the query
    query = f"INSERT INTO {table_name} (common_name, token) VALUES ('{common_name}', '{token}')"
    # execute the query
    pgquery.write_db(query)


# Main function
def get_token():
    storage_type = get_storage_type()
    if storage_type == "gcp":
        client = secretmanager.SecretManagerServiceClient()
        secret_id = "iss-token-secret"
        parent = f"projects/{os.environ['PROJECT_ID']}"

        # Check to see if the GCP secret exists
        data_exists = check_if_secret_exists(client, secret_id, parent)

        if data_exists:
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
    elif storage_type == "postgres":
        key_table_name = os.environ["ISS_KEY_TABLE_NAME"]
        
        # check to see if data exists in the table
        data_exists = check_if_data_exists(key_table_name)

        if data_exists:
            # grab the latest token data
            value = get_latest_data(key_table_name)
            id = value["id"]
            friendly_name = value["name"]
            token = value["token"]
            logging.debug(f"Received token: {friendly_name} with id {id}")
        else:
            # if there is no data, use environment variable for first run
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

    if data_exists:
        # If exists, delete previous API key to prevent key clutter
        iss_delete_body = {"friendlyName": friendly_name}
        requests.delete(iss_base, json=iss_delete_body, headers=iss_headers)
        logging.debug(f"Old token has been deleted from ISS SCMS: {friendly_name}")

    version_data = {"name": new_friendly_name, "token": new_token}

    if get_storage_type() == "gcp":
        # Add new version to the secret
        add_secret_version(client, secret_id, parent, version_data)
    elif get_storage_type() == "postgres":
        # add new entry to the table
        add_data(key_table_name, new_friendly_name, new_token)

    return new_token
