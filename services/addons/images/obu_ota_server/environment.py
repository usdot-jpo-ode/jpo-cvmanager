from common.common_environment import get_env_var

OTA_USERNAME=get_env_var("OTA_USERNAME", "admin")
OTA_PASSWORD=get_env_var("OTA_PASSWORD", error=True)
BLOB_STORAGE_PROVIDER=get_env_var("BLOB_STORAGE_PROVIDER", "DOCKER")
BLOB_STORAGE_PATH=get_env_var("BLOB_STORAGE_PATH", "DOCKER")
SERVER_HOST=get_env_var("SERVER_HOST", "localhost")
NGINX_ENCRYPTION=get_env_var("NGINX_ENCRYPTION", "plain", warn=False)
MAX_COUNT=int(get_env_var("MAX_COUNT", 10, warn=False))
