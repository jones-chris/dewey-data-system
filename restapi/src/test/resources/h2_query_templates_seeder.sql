CREATE SCHEMA qb4j;

CREATE TABLE qb4j.query_templates(
   id SERIAL PRIMARY KEY,

   name VARCHAR(50) NOT NULL,
   version INTEGER NOT NULL,

   query_json TEXT NOT NULL,  -- todo:  consider changing this to JSON or JSONB.  Initial dev work for this didn't work :(.

   discoverable BOOLEAN NOT NULL DEFAULT FALSE,

   database_name VARCHAR(50) NOT NULL,

   -- todo:  Consider adding these back as helpful performance data for DBAs and users considering using a query.
--   number_of_executions INTEGER NOT NULL DEFAULT 0,
--   avg_execution_time DECIMAL NOT NULL DEFAULT 0,

   -- todo:  Consider adding this field to hold the first 5-10 rows of the query to display in the UI as sample data.
   -- todo:  Consider changing from TEXT to JSON or JSONB.
--   sample_data TEXT

   created_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   created_by VARCHAR(50) NOT NULL,
   last_updated_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   last_updated_by VARCHAR(50) NOT NULL,

   UNIQUE (name, version)
);

INSERT INTO qb4j.query_templates (name, version, query_json, discoverable, created_by, last_updated_by, database_name)
VALUES ('queryTemplate0', 0, '{}', true, current_timestamp(), current_timestamp(), 'database1');

INSERT INTO qb4j.query_templates (name, version, query_json, discoverable, created_by, last_updated_by, database_name)
VALUES ('queryTemplate0', 1, '{}', true, current_timestamp(), current_timestamp(), 'database1');

INSERT INTO qb4j.query_templates (name, version, query_json, discoverable, created_by, last_updated_by, database_name)
VALUES ('queryTemplate1', 0, '{}', true, current_timestamp(), current_timestamp(), 'database2');

INSERT INTO qb4j.query_templates (name, version, query_json, discoverable, created_by, last_updated_by, database_name)
VALUES ('queryTemplate1', 1, '{}', true, current_timestamp(), current_timestamp(), 'database2');