from unittest.mock import patch

import src.ssh_commands as ssh_commands
import os

# shared variables
mock_reboot_request = {
  "rsu_ip": "192.168.0.20",
  "creds": {
    "username": "username",
    "password": "password"
  }
}

mock_osupdate_request = {
  "rsu_ip": "192.168.0.20",
  "creds": {
    "username": "username",
    "password": "password"
  },
  "args": {
    "manufacturer": "Cisco",
    "model": "Catalyst 9300",
    "update_type": "os",
    "update_name": "16",
    "image_name": "cat9k_iosxe.16.09.01.SPA.bin",
    "bash_script": "cat9k_iosxe.16.09.01.SPA.bin",
    "rescue_name": "16",
    "rescue_bash_script": "cat9k_iosxe.16.09.01.SPA.bin"
  }
}

mock_fwupdate_request = {
  "rsu_ip": "192.168.0.20",
  "creds": {
    "username": "username",
    "password": "password"
  },
  "args": {
    "manufacturer": "Cisco",
    "model": "Catalyst 9300",
    "update_type": "firmware",
    "update_name": "16",
    "image_name": "cat9k_iosxe.16.09.01.SPA.bin",
    "bash_script": "cat9k_iosxe.16.09.01.SPA.bin",
  }
}

### SNMPFILTER TESTS ###

@patch.dict(os.environ, {
    'RSU_REST_ENDPOINT': '172.10.10.100:5000'
})
def test_snmpfilter_not_commsignia():
  req = {
    "manufacturer": "test"
  }

  resp = ssh_commands.snmpfilter(req)
  assert resp == ("Target RSU is not of type Commsignia", 400)


@patch('requests.post')
@patch.dict(os.environ, {
    'RSU_REST_ENDPOINT': '172.10.10.100:5000'
})
def test_snmpfilter_post_call(mock_requests):
  req = {
    "rsu_ip": "8.8.8.8",
    "manufacturer": "Commsignia",
    "creds": {
      "username": "username",
      "password": "password"
    }
  }
  
  ssh_commands.snmpfilter(req)

  mock_requests.assert_called_with(
    'http://172.10.10.100:5000/snmpfilter',
    {
      "rsu_ip": "8.8.8.8",
      "username": "username",
      "password": "password"
    }
  )

@patch('requests.post')
@patch.dict(os.environ, {
    'RSU_REST_ENDPOINT': '172.10.10.100:5000'
})
def test_snmpfilter_post_response(mock_requests):
  req = {
    "rsu_ip": "8.8.8.8",
    "manufacturer": "Commsignia",
    "creds": {
      "username": "username",
      "password": "password"
    }
  }

  mock_requests.return_value.json.return_value = {'TestMessage': 'Test'}
  mock_requests.return_value.status_code = 200
  resp = ssh_commands.snmpfilter(req)

  assert resp == ({'TestMessage': 'Test'}, 200)

@patch.dict(os.environ, {
    'RSU_REST_ENDPOINT': '172.10.10.100:5000'
})
def test_snmpfilter_error():
  req = {
    "rsu_ip": "8.8.8.8",
    "manufacturer": "Commsignia"
  }

  resp = ssh_commands.snmpfilter(req)

  assert resp == ("Encountered an error with the command snmpfilter", 500)


### REBOOT TESTS ###

@patch('ssh_commands.requests.post')
def test_reboot(mock_requests_post):
  # mock requests.post
  mock_requests_post.return_value.json.return_value = {'TestMessage': 'Test'}
  mock_requests_post.return_value.status_code = 200

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com' 

  # call function
  resp = ssh_commands.reboot(mock_reboot_request)

  # assert that the response is correct
  expected_response = ({'TestMessage': 'Test'}, 200)
  assert resp == expected_response

  # assert calls
  mock_requests_post.assert_called_with(
    'http://myendpoint.com/reboot',
    {
      "rsu_ip": mock_reboot_request["rsu_ip"],
      "username": mock_reboot_request["creds"]["username"],
      "password": mock_reboot_request["creds"]["password"]
    }
  )

@patch('ssh_commands.requests.post')
def test_reboot_exception(mock_requests_post):
  # mock requests.post
  mock_requests_post.side_effect = Exception('Test Exception')

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com'

  # call function
  resp = ssh_commands.reboot(mock_reboot_request)

  # assert that the response is correct
  expected_response = ("Encountered an error with the command reboot", 500)
  assert resp == expected_response


### OSUPDATE TESTS ###

