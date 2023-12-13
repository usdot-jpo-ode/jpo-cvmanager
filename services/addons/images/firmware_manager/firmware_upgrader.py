from google.cloud import storage
import logging

# Only supports GCP Bucket Storage for downloading blobs
def download_gcp_blob(bucket_name, source_blob_name, destination_file_name):
  storage_client = storage.Client()
  bucket = storage_client.bucket(bucket_name)
  blob = bucket.blob(source_blob_name)
  blob.download_to_filename(destination_file_name)
  logging.info(f"Downloaded storage object {source_blob_name} from bucket {bucket_name} to local file {destination_file_name}.")

