CREATE SCHEMA qb4j;

CREATE TABLE qb4j.query_templates(
   id SERIAL PRIMARY KEY,

   name VARCHAR(50) NOT NULL,
   version INTEGER NOT NULL,

   query_json TEXT NOT NULL,  -- todo:  consider changing this to JSON or JSONB.  Initial dev work for this didn't work :(.

   discoverable BOOLEAN NOT NULL DEFAULT FALSE,

   database_name VARCHAR(50) NOT NULL,

   -- todo:  Consider adding this field to hold the first 5-10 rows of the query to display in the UI as sample data.
   -- todo:  Consider changing from TEXT to JSON or JSONB.
--   sample_data TEXT

    built_sql TEXT,

   created_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   created_by VARCHAR(50) NOT NULL,
   last_updated_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   last_updated_by VARCHAR(50) NOT NULL,

   UNIQUE (name, version)
);

COMMENT ON COLUMN qb4j.query_templates.built_sql
    IS 'The SQL string that was built by the application.  Can be null if the user chooses not to build the SelectStatement, such as if the SelectStatement is a work-in-progress.';

CREATE TABLE qb4j.query_template_dependency(
    query_id INTEGER NOT NULL,
    dependency_id INTEGER NOT NULL,

    created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    last_updated_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(50) NOT NULL,

    PRIMARY KEY (query_id, dependency_id),
    FOREIGN KEY (query_id) REFERENCES qb4j.query_templates(id),
    FOREIGN KEY (dependency_id) REFERENCES qb4j.query_templates(id)
--    UNIQUE (query_id, dependency_id)
);

CREATE TABLE qb4j.query_executions(
    id SERIAL PRIMARY KEY,

    query_id INTEGER,

    sql_string TEXT NOT NULL,

    query_execution_start TIMESTAMP NOT NULL,
    query_execution_end TIMESTAMP NOT NULL,
    query_execution_diff INTEGER NOT NULL,

    created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,

    FOREIGN KEY (query_id) REFERENCES qb4j.query_templates(id)
);

COMMENT ON COLUMN qb4j.query_executions.query_id
    IS 'The id of the query template.  Can be null if, for example, the user runs an anonymous, unsaved query.';
COMMENT ON COLUMN qb4j.query_executions.query_execution_diff
    IS 'The number of milliseconds the query took to run';

CREATE TABLE qb4j.query_template_metadata(
    id SERIAL PRIMARY KEY,

    query_id INTEGER NOT NULL,
    tag VARCHAR(20) NOT NULL,

    FOREIGN KEY (query_id) REFERENCES qb4j.query_templates(id)
);

CREATE VIEW qb4j.avg_query_template_execution_times AS
    SELECT query_id,
           COUNT(*),
           percentile_cont(0.25) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_25,
           percentile_cont(0.50) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_50,
           percentile_cont(0.75) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_75,
           percentile_cont(0.95) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_95
    FROM qb4j.query_executions
    WHERE query_id IS NOT NULL
      AND created_ts >= CURRENT_TIMESTAMP - INTERVAL '6 months'
    GROUP BY query_id;
;

COMMENT ON qb4j.avg_query_template_execution_times IS 'Calculates the average execution times of query templates for the last 6 months';
