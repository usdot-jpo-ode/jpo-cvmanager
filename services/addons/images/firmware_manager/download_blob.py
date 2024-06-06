import logging
import os


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


def validate_file_type(file_name):
    """Validate the file type of the file to be downloaded.

    Args:
        file_name (str): The name of the file to be downloaded.
    """
    if not file_name.endswith(".tar"):
        logging.error(
            f"Unsupported file type for storage object {file_name}. Only .tar files are supported."
        )
        return False
    return True


class UnsupportedFileTypeException(Exception):
    def __init__(self, message="Unsupported file type. Only .tar files are supported."):
        self.message = message
        super().__init__(self.message)
