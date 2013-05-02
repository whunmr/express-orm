drop table if exists `users`;
CREATE TABLE `users` (`id` int(11) unsigned NOT NULL AUTO_INCREMENT, `email` VARCHAR(320) unique NOT NULL, PRIMARY KEY (`id`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;

