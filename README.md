[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

# AutoR2RML
AutoR2RML automatically generates R2RML mapping files for the following inputs:

* Comma-separated files (.csv)
* Tab-separated files (.tsv)
* Pipe-separated files (.psv)
* SQLite files (.sqlite and .db) 
* Postgres database connection
* MySQL database connection

The RDBMS metadata are retrieved using JDBC to build the mapping file. The text file contents are queries through JDBC using [Apache Drill](https://drill.apache.org). It uses the first row of each file as header. The mapping file should work out of the box and represent generic rdf representations with a unique id representing the filepath and row-number within the file. 

Please note that for Apache Drill empty string values are treated as NULL and every cell value are trimmed.

## Build
```shell
docker build -t autor2rml .
```
## Run

### Docker

#### Using Apache Drill for TSV files

```shell
# Mappings to System.out
docker run -it --rm --link drill:drill autor2rml -j "jdbc:drill:drillbit=drill:31010" -d /data/data2services -r

# Mappings to a file
docker run -it --rm --link drill:drill -v /data:/data autor2rml -j "jdbc:drill:drillbit=drill:31010" -o /data/data2services/mapping.ttl -d /data/data2services -b http://data2services/ -g http://data2services/graph/autor2rml -r
```

#### Using RDBMS

```shell
## Postgres (run docker)
# Run and load Postgres DB
docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=pwd -d -v /data/data2services/:/data postgres
docker exec -it postgres bash
su postgres
psql drugcentral < /data/drugcentral.dump.08262018.sql

# Run autor2rml on DB
docker run -it --rm --link postgres:postgres -v /data:/data autor2rml -j "jdbc:postgresql://postgres:5432/drugcentral" -u postgres -p pwd -o /data/data2services/mapping.ttl

## SQLite
docker run -it --rm -v /data:/data autor2rml -j "jdbc:sqlite:/data/sqlite/chinook.db" -o /data/data2services/mapping.ttl

```

### Jdbc URL

```shell
# For Apache Drill
jdbc:drill:drillbit=localhost:31010

# For Postgres
jdbc:postgresql://localhost:5432/database

# For SQLite
jdbc:sqlite:/data/data2services/GEOmetadb.sqlite
```

### Options

```shell
docker run --rm -it autor2rml -?
```
### IDE run config

```shell
# Main class
nl.unimaas.ids.autor2rml.autor2rml

# Program arguments for Drill
-j "jdbc:drill:drillbit=localhost:31010" -o /data/data2services/mapping.ttl -d /data/data2services -r
```



## Run tests

Work in progress.

- Files in `/src/test/resources/` needs to be in `/data/data2services-tests` at the moment to query it with Apache Drill
- Apache Drill needs to be running on `/data` 

```shell
docker run -dit --rm -p 8047:8047 -p 31010:31010 --name drill -v /data:/data:ro apache-drill

mvn test
```

Trying to use [docker-compose-rule](https://github.com/palantir/docker-compose-rule) to start the docker container at the start of the tests.