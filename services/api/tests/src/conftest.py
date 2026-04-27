import os
import sys
from os.path import dirname, join, abspath

# Add the services and api/src directories to the path so that imports work
# during testing. This is necessary because of the project structure.
current_dir = dirname(abspath(__file__))
# current_dir is .../services/api/tests/src
root_dir = abspath(join(current_dir, "..", "..", "..", ".."))

sys.path.insert(0, join(root_dir, "services", "common"))
sys.path.insert(0, join(root_dir, "services", "api", "src"))
sys.path.insert(0, join(root_dir, "services"))

os.environ["KEYCLOAK_ENDPOINT"] = "keycloak-endpoint"
os.environ["KEYCLOAK_REALM"] = "keycloak-realm"
os.environ["KEYCLOAK_API_CLIENT_ID"] = "keycloak-api-client-id"
os.environ["KEYCLOAK_API_CLIENT_SECRET_KEY"] = "keycloak-api-client-secret-key"
os.environ["CSM_AUTH_ENABLED"] = "false"
os.environ["WZDX_ENDPOINT"] = "wzdx-endpoint"
os.environ["WZDX_API_KEY"] = "wzdx-api-key"
os.environ["CORS_DOMAIN"] = "test.com"
os.environ["ENABLE_ERROR_EMAILS"] = "true"
os.environ["IAPI_ENDPOINT"] = "http://localhost:8089"
os.environ["KC_SA_CLIENT_ID"] = "sa_cvmanager_python_api"
os.environ["KC_SA_CLIENT_SECRET"] = "sa-cvmanager-python-api-secret"
