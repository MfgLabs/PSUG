/**
* Initial DDL
* TODO: Add comments
*/


create schema strava;

create table strava.activities (
  id               BIGINT    NOT NULL PRIMARY KEY,
  athlete          BIGINT    NOT NULL,
  distance         REAL      NOT NULL,
  start_date       TIMESTAMP NOT NULL,
  created_at       TIMESTAMP NOT NULL DEFAULT now(),
  updated_at       TIMESTAMP NOT NULL DEFAULT now()
);