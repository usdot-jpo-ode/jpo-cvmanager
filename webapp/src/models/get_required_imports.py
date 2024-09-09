import re
import os
import glob
import json

matching_expr = r"    private ([a-zA-Z0-9_]*?) ([a-zA-Z0-9_]*)"

KNOWN_TYPES = [
    '(^String)',

    '(^int)',
    '(^Integer)',
    '(^Double)',
    '(^double)',
    '(^long)',
    '(^Long)',

    '(^Boolean)',
    '(^bool)',

    '^ArrayList<(.*?)>',
    '^List<(.*?)>',

    '(^LineString)',
    '(^ZonedDateTime)',

    '(Set<(.*?)>)',
]

KNOWN_ORGS = {
    "us.dot.its.jpo.ode.plugin": "https://github.com/usdot-jpo-ode/jpo-ode/tree/dev/jpo-ode-plugins/src/main/java/us/dot/its/jpo/ode/plugin",
    "us.dot.its.jpo.ode.model": "https://github.com/usdot-jpo-ode/jpo-ode/tree/dev/jpo-ode-core/src/main/java/us/dot/its/jpo/ode/model",
    "org.locationtech.jts": "https://github.com/locationtech/jts/tree/master/modules/core/src/main/java/org/locationtech/jts",
    "us.dot.its.jpo.geojsonconverter": "https://github.com/usdot-jpo-ode/jpo-geojsonconverter/tree/develop/jpo-geojsonconverter/src/main/java/us/dot/its/jpo/geojsonconverter",
    "us.dot.its.jpo.conflictmonitor": "https://github.com/usdot-jpo-ode/jpo-conflictmonitor/tree/dev/jpo-conflictmonitor/src/main/java/us/dot/its/jpo/conflictmonitor",
}

TEMPLATE = """type {class_name} = {{
{contents}
}};"""

files = [os.path.join(dp, f) for dp, dn, filenames in os.walk('./java/')
         for f in filenames if os.path.splitext(f)[1] == '.java']
print(files)

types = {}
for file_path in files:

    file_path = '.'.join(file_path.replace('\\', '/').split('.')[:-1])
    JAVA_PATH = '/'.join(file_path.split('/')[:-1])
    TS_PATH = JAVA_PATH.replace('java', 'ts')
    CLASS_NAME = file_path.split('/')[-1]
    # print(file_path, JAVA_PATH, TS_PATH, CLASS_NAME)

    file_contents = open(f"{JAVA_PATH}/{CLASS_NAME}.java", 'r').read()

    matches = re.findall(matching_expr, file_contents)
    # print(matches)
    for var in matches:
        java_type = var[0]
        found = False
        for known_type in KNOWN_TYPES:
            if re.match(known_type, java_type):
                found = True
                break
        if not found:
            local_files = [i.replace('\\', '/').split('/')[-1].split('.')[0]
                           for i in glob.glob(f"{JAVA_PATH}/*")]
            if java_type in local_files:
                import_path = file_path[2:]
                types[java_type] = f"./{import_path}/{java_type}.java"
            else:
                import_path = re.search(
                    f"import (.*?\\.{java_type});", file_contents).group(1)
                found_org = False
                for org, url in KNOWN_ORGS.items():
                    print(org, java_type)
                    if org in import_path:
                        found_org = True
                        path = f"{url}{import_path.replace(org, '').replace('.', '/')}.java"
                        types[java_type] = path
                        break
                if not found_org:
                    raise Exception(
                        f"Could not find organization for java type {java_type} in {file_path}")

with open(f"./required_types.json", 'w') as f:
    f.write(json.dumps(types, indent=2))

# ts_contents = TEMPLATE.format(class_name=CLASS_NAME, contents='\n'.join(lines))

# if not os.path.exists(f"{TS_PATH}/{CLASS_NAME}.ts"):
#     os.makedirs(f"{TS_PATH}/")
# with open(f"{TS_PATH}/{CLASS_NAME}.ts", 'w') as f:
#     f.write(ts_contents)
