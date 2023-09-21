from unittest.mock import MagicMock, patch, Mock
from src import pgquery
import sqlalchemy
import os
import json
from flask import Flask
from flask_restful import Api


# test that init_tcp_connection_engine is calling sqlalchemy.create_engine with expected arguments
@patch('src.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
def test_init_tcp_connection_engine():
    # mock return values for function dependencies
    sqlalchemy.create_engine = MagicMock(
        return_value = "myengine"
    )
    sqlalchemy.engine.url.URL.create = MagicMock(
        return_value = "myurl"
    )

    # call function
    db_user = "user"
    db_pass = "pass"
    db_name = "mydatabase"
    db_hostname = "myhostname"
    db_port = 3000
    engine_pool = pgquery.init_tcp_connection_engine(db_user, db_pass, db_name, db_hostname, db_port)
    
    # check return value
    assert(engine_pool == "myengine")

    # check that sqlalchemy.engine.url.URL.create was called with expected arguments
    sqlalchemy.engine.url.URL.create.assert_called_once_with(
        drivername='postgresql+pg8000',
        username=db_user,
        password=db_pass,
        host=db_hostname,
        port=db_port,
        database=db_name
        )
    
    # check that sqlalchemy.create_engine was called with expected arguments
    my_db_config = {'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800}
    sqlalchemy.create_engine.assert_called_once_with("myurl", **my_db_config) 

    @patch.object(Api, 'add_resource')
    def test_api_endpoints(mock_add_resource):
        endpoints = ["/user-auth", "/rsuinfo", "/rsu-online-status", "/rsucounts", 
                 "/rsu-command", "/rsu-map-info", "/rsu-geo-query", "/wzdx-feed",
                 "/rsu-bsm-data", "/iss-scms-status", "/rsu-ssm-srm-data", "/admin-new-rsu",
                 "/admin-rsu", "/admin-new-user", "/admin-user", "/admin-new-org", "/admin-org"]
        resource = MagicMock()  # Mock resource object
        api = Api(Flask(__name__))  # Create an instance of the API
        for endpoint in endpoints:
            api.add_resource(resource, endpoint)
        for call in mock_add_resource.call_args_list:
            _, args, _ = call
            assert args[1] in endpoints, f"{args[1]} not in {endpoints}"
    