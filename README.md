# SQL to create tables

CREATE TABLE `users` (
  `username` varchar(16) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `gogame` (
  `username` varchar(16) NOT NULL,
  `total_score` int(11) DEFAULT '0',
  `wins` int(11) DEFAULT '0',
  `losses` int(11) DEFAULT '0',
  PRIMARY KEY (`username`),
  CONSTRAINT `fk_username` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
