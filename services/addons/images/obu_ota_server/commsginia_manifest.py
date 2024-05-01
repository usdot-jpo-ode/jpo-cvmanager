import os
import datetime
import re
from typing import List
import os
import copy

document = {
    'content': [],
    'pageable': {
        'sort': {
            'empty': False,
            'unsorted': False,
            'sorted': True
        },
        'offset': 0,
        'pageNumber': 0,
        'pageSize': 32,
        'paged': True,
        'unpaged': False
    },
    'last': True,
    'totalPages': 1,
    'totalElements': 0,
    'size': 32,
    'number': 0,
    'sort': {
        'empty': False,
        'unsorted': False,
        'sorted': True
    },
    'numberOfElements': 0,
    'first': True,
    'empty': False
}


magic_regex=re.compile('(?P<variant>(?P<type>ob4|rs4)-generic(?:-.*)?)-(?:(?P<writable>rw)|ro)(?P<secure>-secureboot)?-(?P<release>.*).tar.sig')

def add_contents(server: str, firmware_list: List):
    manifest = copy.deepcopy(document)
    
    for firmware in firmware_list:

        fname = firmware.split("/")[-1]
        info = magic_regex.match(fname)
        if not info:
            continue

        content = {}

        content['id'] = ""
        content['name'] = fname
        content['variant'] = info['variant']
        content['releaseVersion'] = info['release']
        content['type'] = "RSU" if info['type'] == "rs4" else "OBU"
        content['writableSystemPartition'] = bool(info['writable'])
        content['secure'] = bool(info['secure'])
        content['uploadedAt'] = datetime.datetime.fromtimestamp(os.path.getmtime(firmware)).strftime('%Y-%m-%dT%H:%M:%SZ')
        content['size'] = os.stat(firmware).st_size
        content['links'] = [{'rel': 'local-file',
                             'href': server + '/firmwares/' + fname,
                             'type': 'application/octet-stream'}]
        manifest['content'].append(content)
        manifest['totalElements'] += 1
        manifest['numberOfElements'] += 1

        if manifest['size'] <= manifest['totalElements']:
            raise AttributeError("Maximum number of firmwares is:",
                                 manifest['size'])
    return manifest