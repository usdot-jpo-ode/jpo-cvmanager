from common.common_environment import get_env_var

SMTP_SERVER_IP=get_env_var("SMTP_SERVER_IP", error=True)
SMTP_SERVER_PORT=int(get_env_var("SMTP_SERVER_PORT", "587", warn=False))
SMTP_EMAIL=get_env_var("SMTP_EMAIL", error=True)
DEPLOYMENT_TITLE=get_env_var("DEPLOYMENT_TITLE", "Example Deployment", warn=True)
SMTP_USERNAME=get_env_var("SMTP_USERNAME", error=True)
SMTP_PASSWORD=get_env_var("SMTP_PASSWORD", error=True)
MONGO_DB_URI=get_env_var("MONGO_DB_URI", "mongodb://localhost:27017")
MONGO_DB_NAME=get_env_var("MONGO_DB_NAME", "CV")
