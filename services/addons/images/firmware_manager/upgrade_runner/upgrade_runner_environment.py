from common.common_environment import get_env_var

BLOB_STORAGE_PROVIDER = get_env_var("BLOB_STORAGE_PROVIDER", "DOCKER", warn=False)
UPGRADE_SCHEDULER_ENDPOINT = get_env_var("UPGRADE_SCHEDULER_ENDPOINT", "127.0.0.1")

IAPI_ENDPOINT = get_env_var("IAPI_ENDPOINT", error=True)
KC_ENDPOINT = get_env_var("KC_ENDPOINT", error=True)
KC_REALM = get_env_var("KC_REALM", error=True)
KC_SA_CLIENT_ID = get_env_var("KC_SA_CLIENT_ID", "sa_firmware_upgrade_runner")
KC_SA_CLIENT_SECRET = get_env_var("KC_SA_CLIENT_SECRET", error=True, secret=True)
