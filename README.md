# JSON Validator Service #

This service implement an API to upload JSON Schemas and validate JSON documents againts the schema.

Endpoints

``` 
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
``` 

## License ##

This code is licensed under the MIT License, see the
[LICENSE](LICENSE) file for details.

## System requirements ##

- Java 11
- PostgreSQL 11 (or higher)

## Developer guide ##

For development and testing you need to install [sbt](http://www.scala-sbt.org/).
Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details how to to contribute
to the project.

During development you can start (and restart) the application via the `reStart`
sbt task provided by the sbt-revolver plugin.

### Tests ###

Tests are included in the project. You can run them via the appropriate sbt tasks
`test` and `IntegrationTest/test`. The latter will execute the integration tests.
Be aware that the integration tests might need a working database.

## Running local guide ##

Start the local postgres with:
```
docker-compose up
```

Then run the application with
```
sbt run
```

The service will start listening on port 8081 by default.

Useful commands:

* To upload a schema
```
curl http://localhost:8081/schema/config-schema -X POST -d @./examples/config-schema.json
```

* To download a schema
```
curl http://localhost:8081/schema/config-schema -X GET
```

* To test validation successful
```
curl http://localhost:8081/validate/config-schema -X POST -d @./examples/valid-document.json
```


* To test validation errors
```
curl http://localhost:8081/validate/config-schema -X POST -d @./examples/invalid-document.json
```

### Open API

There's also swagger running on:

http://localhost:8081/docs



