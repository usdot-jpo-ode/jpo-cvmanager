from common.common_environment import get_env_var


def process_storage_type(val: str, default: str) -> str:
    if not val:
        return default
    value = val.lower()
    if value not in ["postgres", "gcp"]:
        raise ValueError("Invalid STORAGE_TYPE. Must be 'postgres' or 'gcp'.")
    return value


PROJECT_ID=get_env_var("PROJECT_ID", error=True)
ISS_API_KEY=get_env_var("ISS_API_KEY", error=True)
ISS_API_KEY_NAME=get_env_var("ISS_API_KEY_NAME", error=True)
ISS_KEY_TABLE_NAME=get_env_var("ISS_KEY_TABLE_NAME", error=True)
ISS_SCMS_TOKEN_REST_ENDPOINT=get_env_var("ISS_SCMS_TOKEN_REST_ENDPOINT", error=True)
ISS_SCMS_VEHICLE_REST_ENDPOINT=get_env_var("ISS_SCMS_VEHICLE_REST_ENDPOINT", error=True)
ISS_PROJECT_ID=get_env_var("ISS_PROJECT_ID", error=True)

STORAGE_TYPE = process_storage_type(get_env_var("STORAGE_TYPE", warn=False), "postgres")
