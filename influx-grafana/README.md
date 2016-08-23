# Metrics with InfluxDb and Grafana

## Requirements

We assume that PSUG demo application is up and running.
Docker and Docker compose are also required: the following uses [Docker for Mac](https://docs.docker.com/docker-for-mac/). 

## Setup

### Docker

Start InfluxDB and Grafana containers:

```
cd influx-grafana
docker-compose up
```

### InfluxDB

Connect to <http://localhost:8083>, create `psugdemo` database and configure credentials:

```
CREATE DATABASE "psugdemo"
CREATE USER "psug" WITH PASSWORD 'psug'
```

### Grafana

Connect to <http://localhost:3000> with admin default credentials (see Grafana [documentation](http://docs.grafana.org/installation/configuration/#security)).
Then create InfluxDB datasource:

![InfluxDB Datasource Configuration](/influx-grafana/grafana_influxdb.png)

## Usage

### Application

Edit `conf/application.conf` to configure InfluxDB datasource.
Run the application and make some calls to the API (eg. `curl localhost:9000/status`).

### InfluxDB

List available measurements:

```
SHOW MEASUREMENTS
```

| name           |
| -------------- |
| response_times |


Describe `response_times` measurement:

```
SHOW TAG KEYS FROM "response_times"
```

| tagKey      |
| ----------- |
| callees     |
| category    |
| environment |
| host        |
| method      |
| version     |


Show tags values:

```
SHOW TAG VALUES FROM "response_times" WITH KEY = "callees"
```

| key     | value                                                                             |
| ------- | --------------------------------------------------------------------------------- |
| callees | "/com.mfglabs.controllers.Application.status/com.mfglabs.models.db.Database.test" |
| callees | "/com.mfglabs.controllers.Application.status"                                     |


```
SHOW TAG VALUES FROM "response_times" WITH KEY = "category"
```

| key      | value      |
| -------- | ---------- |
| category | "database" |
| category | "api"      |


### Grafana

Create a Grafana dashboard and add the following charts.

API calls:

```
SELECT "execution_time" FROM "response_times" WHERE "category" = 'api' AND $timeFilter GROUP BY "method"
```

Database calls:

```
SELECT "execution_time" FROM "response_times" WHERE "category" = 'database' AND $timeFilter GROUP BY "method"
```
