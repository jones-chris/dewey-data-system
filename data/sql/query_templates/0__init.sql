CREATE SCHEMA dewey;

CREATE TABLE dewey.query_templates(
   id SERIAL PRIMARY KEY,

   name VARCHAR(50) NOT NULL,
   version INTEGER NOT NULL,

   discoverable BOOLEAN NOT NULL DEFAULT FALSE,

   database_name VARCHAR(50) NOT NULL,

   parameterized_sql TEXT NOT NULL,

   parameters JSON,

   created_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   created_by VARCHAR(50) NOT NULL,
   last_updated_ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
   last_updated_by VARCHAR(50) NOT NULL,

   UNIQUE (name, version)
);

CREATE TABLE dewey.query_template_dependency(
    query_id INTEGER NOT NULL,
    dependency_id INTEGER NOT NULL,

    created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    last_updated_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(50) NOT NULL,

    PRIMARY KEY (query_id, dependency_id),
    FOREIGN KEY (query_id) REFERENCES dewey.query_templates(id),
    FOREIGN KEY (dependency_id) REFERENCES dewey.query_templates(id)
--    UNIQUE (query_id, dependency_id)
);

CREATE TABLE dewey.query_executions(
    id SERIAL PRIMARY KEY,

    query_id INTEGER,

    sql_string TEXT NOT NULL,

    query_execution_start TIMESTAMP NOT NULL,
    query_execution_end TIMESTAMP NOT NULL,
    query_execution_diff INTEGER NOT NULL,

    created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,

    FOREIGN KEY (query_id) REFERENCES dewey.query_templates(id)
);

CREATE TABLE dewey.query_template_metadata(
    id SERIAL PRIMARY KEY,

    query_id INTEGER NOT NULL,
    tag VARCHAR(20) NOT NULL,

    FOREIGN KEY (query_id) REFERENCES dewey.query_templates(id)
);

CREATE VIEW dewey.avg_query_template_execution_times AS
    SELECT query_id,
           COUNT(*),
           percentile_cont(0.25) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_25,
           percentile_cont(0.50) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_50,
           percentile_cont(0.75) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_75,
           percentile_cont(0.95) within GROUP (ORDER BY query_execution_diff ASC) AS percentile_95
    FROM dewey.query_executions
    WHERE query_id IS NOT NULL
      AND created_ts >= CURRENT_TIMESTAMP - INTERVAL '6 months'
    GROUP BY query_id;
;
