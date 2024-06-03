from google.cloud import storage
import logging
import os


# Only supports GCP Bucket Storage for downloading blobs
def download_gcp_blob(blob_name, destination_file_name):
    gcp_project = os.environ.get("GCP_PROJECT")
    bucket_name = os.environ.get("BLOB_STORAGE_BUCKET")
    storage_client = storage.Client(gcp_project)
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(blob_name)
    if blob.exists():
        blob.download_to_filename(destination_file_name)
        logging.info(
            f"Downloaded storage object {blob_name} from bucket {bucket_name} to local file {destination_file_name}."
        )
        return True
    return False


def list_gcs_blobs(gcs_prefix, file_extension):
    files = []
    gcp_project = os.environ.get("GCP_PROJECT")
    bucket_name = os.environ.get("BLOB_STORAGE_BUCKET")
    logging.debug(f"Listing blobs in bucket {bucket_name} with prefix {gcs_prefix}.")
    storage_client = storage.Client(gcp_project)
    blobs = storage_client.list_blobs(bucket_name, prefix=gcs_prefix, delimiter="/")
    blob_count = 0
    download_count = 0
    for blob in blobs:
        logging.debug(f"Blob: {blob.name}")
        if blob.name.endswith(file_extension):
            path = f"/firmwares/{blob.name.split(gcs_prefix)[-1]}"
            files.append(path)
            if not os.path.exists(path):
                blob.download_to_filename(path)
                logging.debug(f"Downloaded blob {blob.name} to {path}")
                download_count += 1
            else:
                logging.debug(f"File {path} already exists, skipping download.")
        else:
            logging.debug(f"Blob {blob.name} does not end with {file_extension}")
        blob_count += 1
    logging.debug(f"Found {blob_count} blobs, and downloaded {download_count} files.")
    logging.debug(f"Found {len(files)} files with extension {file_extension}.")
    return files
