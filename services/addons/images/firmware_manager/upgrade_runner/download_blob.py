import logging
import os
from common.util import validate_file_type


def download_docker_blob(blob_name, destination_file_name):
    """Copy a file from a directory mounted as a volume in a Docker container to a local file.

    Args:
        blob_name (str): The name of the file in the directory.
        destination_file_name (str): The name of the local file to copy the directory file to.
    """

    if not validate_file_type(blob_name):
        return False

    directory = "/mnt/blob_storage"
    source_file_name = f"{directory}/{blob_name}"
    os.system(f"cp {source_file_name} {destination_file_name}")
    logging.info(
        f"Copied storage object {blob_name} from directory {directory} to local file {destination_file_name}."
    )
    return True
