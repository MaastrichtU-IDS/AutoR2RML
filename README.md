[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

# AutoDrill
AutoDrill generates automated RML mapping files for comma-seperated (CSV), tap-seperated (TSV), and pipe-seperated (PSV) files by querying files and their contents through JDBC using [Apache Drill](https://drill.apache.org). It uses the first row of each file as header. The mapping file should work out of the box and represent generic rdf representations with a unique id representing the filepath and row-number within the file.

## Build
```
# docker build -t autodrill .
```
## Usage
```
# docker run -it --rm autodrill -?

Usage: autodrill [-?r] -h=<host> [-p=<port>] [-pw=<passWord>]
                    [-un=<userName>] DIRECTORY
      DIRECTORY       Base directory to scan for structured files
  -?, --help          display a help message
  -h, --host=<host>   Connect to drill host
  -p, --port=<port>   Connect to drill port
                        Default: 31010
      -pw, --password=<passWord>
                      Password for login
  -r, --recursive     process subDirectories recursively
      -un, --username=<userName>
                      Username for login if not empty
```
## Run
```
# docker run -it --rm autodrill --recursive --host <host> <absolute path for drill query> | tee ~/drill_output.ttl
```
