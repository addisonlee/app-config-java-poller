#!/bin/sh

# WIP
cd ../app-config-app
rvm 1.9.3
bundle install
./perforce_daemon.sh &
./rackup &
cd ../app-config-java-poller
mvn clean install
cd integration-tests # modify integration-tests/pom.xml to run unit tests located at src/main/java.
mvn test
