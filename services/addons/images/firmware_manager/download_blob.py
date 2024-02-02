from google.cloud import storage
import logging
import os


# Download a blob from GCP Bucket Storage
def download_gcp_blob(blob_name, destination_file_name):
    gcp_project = os.environ.get("GCP_PROJECT")
    bucket_name = os.environ.get("BLOB_STORAGE_BUCKET")
    storage_client = storage.Client(gcp_project)
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(blob_name)
    blob.download_to_filename(destination_file_name)
    logging.info(
        f"Downloaded storage object {blob_name} from bucket {bucket_name} to local file {destination_file_name}."
    )


# "Download" a blob from a directory mounted as a volume in a Docker container
def download_docker_blob(blob_name, destination_file_name):
    directory = "/mnt/blob_storage"
    source_file_name = f"{directory}/{blob_name}"
    os.system(f"cp {source_file_name} {destination_file_name}")
    logging.info(
        f"Copied storage object {blob_name} from directory {directory} to local file {destination_file_name}."
    )
