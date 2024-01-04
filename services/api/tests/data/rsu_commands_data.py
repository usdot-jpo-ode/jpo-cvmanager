import multidict
from datetime import datetime

##################################### request_data ###########################################

request_environ_good = {
        'organization': 'Test',
        'role': 'user'
}

request_json_good = {
    'command':'test',
    'rsu_ip':['8.8.8.8'],
    'args': {
        'data':'test'
    }
}