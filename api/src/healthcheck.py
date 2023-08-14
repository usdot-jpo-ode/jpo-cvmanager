# REST endpoint resource class
from flask_restful import Resource

class HealthCheck(Resource):
  options_headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'GET',
    'Access-Control-Max-Age': '3600'
  }

  headers = {
    'Access-Control-Allow-Origin': '*',
    'Content-Type': 'application/json'
  }

  def options(self):
    # CORS support
    return ('', 204, self.options_headers)
  
  def get(self):
    return ('Health response', 200, self.headers)