import logging
import common.pgquery as pgquery
import sqlalchemy
import os
import admin_new_user


def check_safe_input(org_spec):
    special_characters = "!\"#$%&'()*@-+,./:;<=>?[\]^`{|}~"
    # Check all string based fields for special characters
    if any(c in special_characters for c in org_spec["name"]):
        return False
    return True


def add_organization(org_spec):
    # Check for special characters for potential SQL injection
    if not check_safe_input(org_spec):
        return {
            "message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\]^`{|}~. No sequences of '-' characters are allowed"
        }, 500

    if not admin_new_user.check_email(org_spec["email"]):
        return {"message": "Organization email is not valid"}, 500

    try:
        org_insert_query = (
            "INSERT INTO public.organizations(name, email) "
            f"VALUES ('{org_spec['name']}', '{org_spec['email']}')"
        )
        pgquery.write_db(org_insert_query)
    except sqlalchemy.exc.IntegrityError as e:
        failed_value = e.orig.args[0]["D"]
        failed_value = failed_value.replace("(", '"')
        failed_value = failed_value.replace(")", '"')
        failed_value = failed_value.replace("=", " = ")
        print(f"Exception encountered: {failed_value}")
        return {"message": failed_value}, 500
    except Exception as e:
        print(f"Exception encountered: {e}")
        return {"message": "Encountered unknown issue"}, 500

    return {"message": "New organization successfully added"}, 200


# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields, validate


class AdminNewOrgSchema(Schema):
    name = fields.Str(required=True)
    email = fields.Str(required=True)


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

    def post(self):
        logging.debug("AdminNewOrg POST requested")
        # Check for main body values
        schema = AdminNewOrgSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            abort(400, str(errors))

        data, code = add_organization(request.json)
        return (data, code, self.headers)
