# app-config-java-poller

Provides rest interfaces for polling app-config-app changes. Written in Java and built with Maven.

## Compiling

```
$ mvn install
```

## Running in Jetty

```
$ mvn jetty:run
```

Then open <http://localhost:8080/> and click on the "get md5" button,
or just go directly to <http://localhost:8080/data/poller/md5>
