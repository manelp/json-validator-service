CREATE TABLE "json_schemas"
(
  "schema_id" TEXT PRIMARY KEY,
  "json_schema" JSON NOT NULL
)
WITH (
  OIDS=FALSE
);
