from flask import Flask
from flask_restful import Api
import api_environment
import logging

# Custom script imports
from middleware import Middleware
from admin_email_notification import AdminNotification
from admin_new_email_notification import AdminNewNotification
from healthcheck import HealthCheck
from rsu_querycounts import RsuQueryCounts
from rsu_querymsgfwd import RsuQueryMsgFwd
from rsu_online_status import RsuOnlineStatus
from rsu_commands import RsuCommandRequest
from rsu_snmp_fwd_fetch import RsuSnmpFwdFetch
from rsu_geo_query import RsuGeoQuery
from wzdx_feed import WzdxFeed
from rsu_geo_msg_query import RsuGeoData
from rsu_ssm_srm import RsuSsmSrmData
from admin_new_org import AdminNewOrg
from admin_org import AdminOrg, AdminOrgTimDeposit, AdminOrgSnmpMonitoring
import smtp_error_handler
from common import common_environment

logging.info(
    "CVManager API running with LOGGING_LEVEL: " + str(common_environment.LOGGING_LEVEL)
)

app = Flask(__name__)

if api_environment.ENABLE_ERROR_EMAILS:
    smtp_error_handler.configure_error_emails(app)

app.wsgi_app = Middleware(app.wsgi_app)


@app.after_request
def apply_cors_header(response):
    # Add CORS header to all responses to prevent webapp parsing errors. Webapps have trouble handling responses that do not have the Access-Control-Allow-Origin header set.
    response.headers["Access-Control-Allow-Origin"] = api_environment.CORS_DOMAIN
    return response


api = Api(app)

api.add_resource(HealthCheck, "/")
api.add_resource(AdminNewOrg, "/admin-new-org")
api.add_resource(AdminOrg, "/admin-org")
api.add_resource(AdminOrgTimDeposit, "/admin-org-tim-deposit")
api.add_resource(AdminOrgSnmpMonitoring, "/admin-org-snmp-monitoring")
api.add_resource(AdminNotification, "/admin-notification")
api.add_resource(AdminNewNotification, "/admin-new-notification")

if api_environment.ENABLE_RSU_FEATURES:
    api.add_resource(RsuOnlineStatus, "/rsu-online-status")
    api.add_resource(RsuQueryCounts, "/rsucounts")
    api.add_resource(RsuQueryMsgFwd, "/rsu-msgfwd-query")
    api.add_resource(RsuSnmpFwdFetch, "/rsu-msgfwd-fetch")
    api.add_resource(RsuCommandRequest, "/rsu-command")
    api.add_resource(RsuGeoQuery, "/rsu-config-geo-query")
    api.add_resource(RsuGeoData, "/rsu-geo-msg-data")
    api.add_resource(RsuSsmSrmData, "/rsu-ssm-srm-data")
if api_environment.ENABLE_WZDX_FEATURES:
    api.add_resource(WzdxFeed, "/wzdx-feed")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=api_environment.APPLICATION_PORT)
