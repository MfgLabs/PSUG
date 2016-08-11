/**
* Initial DDL
* TODO: Add comments
*/

CREATE TYPE vector AS (
  cushioning    DOUBLE PRECISION,
  breathability DOUBLE PRECISION,
  lightness     DOUBLE PRECISION,
  stability     DOUBLE PRECISION,
  anti_debris   DOUBLE PRECISION
);

CREATE DOMAIN height AS TEXT CHECK (VALUE IN ('Low', 'Mid'));
CREATE DOMAIN terrain AS TEXT CHECK (VALUE IN ('Road', 'Easytrail', 'Technicaltrail'));
CREATE DOMAIN shoe_terrain AS TEXT CHECK (VALUE IN ('Road', 'Trail'));
CREATE DOMAIN surface AS TEXT CHECK (VALUE IN ('SG', 'HG', 'Road'));
CREATE DOMAIN user_role AS TEXT CHECK (VALUE IN ('Simple', 'Admin'));
CREATE DOMAIN waterproof AS TEXT CHECK (VALUE IN ('Waterproof', 'NotWaterproof'));
CREATE DOMAIN fit AS TEXT CHECK (VALUE IN ('Tight', 'Medium', 'Loose'));
CREATE DOMAIN gender AS TEXT CHECK (VALUE IN ('M', 'F'));
CREATE DOMAIN rfs AS SMALLINT CHECK (VALUE >= 0 AND VALUE <= 100);

CREATE SCHEMA components;

CREATE TABLE components.color (
  id         SERIAL    PRIMARY KEY,
  name       TEXT      NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE components.base (
  id         SERIAL PRIMARY KEY,
  name       TEXT      NOT NULL,
  is_active  BOOLEAN   NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE components.vector (
  id           SERIAL,
  vec          vector    NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT now(),
  component_id INTEGER   NOT NULL REFERENCES components.base (id)
);

CREATE TABLE components.twinskin (
  id                 INTEGER UNIQUE NOT NULL REFERENCES components.base,
  volume             INTEGER        NOT NULL,
  height             height         NOT NULL
);

CREATE TABLE components.skeleton (
  id       INTEGER UNIQUE NOT NULL REFERENCES components.base,
  material TEXT           NOT NULL,
  cut      shoe_terrain   NOT NULL
);

CREATE TABLE components.webbing (
  id INTEGER UNIQUE NOT NULL REFERENCES components.base
);

CREATE TABLE components.tpu (
  id INTEGER UNIQUE NOT NULL REFERENCES components.base
);

CREATE TABLE components.lace (
  id INTEGER UNIQUE NOT NULL REFERENCES components.base
);

CREATE TABLE components.sockliner (
  id INTEGER UNIQUE NOT NULL REFERENCES components.base
);

CREATE TABLE components.midsole (
  id       INTEGER UNIQUE NOT NULL REFERENCES components.base,
  surface  surface,
  drop     INT            NOT NULL,
  material TEXT           NOT NULL
);

CREATE TABLE components.midsole_insert (
  id       INTEGER UNIQUE NOT NULL REFERENCES components.base,
  drop     INT            NOT NULL,
  material TEXT           NOT NULL,
  heel     INTEGER        NOT NULL,
  forefoot INTEGER
);

CREATE TABLE components.outsole (
  id      SERIAL PRIMARY KEY,
  terrain shoe_terrain NOT NULL,
  drop    INT,
  surface surface      NOT NULL
);

CREATE SCHEMA se; -- Store edition specific

CREATE TABLE se.draft(
  id                UUID PRIMARY KEY,
  rep_id            UUID NOT NULL,
  name              VARCHAR(128) NOT NULL,
  ship_to           UUID,
  twinskin_id       INTEGER REFERENCES components.twinskin (id),
  skeleton_id       INTEGER REFERENCES components.skeleton (id),
  webbing_id        INTEGER REFERENCES components.webbing (id),
  tpu_id            INTEGER REFERENCES components.tpu (id),
  lace_id           INTEGER REFERENCES components.lace (id),
  sockliner_id      INTEGER REFERENCES components.sockliner (id),
  midsole_id        INTEGER REFERENCES components.midsole (id),
  midsole_insert_id INTEGER REFERENCES components.midsole_insert (id),
  outsole_id        INTEGER REFERENCES components.outsole (id),
  created_at        TIMESTAMP NOT NULL DEFAULT now(),
  updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE se.draft_colors(
  cid               UUID UNIQUE NOT NULL REFERENCES se.draft(id),
  twinskin          INTEGER REFERENCES components.color(id),
  skeleton          INTEGER REFERENCES components.color(id),
  midsole           INTEGER REFERENCES components.color(id),
  tpu_eyestay       INTEGER REFERENCES components.color(id),
  tpu_deco          INTEGER REFERENCES components.color(id),
  webbing           INTEGER REFERENCES components.color(id),
  lace              INTEGER REFERENCES components.color(id),
  toe_cap           INTEGER REFERENCES components.color(id),
  created_at        TIMESTAMP NOT NULL DEFAULT now(),
  updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE se.config(
  id                UUID PRIMARY KEY,
  rep_id            UUID NOT NULL,
  name              VARCHAR(128) NOT NULL,
  ship_to           UUID NOT NULL,
  twinskin_id       INTEGER NOT NULL REFERENCES components.twinskin (id),
  skeleton_id       INTEGER NOT NULL REFERENCES components.skeleton (id),
  webbing_id        INTEGER NOT NULL REFERENCES components.webbing (id),
  tpu_id            INTEGER NOT NULL REFERENCES components.tpu (id),
  lace_id           INTEGER NOT NULL REFERENCES components.lace (id),
  sockliner_id      INTEGER NOT NULL REFERENCES components.sockliner (id),
  midsole_id        INTEGER NOT NULL REFERENCES components.midsole (id),
  midsole_insert_id INTEGER NOT NULL REFERENCES components.midsole_insert (id),
  outsole_id        INTEGER NOT NULL REFERENCES components.outsole (id),
  active            BOOLEAN NOT NULL DEFAULT TRUE, -- Configs are never deleted, just deactivated
  created_at        TIMESTAMP NOT NULL DEFAULT now(),
  updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE se.config_colors(
  cid               UUID UNIQUE NOT NULL REFERENCES se.draft(id),
  twinskin          INTEGER NOT NULL REFERENCES components.color(id),
  skeleton          INTEGER NOT NULL REFERENCES components.color(id),
  midsole           INTEGER NOT NULL REFERENCES components.color(id),
  tpu_eyestay       INTEGER NOT NULL REFERENCES components.color(id),
  tpu_deco          INTEGER NOT NULL REFERENCES components.color(id),
  webbing           INTEGER NOT NULL REFERENCES components.color(id),
  lace              INTEGER NOT NULL REFERENCES components.color(id),
  toe_cap           INTEGER REFERENCES components.color(id),
  created_at        TIMESTAMP NOT NULL DEFAULT now(),
  updated_at        TIMESTAMP NOT NULL DEFAULT now()
);