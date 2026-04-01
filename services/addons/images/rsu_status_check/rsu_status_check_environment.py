from common.common_environment import get_env_var

RSU_PING=get_env_var("RSU_PING", "False", warn=False).lower() == "true"
ZABBIX=get_env_var("ZABBIX", "False", warn=False).lower() == "true"
STALE_PERIOD_HOURS=int(get_env_var("STALE_PERIOD", "24", warn=False))
RSU_MSGFWD_FETCH=get_env_var("RSU_MSGFWD_FETCH", "False", warn=False).lower() == "true"

ZABBIX_ENDPOINT=get_env_var("ZABBIX_ENDPOINT", error=True)
ZABBIX_USER=get_env_var("ZABBIX_USER", error=True)
ZABBIX_PASSWORD=get_env_var("ZABBIX_PASSWORD", error=True)
