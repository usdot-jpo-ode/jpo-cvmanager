# Models

This directory contains type definitions for the types used across this webapp.

## Generated Classes

The convert_java_to_ts.py script is used for generating typescript type definition files from java classes. This script utilizes the jpo-conflictmonitor submodule in the api directory. It then searches through that directory, utilizes regular expressions to identify data classes, and pulls out the properties. Each Java class gets converted to a .d.ts file in the jpo-conflictmonitor directory.

This script is usually accurate, but can leave some errors. These are best fixed by manually going through any files that fail to compile and resolving the issue.

To re-generate the classes from the latest Java file updates, simply remove the jpo-conflictmonitor directory and run the python script:

```sh
rm -r ./jpo-conflictmonitor
python convert_java_to_ts.py
```

## get_required_imports.py

The get_required_imports.py script is used to generate a required_types.json file, which contains links to all required external dependencies. These should be manually downloaded and run through the convert_java_to_ts.py script, or manually converted to .d.ts files.

To regenerate the imports JSON file:

```sh
python get_required_imports.py
```
