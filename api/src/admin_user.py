import logging
import pgquery
import sqlalchemy
import admin_new_user
import os

def get_user_data(user_email):
  query = "SELECT email, first_name, last_name, super_user, receive_error_emails, org.name, roles.name AS role " \
    "FROM public.users " \
    "JOIN public.user_organization AS uo ON uo.user_id = users.user_id " \
    "JOIN public.organizations AS org ON org.organization_id = uo.organization_id " \
    "JOIN public.roles ON roles.role_id = uo.role_id"
  if user_email != "all":
    query += f" WHERE email = '{user_email}'"

  data = pgquery.query_db(query)

  user_dict = {}
  for point in data:
    if point['email'] not in user_dict:
      user_dict[point['email']] = {
        'email': point['email'],
        'first_name': point['first_name'],
        'last_name': point['last_name'],
        'super_user': True if point['super_user'] == '1' else False,
        'receive_error_emails': True if point['receive_error_emails'] == '1' else False,
        'organizations': []
      }
    user_dict[point['email']]['organizations'].append({
      'name': point['name'],
      'role': point['role']
    })

  user_list = list(user_dict.values())
  # If list is empty and a single user was requested, return empty object
  if len(user_list) == 0 and user_email != "all":
    return {}
  # If list is not empty and a single user was requested, return the first index of the list
  elif len(user_list) == 1 and user_email != "all":
    return user_list[0]
  else:
    return user_list

def get_modify_user_data(user_email):
  modify_user_obj = {}
  modify_user_obj['user_data'] = get_user_data(user_email)
  if user_email != "all":
    modify_user_obj['allowed_selections'] = admin_new_user.get_allowed_selections()
  return modify_user_obj

def check_safe_input(user_spec):
  special_characters = "!\"#$%&'()*@-+,./:;<=>?[\]^`{|}~"
  # Check all string based fields for special characters
  if any(c in special_characters for c in user_spec['first_name']):
    return False
  if any(c in special_characters for c in user_spec['last_name']):
    return False
  for org in user_spec['organizations_to_add']:
    if any(c in special_characters for c in org['name']):
      return False
    if any(c in special_characters for c in org['role']):
      return False
  for org in user_spec['organizations_to_modify']:
    if any(c in special_characters for c in org['name']):
      return False
    if any(c in special_characters for c in org['role']):
      return False
  for org in user_spec['organizations_to_remove']:
    if any(c in special_characters for c in org['name']):
      return False
    if any(c in special_characters for c in org['role']):
      return False
  return True

def modify_user(user_spec):
  # Check for special characters for potential SQL injection
  if not admin_new_user.check_email(user_spec['email']) or not admin_new_user.check_email(user_spec['orig_email']):
    return {"message": "Email is not valid"}, 500
  if not check_safe_input(user_spec):
    return {"message": "No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\]^`{|}~. No sequences of '-' characters are allowed"}, 500

  try:
    # Modify the existing user data
    query = "UPDATE public.users SET " \
          f"email='{user_spec['email']}', " \
          f"first_name='{user_spec['first_name']}', " \
          f"last_name='{user_spec['last_name']}', " \
          f"super_user='{'1' if user_spec['super_user'] else '0'}', " \
          f"receive_error_emails='{'1' if user_spec['receive_error_emails'] else '0'}' " \
          f"WHERE email = '{user_spec['orig_email']}'"
    pgquery.insert_db(query)

    # Add the user-to-organization relationships
    if len(user_spec['organizations_to_add']) > 0:
      org_add_query = "INSERT INTO public.user_organization(user_id, organization_id, role_id) VALUES"
      for organization in user_spec['organizations_to_add']:
        org_add_query += " (" \
                    f"(SELECT user_id FROM public.users WHERE email = '{user_spec['email']}'), " \
                    f"(SELECT organization_id FROM public.organizations WHERE name = '{organization['name']}'), " \
                    f"(SELECT role_id FROM public.roles WHERE name = '{organization['role']}')" \
                    "),"
      org_add_query = org_add_query[:-1]
      pgquery.insert_db(org_add_query)

    # Modify the user-to-organization relationships
    for organization in user_spec['organizations_to_modify']:
      org_modify_query = "UPDATE public.user_organization " \
                  f"SET role_id = (SELECT role_id FROM public.roles WHERE name = '{organization['role']}') " \
                  f"WHERE user_id = (SELECT user_id FROM public.users WHERE email = '{user_spec['email']}') " \
                  f"AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = '{organization['name']}')"
      pgquery.insert_db(org_modify_query)

    # Remove the user-to-organization relationships
    for organization in user_spec['organizations_to_remove']:
      org_remove_query = "DELETE FROM public.user_organization WHERE " \
                  f"user_id = (SELECT user_id FROM public.users WHERE email = '{user_spec['email']}') " \
                  f"AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = '{organization['name']}')"
      pgquery.insert_db(org_remove_query)
  except sqlalchemy.exc.IntegrityError as e:
    failed_value = e.orig.args[0]['D']
    failed_value = failed_value.replace('(', '"')
    failed_value = failed_value.replace(')', '"')
    failed_value = failed_value.replace('=', ' = ')
    logging.error(f"Exception encountered: {failed_value}")
    return {"message": failed_value}, 500
  except Exception as e:
    logging.error(f"Exception encountered: {e}")
    return {"message": "Encountered unknown issue"}, 500

  return {"message": "User successfully modified"}, 200

