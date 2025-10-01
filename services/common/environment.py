from util import get_env_var, configure_logging

LOGGING_LEVEL=configure_logging()
TIMEZONE=get_env_var("TIMEZONE", "America/Denver")

GCP_PROJECT=get_env_var("GCP_PROJECT")
BLOB_STORAGE_BUCKET=get_env_var("BLOB_STORAGE_BUCKET")

PG_DB_HOST=get_env_var("PG_DB_HOST", error=True)
PG_DB_USER=get_env_var("PG_DB_USER", error=True)
PG_DB_PASS=get_env_var("PG_DB_PASS", error=True)
PG_DB_NAME=get_env_var("PG_DB_NAME", error=True)
INSTANCE_CONNECTION_NAME=get_env_var("INSTANCE_CONNECTION_NAME", "").strip()