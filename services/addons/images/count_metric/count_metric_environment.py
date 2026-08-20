from common.common_environment import get_env_var

DEPLOYMENT_TITLE = get_env_var("DEPLOYMENT_TITLE", "Example Deployment", warn=True)
MONGO_DB_URI = get_env_var("MONGO_DB_URI", "mongodb://localhost:27017")
MONGO_DB_NAME = get_env_var("MONGO_DB_NAME", "CV")
IAPI_ENDPOINT = get_env_var("IAPI_ENDPOINT", error=True)
KC_ENDPOINT = get_env_var("KC_ENDPOINT", error=True)
KC_REALM = get_env_var("KC_REALM", error=True)
KC_SA_CLIENT_ID = get_env_var("KC_SA_CLIENT_ID", "sa_count_metric")
KC_SA_CLIENT_SECRET = get_env_var("KC_SA_CLIENT_SECRET", error=True, secret=True)