def delete_user(user_email):
  # Delete user-to-organization relationships
  org_remove_query = "DELETE FROM public.user_organization WHERE " \
        f"user_id = (SELECT user_id FROM public.users WHERE email = '{user_email}')"
  pgquery.insert_db(org_remove_query)

  # Delete user data
  user_remove_query = "DELETE FROM public.users WHERE " \
        f"email = '{user_email}'"
  pgquery.insert_db(user_remove_query)

  return {"message": "User successfully deleted"}

# REST endpoint resource class
from flask import request, abort
from flask_restful import Resource
from marshmallow import Schema, fields
import urllib.request

class AdminUserGetDeleteSchema(Schema):
  user_email = fields.Str(required=True)

class UserOrganizationSchema(Schema):
  name = fields.Str(required=True)
  role = fields.Str(required=True)

class AdminUserPatchSchema(Schema):
  orig_email = fields.Str(required=True)
  email = fields.Str(required=True)
  first_name = fields.Str(required=True)
  last_name = fields.Str(required=True)
  super_user = fields.Bool(required=True)
  receive_error_emails = fields.Bool(required=True)
  organizations_to_add = fields.List(fields.Nested(UserOrganizationSchema), required=True)
  organizations_to_modify = fields.List(fields.Nested(UserOrganizationSchema), required=True)
  organizations_to_remove = fields.List(fields.Nested(UserOrganizationSchema), required=True)

class AdminUser(Resource):
  options_headers = {
    'Access-Control-Allow-Origin': os.environ["CORS_DOMAIN"],
    'Access-Control-Allow-Headers': 'Content-Type,Authorization',
    'Access-Control-Allow-Methods': 'GET,PATCH,DELETE',
    'Access-Control-Max-Age': '3600'
  }

  headers = {
    'Access-Control-Allow-Origin': os.environ["CORS_DOMAIN"],
    'Content-Type': 'application/json'
  }

  def options(self):
    # CORS support
    return ('', 204, self.options_headers)

  def get(self):
    logging.debug("AdminUser GET requested")
    schema = AdminUserGetDeleteSchema()
    errors = schema.validate(request.args)
    if errors:
      logging.error(errors)
      abort(400, errors)
    
    user_email = urllib.request.unquote(request.args['user_email'])
    return (get_modify_user_data(user_email), 200, self.headers)

  def patch(self):
    logging.debug("AdminUser PATCH requested")
    # Check for main body values
    schema = AdminUserPatchSchema()
    errors = schema.validate(request.json)
    if errors:
      logging.error(str(errors))
      abort(400, str(errors))
    
    data, code = modify_user(request.json)
    return (data, code, self.headers)
  
  def delete(self):
    logging.debug("AdminUser DELETE requested")
    schema = AdminUserGetDeleteSchema()
    errors = schema.validate(request.args)
    if errors:
      logging.error(errors)
      abort(400, errors)
    
    user_email = urllib.request.unquote(request.args['user_email'])
    return (delete_user(user_email), 200, self.headers)