from common.common_environment import get_env_var

BLOB_STORAGE_PROVIDER=get_env_var("BLOB_STORAGE_PROVIDER", "DOCKER", warn=False)
UPGRADE_SCHEDULER_ENDPOINT=get_env_var("UPGRADE_SCHEDULER_ENDPOINT", "127.0.0.1")

SMTP_SERVER_IP=get_env_var("SMTP_SERVER_IP", error=True)
SMTP_SERVER_PORT=int(get_env_var("SMTP_SERVER_PORT", "587", warn=False))
SMTP_EMAIL=get_env_var("SMTP_EMAIL", error=True)
DEPLOYMENT_TITLE=get_env_var("DEPLOYMENT_TITLE", "Example Deployment", warn=True)
SMTP_USERNAME=get_env_var("SMTP_USERNAME", error=True)
SMTP_PASSWORD=get_env_var("SMTP_PASSWORD", error=True)
