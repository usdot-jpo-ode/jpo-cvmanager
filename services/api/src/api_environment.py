from common.common_environment import get_env_var


APPLICATION_PORT = int(get_env_var("FLASK_RUN_PORT", "5000"))

ENABLE_RSU_FEATURES = get_env_var("ENABLE_RSU_FEATURES", "true").lower() != "false"
ENABLE_INTERSECTION_FEATURES = (
    get_env_var("ENABLE_INTERSECTION_FEATURES", "true").lower() != "false"
)
ENABLE_WZDX_FEATURES = get_env_var("ENABLE_WZDX_FEATURES", "true").lower() != "false"

KEYCLOAK_ENDPOINT = get_env_var("KEYCLOAK_ENDPOINT", error=True)
KEYCLOAK_REALM = get_env_var("KEYCLOAK_REALM", error=True)
KEYCLOAK_API_CLIENT_ID = get_env_var("KEYCLOAK_API_CLIENT_ID", error=True)
KEYCLOAK_API_CLIENT_SECRET_KEY = get_env_var(
    "KEYCLOAK_API_CLIENT_SECRET_KEY", error=True
)

CORS_DOMAIN = get_env_var("CORS_DOMAIN", "*")

MONGO_DB_URI = get_env_var("MONGO_DB_URI", "mongodb://localhost:27017/", warn=True)
MONGO_DB_NAME = get_env_var("MONGO_DB_NAME", "CV", warn=True)
MONGO_SSM_COLLECTION_NAME = get_env_var("MONGO_SSM_COLLECTION_NAME")
MONGO_SRM_COLLECTION_NAME = get_env_var("MONGO_SRM_COLLECTION_NAME")
MONGO_PROCESSED_BSM_COLLECTION_NAME = get_env_var(
    "MONGO_PROCESSED_BSM_COLLECTION_NAME", "ProcessedBsm", warn=False
)
MONGO_PROCESSED_PSM_COLLECTION_NAME = get_env_var(
    "MONGO_PROCESSED_PSM_COLLECTION_NAME", "ProcessedPsm", warn=False
)
MAX_GEO_QUERY_RECORDS = int(get_env_var("MAX_GEO_QUERY_RECORDS", "10000", warn=False))

ENVIRONMENT_NAME = get_env_var("ENVIRONMENT_NAME")
LOGS_LINK = get_env_var("LOGS_LINK")

WZDX_ENDPOINT = get_env_var("WZDX_ENDPOINT", error=ENABLE_WZDX_FEATURES)
WZDX_API_KEY = get_env_var("WZDX_API_KEY", error=ENABLE_WZDX_FEATURES)

FIRMWARE_MANAGER_ENDPOINT = get_env_var("FIRMWARE_MANAGER_ENDPOINT", warn=False)

ENABLE_ERROR_EMAILS = get_env_var("ENABLE_ERROR_EMAILS", "false").lower() != "false"
IAPI_ENDPOINT = get_env_var("IAPI_ENDPOINT", error=ENABLE_ERROR_EMAILS)
KC_SA_CLIENT_ID = get_env_var(
    "KC_SA_CLIENT_ID", "sa_cvmanager_python_api", error=ENABLE_ERROR_EMAILS
)
KC_SA_CLIENT_SECRET = get_env_var("KC_SA_CLIENT_SECRET", error=ENABLE_ERROR_EMAILS)
