import os
import logging


def configure_logging() -> str:
    LOGGING_LEVEL = os.environ.get("LOGGING_LEVEL", "INFO")
    if not LOGGING_LEVEL or LOGGING_LEVEL == "":
        LOGGING_LEVEL = "INFO"
    logging.basicConfig(format="%(levelname)s:%(message)s", level=LOGGING_LEVEL)
    return LOGGING_LEVEL


def get_env_var(key: str, default: str | None = None, error=False, warn=True):
    value = os.environ.get(key)
    if value is None or value == "":
        if error:
            raise Exception(f"Missing required environment variable: {key}")
        if warn:
            logging.warning(
                f"Missing environment variable: {key}, using default: {default}"
            )
        else:
            logging.info(
                f"Environment variable {key} was not specified, using default: {default}"
            )
        return default
    logging.info(f"Environment variable {key} is set to {value}")
    return value


LOGGING_LEVEL = configure_logging()
TIMEZONE = get_env_var("TIMEZONE", "America/Denver")

GCP_PROJECT = get_env_var("GCP_PROJECT")
BLOB_STORAGE_BUCKET = get_env_var("BLOB_STORAGE_BUCKET")

PG_DB_HOST = get_env_var("PG_DB_HOST")
PG_DB_USER = get_env_var("PG_DB_USER")
PG_DB_PASS = get_env_var("PG_DB_PASS")
PG_DB_NAME = get_env_var("PG_DB_NAME")
INSTANCE_CONNECTION_NAME = get_env_var("INSTANCE_CONNECTION_NAME", "").strip()
