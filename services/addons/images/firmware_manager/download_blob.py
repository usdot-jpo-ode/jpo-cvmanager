from google.cloud import storage
import logging
import os

# Only supports GCP Bucket Storage for downloading blobs
def download_gcp_blob(blob_name, destination_file_name):
  bucket_name = os.environ.get('BLOB_STORAGE_BUCKET')
  storage_client = storage.Client()
  bucket = storage_client.bucket(bucket_name)
  blob = bucket.blob(blob_name)
  blob.download_to_filename(destination_file_name)
  logging.info(f"Downloaded storage object {blob_name} from bucket {bucket_name} to local file {destination_file_name}.")