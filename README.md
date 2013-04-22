# app-config-java-poller

The core module provides a java polling library which can be embedded in a java application which uses
Logan Mcgrath's <https://github.com/lmcgrath/app-config-app>.
See <http://loganmcgrath.com/blog/2012/11/16/scm-backed-application-configuration-with-perforce/>
for more background information.

The example-piano-webapp module shows an example of how the java poller can be used to detect
configuration changes made in the app-config-app.

## Build

```
$ mvn install
```

## Running the example piano webapp

```
First, run Logan Mcgrath's <https://github.com/lmcgrath/app-config-app>, then:
$ cd example-piano-webapp
$ mvn jetty:run
```

Then open <http://localhost:8080/> to start playing with the piano. Make instrument changes in the app-config-app
and observe that the piano switches to the new instrument.
