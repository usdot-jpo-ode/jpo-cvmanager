import api_environment

from flask_restful import Resource

from common.auth_tools import PermissionResult, require_permission


class UserAuth(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "GET",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    @require_permission(
        required_role=None,
    )
    def get(self, permission_result: PermissionResult):
        # Check for user info and return data

        return (
            permission_result.user.user_info.to_dict(),
            200,
            self.headers,
        )
