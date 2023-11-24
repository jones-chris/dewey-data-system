#!/bin/bash

export QUERY_PRODUCER_PROJECT_VERSION=$(mvn -f ./query-producer help:evaluate -Dexpression=project.version -q -DforceStdout)
export QUERY_CONSUMER_PROJECT_VERSION=$(mvn -f ./query-consumer help:evaluate -Dexpression=project.version -q -DforceStdout)
export AUTH_VERSION=$(cd ./auth && npm pkg get version | xargs echo && cd ..)
export UI_VERSION=$(cd ./ui && npm pkg get version | xargs echo && cd ..)

# Skip running unit tests, if specified.
if [ "$1" == "noTests" ]
then
  mvn clean install -Dmaven.javadoc.skip=true -DskipTests
else
  mvn clean install -Dmaven.javadoc.skip=true
fi

if [ $? != 0 ]; then exit $?; fi

# Skip building Docker images, if specified.
if [ "$2" == "noImageBuilds" ]
then
  echo "Skipping building Docker images..."
else
  sudo docker image build -t dewey-data/query-producer:"$QUERY_PRODUCER_PROJECT_VERSION" --build-arg project_version="$QUERY_PRODUCER_PROJECT_VERSION" --file ./query-producer/Dockerfile .
  sudo docker image build -t dewey-data/query-consumer:"$QUERY_CONSUMER_PROJECT_VERSION" --build-arg project_version="$QUERY_CONSUMER_PROJECT_VERSION" --file ./query-consumer/Dockerfile .
  sudo docker image build -t dewey-data/auth:"$AUTH_VERSION" --file ./auth/Dockerfile ./auth
  sudo docker image build -t dewey-data/ui:"$UI_VERSION" --file ./ui/Dockerfile ./ui
fi

if [ $? != 0 ]; then exit $?; fi

export UPDATE_CACHE=false

sudo -E docker-compose -f ./examples/deployments/docker-compose.yml up && 
  sudo -E docker-compose -f ./examples/deployments/docker-compose.yml down
