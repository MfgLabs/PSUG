# Scala @ MFG Labs

This document is a quick start for PSUG Demo Application.

This application was written for Paris Scala User Group [#65](http://www.meetup.com/Paris-Scala-User-Group-PSUG/events/233348116/).
Presentation slides are available on [Google Docs](https://docs.google.com/presentation/d/16HA6wN_GtHPFLrICqF4X3XFcWVGw6BxIZ24J3Lu84yY/edit?usp=sharing).

## Requirements

This project requires Java 8 and SBT.

You will also need a running local PostgreSQL 9.5 instance.
Follow the instructions bellow to setup the required databases.

First, create `mfg` user:

```sql
CREATE USER mfg WITH PASSWORD 'mfg';
```

Then, create `psugdemo` database and grant privileges to `mfg` user:

```sql
CREATE DATABASE psugdemo;
GRANT ALL PRIVILEGES ON DATABASE psugdemo TO mfg;
```

Tests require a separate DB. Create `psugdemo_test` database and grant privileges to `mfg` user:

```sql
CREATE DATABASE psugdemo_test;
GRANT ALL PRIVILEGES ON DATABASE psugdemo_test TO mfg;
```

## Endpoints Reference

API documentation uses [API Blueprint](https://apiblueprint.org) description language and is available on [apiary.io](http://docs.psug.apiary.io/#).

## Configuration Files

### Play

All commons parameters are defined in `conf/_base.conf`.

All secrets parameters (like Strava secret) are defined in `conf/_private.conf`. This file should not be available in this repo.

Available configurations are:
* `application.conf` (default configuration),
* `application.test.conf` (test environment).

These configurations include `_base.conf` and `_private.conf` files.

### Logback

Logback configuration is defined in `conf/logback.xml` file. A JSON encoder is used for stdout and file appenders.

## Running the Application

### Running the Tests

```
➭ sbt
[psug-demo] $ test
```

### Running Locally

```
➭ sbt
[psug-demo] $ run
```

#### Tip: Using the Application in the Console

```
➭ sbt
[psug-demo] $ console
scala> val app = com.mfglabs.DemoComponents.default
app: com.mfglabs.DemoComponents = com.mfglabs.DemoComponents@62f81ea4

scala> implicit val ctx = app.ctx.dbCtx
ctx: com.mfglabs.commons.Contexts.DBExeCtx = DBExeCtx(Dispatcher[contexts.db-context])

scala> import com.mfglabs.models.db.Await._
import com.mfglabs.models.db.Await._

scala> await(app.strava.activities)
res2: List[(com.mfglabs.models.Strava.Activity.Id, com.mfglabs.models.Strava.Activity)] =...

scala> app.applicationLifecycle.stop
res3: scala.concurrent.Future[Any] = Success(())
```

## Continuous Integration

The demo process is very simple. Each Drone build runs the tests with code coverage and publish the report to [coveralls.io](https://coveralls.io/github/MfgLabs/PSUG).

A more common flow should be:
- Run the tests with code coverage and publish the report.
- Build a ZIP file containing all JAR needed to run the application.
- Upload the archive on a storage service for a later deployment.

### Deploying in *development* environment

The application is automatically deployed when code is pushed on the `develop` branch if build is successful.

### Deploying in *staging* environment

To deploy in staging environment, merge `develop` branch into `staging`, push and wait for Drone build success. The application is automatically deployed on all nodes and Flyway migrations are performed silently.

### Deploying in *production* environment

Just merge the `staging` branch in `master` and push. The CI server will build, publish and tag the new version. No automatic deployment is performed.

The CI script will also check that the latest staging version is published and is currently deployed in staging environment before releasing.

## Miscellaneous

### Formating the code

In the `sbt` console, use the `scalariformFormat` command. This command is automatically ran at every compilation.

