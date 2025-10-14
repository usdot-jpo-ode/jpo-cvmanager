from typing import Any
from flask_restful import Resource
import logging
import common.pgquery as pgquery
import api_environment

from common.auth_tools import (
    ORG_ROLE_LITERAL,
    EnvironWithOrg,
    PermissionResult,
    require_permission,
    generate_sql_placeholders_for_list,
)


def get_rsu_data(user: EnvironWithOrg, qualified_orgs: list[str]):

    # Execute the query and fetch all results
    query = (
        "SELECT jsonb_build_object('type', 'Feature', 'id', row.rsu_id, 'geometry', ST_AsGeoJSON(row.geography)::jsonb, 'properties', to_jsonb(row)) "
        "FROM ("
        "SELECT rd.rsu_id, rd.geography, rd.milepost, rd.ipv4_address, rd.serial_number, rd.primary_route, rm.name AS model_name, man.name AS manufacturer_name "
        "FROM public.rsus AS rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rd.model "
        "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer "
    )

    where_clause = None
    params: dict[str, Any] = {}
    if user.organization:
        where_clause = "ron_v.name = :user_org"
        params["user_org"] = user.organization
    if not user.user_info.super_user:
        org_names_placeholder, _ = generate_sql_placeholders_for_list(
            qualified_orgs, params_to_update=params
        )
        where_clause = f"ron_v.name IN ({org_names_placeholder})"
    if where_clause:
        query += f" WHERE {where_clause}"
    query += ") as row"

    logging.debug(f'Executing query "{query};"')
    data = pgquery.query_db(query, params=params)

    logging.info("Parsing results...")
    result: dict[str, Any] = {"rsuList": []}
    for row in data:
        row = dict(row[0])
        result["rsuList"].append(row)
    return result


# REST endpoint resource class
class RsuInfo(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
        "Access-Control-Allow-Headers": "Content-Type,Authorization,Organization",
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
        required_role=ORG_ROLE_LITERAL.USER,
    )
    def get(self, permission_result: PermissionResult):
        logging.debug("RsuInfo GET requested")
        return (
            get_rsu_data(permission_result.user, permission_result.qualified_orgs),
            200,
            self.headers,
        )
