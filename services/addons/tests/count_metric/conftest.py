import os

os.environ["IAPI_ENDPOINT"] = "localhost:8089"
os.environ["KC_ENDPOINT"] = "http://localhost:8084"
os.environ["KC_REALM"] = "cvmanager"
os.environ["KC_SA_CLIENT_ID"] = "sa_count_metric"
os.environ["KC_SA_CLIENT_SECRET"] = "sa_count_metric_secret"
