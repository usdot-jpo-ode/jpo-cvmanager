from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import logging
import common.pgquery as pgquery
from sqlalchemy.exc import IntegrityError
import os
import admin_new_user
from werkzeug.exceptions import InternalServerError, BadRequest
from common.auth_tools import require_permission


def check_safe_input(org_spec):
    special_characters = "!\"#$%&'()*@-+,./:;<=>?[\\]^`{|}~"
    # Check all string based fields for special characters
    if any(c in special_characters for c in org_spec["name"]):
        return False
    return True


def add_organization(org_spec):
    # Check for special characters for potential SQL injection
    if not check_safe_input(org_spec):
        raise BadRequest(
            "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
        )

    if org_spec["email"]:
        if org_spec["email"] != "" and not admin_new_user.check_email(
            org_spec["email"]
        ):
            raise BadRequest("Organization email is not valid")

    try:
        org_insert_query = (
            "INSERT INTO public.organizations(name, email) "
            f"VALUES ('{org_spec['name']}', '{org_spec['email']}')"
        )
        pgquery.write_db(org_insert_query)
    except IntegrityError as e:
        if e.orig is None:
            raise InternalServerError("Encountered unknown issue") from e
        failed_value = e.orig.args[0]["D"]
        failed_value = failed_value.replace("(", '"')
        failed_value = failed_value.replace(")", '"')
        failed_value = failed_value.replace("=", " = ")
        print(f"Exception encountered: {failed_value}")
        raise InternalServerError(failed_value)
    except InternalServerError:
        # Re-raise InternalServerError without catching it
        raise
    except Exception as e:
        print(f"Exception encountered: {e}")
        raise InternalServerError("Encountered unknown issue")

    return {"message": "New organization successfully added"}


# REST endpoint resource class
class AdminNewOrgSchema(Schema):
    name = fields.Str(required=True)
    email = fields.Str(required=True, allow_none=True)


class AdminNewOrg(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    @require_permission(required_role=None)
    def post(self):
        logging.debug("AdminNewOrg POST requested")
        # Check for main body values
        schema = AdminNewOrgSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        return (add_organization(request.json), 200, self.headers)
