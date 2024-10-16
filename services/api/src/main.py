from flask import Flask
from flask_restful import Api
import os
import logging

# Custom script imports
from middleware import Middleware
from admin_email_notification import AdminNotification
from admin_new_email_notification import AdminNewNotification
from userauth import UserAuth
from healthcheck import HealthCheck
from rsuinfo import RsuInfo
from rsu_querycounts import RsuQueryCounts
from rsu_querymsgfwd import RsuQueryMsgFwd
from rsu_online_status import RsuOnlineStatus
from rsu_commands import RsuCommandRequest
from rsu_map_info import RsuMapInfo
from rsu_geo_query import RsuGeoQuery
from wzdx_feed import WzdxFeed
from rsu_geo_msg_query import RsuGeoData
from iss_scms_status import IssScmsStatus
from rsu_ssm_srm import RsuSsmSrmData
from admin_new_rsu import AdminNewRsu
from admin_rsu import AdminRsu
from admin_new_intersection import AdminNewIntersection
from admin_intersection import AdminIntersection
from admin_new_user import AdminNewUser
from admin_user import AdminUser
from admin_new_org import AdminNewOrg
from admin_org import AdminOrg
from contact_support import ContactSupportResource
import smtp_error_handler

log_level = os.environ.get("LOGGING_LEVEL", "INFO")
logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

app = Flask(__name__)

smtp_error_handler.configure_error_emails(app)

app.wsgi_app = Middleware(app.wsgi_app)
api = Api(app)

api.add_resource(HealthCheck, "/")
api.add_resource(UserAuth, "/user-auth")
api.add_resource(RsuInfo, "/rsuinfo")
api.add_resource(RsuOnlineStatus, "/rsu-online-status")
api.add_resource(RsuQueryCounts, "/rsucounts")
api.add_resource(RsuQueryMsgFwd, "/rsu-msgfwd-query")
api.add_resource(RsuCommandRequest, "/rsu-command")
api.add_resource(RsuMapInfo, "/rsu-map-info")
api.add_resource(RsuGeoQuery, "/rsu-geo-query")
api.add_resource(WzdxFeed, "/wzdx-feed")
api.add_resource(RsuGeoData, "/rsu-geo-msg-data")
api.add_resource(IssScmsStatus, "/iss-scms-status")
api.add_resource(RsuSsmSrmData, "/rsu-ssm-srm-data")
api.add_resource(AdminNewRsu, "/admin-new-rsu")
api.add_resource(AdminRsu, "/admin-rsu")
api.add_resource(AdminNewIntersection, "/admin-new-intersection")
api.add_resource(AdminIntersection, "/admin-intersection")
api.add_resource(AdminNewUser, "/admin-new-user")
api.add_resource(AdminUser, "/admin-user")
api.add_resource(AdminNewOrg, "/admin-new-org")
api.add_resource(AdminOrg, "/admin-org")
api.add_resource(AdminNotification, "/admin-notification")
api.add_resource(AdminNewNotification, "/admin-new-notification")
api.add_resource(ContactSupportResource, "/contact-support")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
