#!/bin/sh

# WIP
cd ../app-config-app
bundle install
./perforce_daemon.sh &
./rackup &
cd ../app-config-java-poller
mvn clean install
cd integration-tests
mvn test
