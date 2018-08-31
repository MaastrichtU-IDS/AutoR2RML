[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

# AutoRML
AutoRML generates automated R2RML mapping files for the following file formats:

* Comma-separated (.csv)
* Tab-separated (.tsv)
* Pipe-separated (.psv)
* SQLite (.sqlite and .db) 
* Postgres
* MySQL

The RDBMS metadata are retrieved using JDBC to build the mapping file. The text file contents are queries through JDBC using [Apache Drill](https://drill.apache.org). It uses the first row of each file as header. The mapping file should work out of the box and represent generic rdf representations with a unique id representing the filepath and row-number within the file.

## Build
```shell
docker build -t autorml .
```
## Run

### Docker

#### Using Apache Drill

```shell
# Mappings to System.out
docker run -it --rm --link drill:drill autorml -j "jdbc:drill:drillbit=drill:31010" -r /data/pharmgkb_drugs

# Mappings to a file
docker run -it --rm --link drill:drill -v /data:/data autorml -j "jdbc:drill:drillbit=drill:31010" -o /data/pharmgkb_drugs/mapping.ttl -r /data/pharmgkb_drugs
```

#### Using RDBMS

```shell
# Postgres
docker run -it --rm --link postgres:postgres autorml -j "jdbc:postgresql://postgres:5432/drugcentral" -u postgres -p pwd
```



### Jdbc URL

```shell
# For Apache Drill
jdbc:drill:drillbit=localhost:31010

# For Postgres
jdbc:postgresql://localhost:5432/database
```

### Options

```shell
autodrill [-?r] -j=<jdbcurl> [-o=<outputFilepath>] [-p=<password>]
                 [-u=<username>] DIRECTORY
      DIRECTORY             Base directory to scan for structured files. Needs to be
                            under the dir scanned by Apache Drill running (/data
                            by default)
  -?, --help                display a help message
  -j, --jdbcurl=<jdbcurl>   Connect to drill host
  -o, --outputfile=<outputFilepath>
                            Path to the file where the mappings will be stored. If
                            empty, then mappings go to System.out
  -u, --username=<username> Username for login if not empty
  -p, --password=<password> Password for Username
  -r, --recursive           process subDirectories recursively
```
### IDE run config

```shell
# Main class
nl.unimaas.ids.autorml.AutoRML

# Program arguments for Drill
-j "jdbc:drill:drillbit=localhost:31010" -o /data/pharmgkb_drugs/mapping.ttl -r /data/pharmgkb_drugs
```