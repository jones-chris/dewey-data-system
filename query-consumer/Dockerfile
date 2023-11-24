#FROM openjdk:11
FROM openjdk:11-slim

# Expose arguments to `docker build` so that the environment variables with the same names can be set dynamically.
ARG project_version

# Check that required arguments have values.
RUN if [ -z $project_version ]; then echo "project_version required"; exit 1; fi

ENV project_version=${project_version}
ENV qb_config=${qb_config}

# Create a directory for the API jar and copy the jar into the directory.
RUN mkdir /dewey-data-system
RUN mkdir /dewey-data-system/config
WORKDIR /dewey-data-system
COPY /query-consumer/target/query-consumer-${project_version}.jar .

# The port you can debug the JVM on.
# todo:  turn this off for prod.
EXPOSE 5005
EXPOSE 8080

# Execute the jar with the qb.yml contents passed into the execution as a named argument.
# todo:  remove the JVM args for port 5005 for prod.
CMD java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -DqbConfig="$qb_config" -jar /dewey-data-system/query-consumer-${project_version}.jar