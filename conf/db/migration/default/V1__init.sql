/**
* Initial DDL
* TODO: Add comments
*/


create schema strava;

create table activities (
  id               INTEGER  NOT NULL PRIMARY KEY,
  strat_date       TIMESTAMP NOT NULL,
  created_at       TIMESTAMP NOT NULL DEFAULT now(),
  updated_at       TIMESTAMP NOT NULL DEFAULT now()
);