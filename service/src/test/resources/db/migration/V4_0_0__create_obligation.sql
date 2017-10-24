CREATE TABLE obligation (
   `id` int(18) NOT NULL AUTO_INCREMENT,
   `obligation_id`   varchar(128) NOT NULL,
   `obligation_json` MEDIUMTEXT NOT NULL,
   `authorization_zone_id` int(18) DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

ALTER TABLE resource ADD FOREIGN KEY (authorization_zone_id) REFERENCES authorization_zone(id) ON DELETE CASCADE;


