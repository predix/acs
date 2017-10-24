CREATE TABLE obligation (
   id   bigint   NOT NULL   PRIMARY KEY,
   obligation_id   character varying(128)   NOT NULL,
   obligation_json   text   NOT NULL,
   authorization_zone_id   integer   REFERENCES authorization_zone(id)
)