@patch('ssh_commands.requests.post')
def test_osupdate(mock_requests_post):
  # mock requests.post
  mock_requests_post.return_value.json.return_value = {'TestMessage': 'Test'}
  mock_requests_post.return_value.status_code = 200

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com'

  # call function
  resp = ssh_commands.osupdate(mock_osupdate_request)

  # assert that the response is correct
  expected_response = ({'TestMessage': 'Test'}, 200)
  assert resp == expected_response

  # assert calls
  mock_requests_post.assert_called_with(
    'http://myendpoint.com/osupdate',
    {
      "rsu_ip": mock_osupdate_request["rsu_ip"],
      "username": mock_osupdate_request["creds"]["username"],
      "password": mock_osupdate_request["creds"]["password"],
      "manufacturer": mock_osupdate_request["args"]["manufacturer"],
      "model": mock_osupdate_request["args"]["model"],
      "update_type": mock_osupdate_request["args"]["update_type"],
      "update_name": mock_osupdate_request["args"]["update_name"],
      "image_name": mock_osupdate_request["args"]["image_name"],
      "bash_script": mock_osupdate_request["args"]["bash_script"],
      "rescue_name": mock_osupdate_request["args"]["rescue_name"],
      "rescue_bash_script": mock_osupdate_request["args"]["rescue_bash_script"]
    }
  )

@patch('ssh_commands.requests.post')
def test_osupdate_schema_validation_error(mock_requests_post):
  # make request have bad data
  mock_osupdate_request["args"]["manufacturer"] = 123

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com'
  
  # call function
  resp = ssh_commands.osupdate(mock_osupdate_request)

  # assert that the response is correct
  expected_response = ("The provided args does not match required values: {'manufacturer': ['Not a valid string.']}", 400)
  assert resp == expected_response

  # assert that requests.post was not called
  mock_requests_post.assert_not_called()

  # return request to original state
  mock_osupdate_request["args"]["manufacturer"] = "Commsignia"

@patch('ssh_commands.requests.post')
def test_osupdate_exception(mock_requests_post):
  # mock requests.post
  mock_requests_post.side_effect = Exception('Test Exception')

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com'

  # call function
  resp = ssh_commands.osupdate(mock_osupdate_request)

  # assert that the response is correct
  expected_response = ("Encountered an error with the command osupdate", 500)
  assert resp == expected_response


### FWUPDATE TESTS ###

@patch('ssh_commands.requests.post')
def test_fwupdate(mock_requests_post):
  # mock requests.post
  mock_requests_post.return_value.json.return_value = {'TestMessage': 'Test'}
  mock_requests_post.return_value.status_code = 200

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com'

  # call function
  resp = ssh_commands.fwupdate(mock_fwupdate_request)

  # assert that the response is correct
  expected_response = ({'TestMessage': 'Test'}, 200)
  assert resp == expected_response

  # assert calls
  mock_requests_post.assert_called_with(
    'http://myendpoint.com/fwupdate',
    {
      "rsu_ip": mock_fwupdate_request["rsu_ip"],
      "username": mock_fwupdate_request["creds"]["username"],
      "password": mock_fwupdate_request["creds"]["password"],
      "manufacturer": mock_fwupdate_request["args"]["manufacturer"],
      "model": mock_fwupdate_request["args"]["model"],
      "update_type": mock_fwupdate_request["args"]["update_type"],
      "update_name": mock_fwupdate_request["args"]["update_name"],
      "image_name": mock_fwupdate_request["args"]["image_name"],
      "bash_script": mock_fwupdate_request["args"]["bash_script"]
    }
  )

@patch('ssh_commands.requests.post')
def test_fwupdate_schema_validation_error(mock_requests_post):
  # make request have bad data
  mock_fwupdate_request["args"]["manufacturer"] = 123

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com'

  # call function
  resp = ssh_commands.fwupdate(mock_fwupdate_request)

  # assert that the response is correct
  expected_response = ("The provided args does not match required values: {'manufacturer': ['Not a valid string.']}", 400)
  assert resp == expected_response

  # assert that requests.post was not called
  mock_requests_post.assert_not_called()

  # return request to original state
  mock_fwupdate_request["args"]["manufacturer"] = "Commsignia"

@patch('ssh_commands.requests.post')
def test_fwupdate_exception(mock_requests_post):
  # mock requests.post
  mock_requests_post.side_effect = Exception('Test Exception')

  # mock environment variables
  os.environ['RSU_REST_ENDPOINT'] = 'myendpoint.com'

  # call function
  resp = ssh_commands.fwupdate(mock_fwupdate_request)

  # assert that the response is correct
  expected_response = ("Encountered an error with the command fwupdate", 500)
  assert resp == expected_response