CREATE TABLE `authentication`.`credentials`
(
  `uid`      INT      NOT NULL,
  `password` CHAR(60) NOT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE INDEX `idx_credentials_uid_UNIQUE` (`uid` ASC) VISIBLE
);
