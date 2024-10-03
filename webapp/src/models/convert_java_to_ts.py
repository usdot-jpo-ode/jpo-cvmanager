import re
import os

matching_expr = r"^    (?:[a-zA-Z@]+? )*(?:private )?(?:public )?(?:final )?(?:static )?([a-zA-Z0-9_<>]+?) ([a-zA-Z_]+).*?;"
matching_expr_extends = r"public class (.*?) extends (.*?)\{"
matching_expr_interface = r"public interface (.*?)\{"

TYPE_MAPPINGS = {
    # Simple types
    '(^String)': 'str',

    '(^int)': 'number',
    '(^Integer)': 'number',
    '(^Double)': 'number',
    '(^double)': 'number',
    '(^long)': 'number',
    '(^Long)': 'number',
    'final': 'any',

    '(^Boolean)': 'boolean',
    '(^bool)': 'boolean',

    '^ArrayList<(.*?)>': r'\1[]',
    '^List<(.*?)>': r'\1[]',

    '(^LineString)': 'number[][]',
    '(^ZonedDateTime)': 'Date',

    # '(Set<(.*?)>)': r'Set<\1>',
}

# IMPORTS_PATH = "./ts/imports"
TEMPLATE = """{imports}
type {class_name} = {extension}{{
{contents}
}}"""

# if not os.path.exists(IMPORTS_PATH):
#     os.mkdir(IMPORTS_PATH)

# INITIAL_PATH = "java/jpo-conflictmonitor/jpo-conflictmonitor/src/main/java/us/dot/its/jpo/conflictmonitor/monitor/models"
INITIAL_PATH = "../../../api/jpo-conflictmonitor/jpo-conflictmonitor/src/main/java/us/dot/its/jpo/conflictmonitor/monitor/models"
files = [os.path.join(dp, f) for dp, dn, filenames in os.walk("./" + INITIAL_PATH)
         for f in filenames if os.path.splitext(f)[1] == '.java']

DIRECTORIES_TO_IGNORE = ["test", "jpo-s3-deposit", "jpo-ode-consumer-example",
                         "jpo-ode-svcs", "jpo-sdw-depositor", "jpo-security-scvs", "asn1_codec", "jpo-geojsonconverter"]

CUSTOM_DIRECTORIES_TO_NOT_REGENERATE = ["config"]

for file_path in files:
    print(file_path)
    imports = ""

    file_path = '.'.join(file_path.replace('\\', '/').split('.')[:-1])
    JAVA_PATH = '/'.join(file_path.split('/')[:-1])
    TS_PATH = JAVA_PATH.replace(INITIAL_PATH, 'jpo-conflictmonitor/')
    CLASS_NAME = file_path.split('/')[-1]

    ignored = False
    directories = file_path.split('/')
    for ignored_dir in DIRECTORIES_TO_IGNORE:
        if ignored_dir in directories:
            ignored = True
    for ignored_dir in CUSTOM_DIRECTORIES_TO_NOT_REGENERATE:
        if ignored_dir in directories:
            ignored = True
    if ignored:
        continue

    file_contents = open(f"{JAVA_PATH}/{CLASS_NAME}.java", 'r').read()
    interface_match = re.findall(matching_expr_interface, file_contents)
    if interface_match:
        continue

    extension_match = re.findall(matching_expr_extends, file_contents)
    extension = ""
    if extension_match and extension_match[0][1]:
        if extension_match[0][1].strip() == "Notification":
            imports = '/// <reference path="Notification.d.ts" />'
            extension = f"MessageMonitor.{extension_match[0][1]} & " if extension_match else ""
        elif extension_match[0][1].strip() == "Event":
            imports = '/// <reference path="Event.d.ts" />'
            extension = f"MessageMonitor.{extension_match[0][1]} & " if extension_match else ""
        else:
            extension = f"{extension_match[0][1]} & " if extension_match else ""
            
    matches = []
    lines = file_contents.split('\n')
    for l in lines:
        match = re.search(matching_expr, l)
        if match: matches.append(match.groups())
        
    print(matches)
    lines = []
    for var in matches:
        ts_type = var[0]
        ts_name = var[1]
        for k, v in TYPE_MAPPINGS.items():
            ts_type = re.sub(k, v, ts_type)
        lines.append(f"  {ts_name}: {ts_type}")

    contents = '\n'.join(lines)
    if CLASS_NAME == "Notification":
        imports = "declare namespace MessageMonitor {"
        contents += "\n}"
    if CLASS_NAME == "Event":
        imports = "declare namespace MessageMonitor {"
        contents += "\n}"

    ts_contents = TEMPLATE.format(
        imports=imports, class_name=CLASS_NAME, extension=extension, contents=contents)

    if not os.path.exists(f"{TS_PATH}/"):
        os.makedirs(f"{TS_PATH}/")
    with open(f"{TS_PATH}/{CLASS_NAME}.d.ts", 'w') as f:
        f.write(ts_contents)
