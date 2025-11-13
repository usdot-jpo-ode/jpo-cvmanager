from common.common_environment import get_env_var

def process_count_msg_types(type_string: str) -> list[str]:
    """Process the COUNTS_MSG_TYPES environment variable into a list of uppercase strings"""
    return [t.strip().upper() for t in type_string.split(",") if t.strip()]


ENABLE_RSU_FEATURES=get_env_var("ENABLE_RSU_FEATURES", "true").lower() != "false"
ENABLE_INTERSECTION_FEATURES=get_env_var("ENABLE_INTERSECTION_FEATURES", "true").lower() != "false"
ENABLE_WZDX_FEATURES=get_env_var("ENABLE_WZDX_FEATURES", "true").lower() != "false"
ENABLE_MOOVE_AI_FEATURES=get_env_var("ENABLE_MOOVE_AI_FEATURES", "true").lower() != "false"

KEYCLOAK_ENDPOINT=get_env_var("KEYCLOAK_ENDPOINT", error=True)
KEYCLOAK_REALM=get_env_var("KEYCLOAK_REALM", error=True)
KEYCLOAK_API_CLIENT_ID=get_env_var("KEYCLOAK_API_CLIENT_ID", error=True)
KEYCLOAK_API_CLIENT_SECRET_KEY=get_env_var("KEYCLOAK_API_CLIENT_SECRET_KEY", error=True)

CORS_DOMAIN=get_env_var("CORS_DOMAIN", "*")
CSM_EMAILS_TO_SEND_TO=get_env_var("CSM_EMAILS_TO_SEND_TO", "").split(",")
CSM_EMAIL_TO_SEND_FROM=get_env_var("CSM_EMAIL_TO_SEND_FROM")
CSM_TARGET_SMTP_SERVER_ADDRESS = get_env_var("CSM_TARGET_SMTP_SERVER_ADDRESS")
CSM_TARGET_SMTP_SERVER_PORT = int(get_env_var("CSM_TARGET_SMTP_SERVER_PORT", "587"))
CSM_TLS_ENABLED = get_env_var("CSM_TLS_ENABLED", "true").lower() == "true"
CSM_AUTH_ENABLED = get_env_var("CSM_AUTH_ENABLED", "true").lower() == "true"
CSM_EMAIL_APP_USERNAME = get_env_var("CSM_EMAIL_APP_USERNAME", warn=CSM_AUTH_ENABLED)
CSM_EMAIL_APP_PASSWORD = get_env_var("CSM_EMAIL_APP_PASSWORD", warn=CSM_AUTH_ENABLED)

MONGO_DB_URI = get_env_var("MONGO_DB_URI", "mongodb://localhost:27017/", warn=True)
MONGO_DB_NAME = get_env_var("MONGO_DB_NAME", "CV", warn=True)
MONGO_SSM_COLLECTION_NAME=get_env_var("MONGO_SSM_COLLECTION_NAME")
MONGO_SRM_COLLECTION_NAME=get_env_var("MONGO_SRM_COLLECTION_NAME")
MONGO_PROCESSED_BSM_COLLECTION_NAME=get_env_var("MONGO_PROCESSED_BSM_COLLECTION_NAME", "ProcessedBsm", warn=False)
MONGO_PROCESSED_PSM_COLLECTION_NAME=get_env_var("MONGO_PROCESSED_PSM_COLLECTION_NAME", "ProcessedPsm", warn=False)
MAX_GEO_QUERY_RECORDS=int(get_env_var("MAX_GEO_QUERY_RECORDS", "10000", warn=False))
COUNTS_MSG_TYPES=process_count_msg_types(get_env_var("COUNTS_MSG_TYPES", "BSM,SSM,SPAT,SRM,MAP", warn=False))

ENVIRONMENT_NAME=get_env_var("ENVIRONMENT_NAME")
LOGS_LINK=get_env_var("LOGS_LINK")

WZDX_ENDPOINT=get_env_var("WZDX_ENDPOINT", error=ENABLE_WZDX_FEATURES)
WZDX_API_KEY=get_env_var("WZDX_API_KEY", error=ENABLE_WZDX_FEATURES)

FIRMWARE_MANAGER_ENDPOINT=get_env_var("FIRMWARE_MANAGER_ENDPOINT", warn=False)

GCP_PROJECT_ID = get_env_var("GCP_PROJECT_ID", warn=ENABLE_MOOVE_AI_FEATURES)
MOOVE_AI_SEGMENT_AGG_STATS_TABLE = get_env_var(
    "MOOVE_AI_SEGMENT_AGG_STATS_TABLE", warn=ENABLE_MOOVE_AI_FEATURES
)
MOOVE_AI_SEGMENT_EVENT_STATS_TABLE = get_env_var(
    "MOOVE_AI_SEGMENT_EVENT_STATS_TABLE", warn=ENABLE_MOOVE_AI_FEATURES
)
