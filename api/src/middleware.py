from werkzeug.wrappers import Request, Response
from google.oauth2 import id_token
import google.auth.transport.requests
import logging
import os
import pgquery

def get_user_role(idinfo):
  # Extract important info to query on from authorized token response
  email = idinfo["email"]

  query = "SELECT jsonb_build_object('email', u.email, 'first_name', u.first_name, 'last_name', u.last_name, 'organization', org.name, 'role', roles.name, 'super_user', u.super_user) " \
          "FROM public.users u " \
          "JOIN public.user_organization uo on u.user_id = uo.user_id " \
          "JOIN public.organizations org on uo.organization_id = org.organization_id " \
          "JOIN public.roles on uo.role_id = roles.role_id " \
          f"WHERE u.email = '{email}'"

  logging.debug(f'Executing query "{query};"...')
  data = pgquery.query_db(query)
  
  if len(data) != 0:
    return data
  return None

organization_required = {
  '/rsu-google-auth': False,
  '/rsuinfo': True,
  '/rsu-online-status': True,
  '/rsucounts': True,
  '/rsu-command': True,
  '/rsu-map-info': True,
  '/iss-scms-status': True,
  '/wzdx-feed': False,
  '/rsu-bsm-data': False,
  '/rsu-ssm-srm-data': False,
  '/admin-new-rsu': False,
  '/admin-rsu': False,
  '/admin-new-user': False,
  '/admin-user': False,
  '/admin-new-org': False,
  '/admin-org': False,
  '/rsu-geo-query': True
}

class Middleware():
  def __init__(self, app):
    self.app = app
  
  def __call__(self, environ, start_response):
    request = Request(environ)
    logging.info(f'Request - {request.method} {request.path}')

    # Do not bother authorizing a CORS check
    if request.method == "OPTIONS":
      return self.app(environ, start_response)
    
    # If request is a health check, do not authorize
    if request.path == "/":
      return self.app(environ, start_response)

    # if request is hitting the /contact-support endpoint, do not authorize
    if request.path == "/contact-support":
      return self.app(environ, start_response)
    
    try:
      # Verify user token ID is a real token
      token_id = request.headers['Authorization']
      idinfo = id_token.verify_oauth2_token(token_id, google.auth.transport.requests.Request(), os.environ["GOOGLE_CLIENT_ID"])

      # Verify authorized user
      data = get_user_role(idinfo)
      if data:
        user_info = {
          'name': f'{data[0][0]["first_name"]} {data[0][0]["last_name"]}',
          'email': data[0][0]["email"],
          'organizations': [],
          'super_user': data[0][0]["super_user"] == "1"
        }

        # Parse the organization permissions
        for org in data:
          user_info['organizations'].append({
            'name': org[0]['organization'],
            'role': org[0]['role']
          })
        environ['user_info'] = user_info

        # If endpoint requires, check if user is permitted for the specified organization
        permitted = False
        if organization_required[request.path]:
          requested_org = request.headers['Organization']
          for permission in user_info['organizations']:
            if permission['name'] == requested_org:
              permitted = True
              environ['organization'] = permission['name']
              environ['role'] = permission['role']
        elif 'admin' in request.path:
          if user_info['super_user']:
            permitted = True
        else:
          permitted = True
        
        if permitted:
          return self.app(environ, start_response)
      
      res = Response('User unauthorized', status=401)
      return res(environ, start_response)
    except Exception as e:
      # Throws an exception if not valid
      logging.exception(f"Invalid token for reason: {e}")
      res = Response('Authorization failed', status=401)
      return res(environ, start_response)