drop table if exists `users`;
CREATE TABLE `users` (`id` int(9) unsigned NOT NULL AUTO_INCREMENT, `first_name` VARCHAR(100), `email` VARCHAR(30), PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table if exists `miscs`;
CREATE TABLE `miscs` (`id` int(9) unsigned NOT NULL AUTO_INCREMENT, `bool_value` TINYINT, `char_value` CHAR(1), `byte_value` TINYINT, `short_value` SMALLINT, `integer_value` MEDIUMINT, `long_value` INT, `float_value` FLOAT, `double_value` DOUBLE, `string_value` TEXT, `primitive_int_value` MEDIUMINT, `primitive_double_value` DOUBLE, `primitive_float_value` FLOAT, `primitive_boolean_value` TINYINT, `primitive_short_value` SMALLINT, `primitive_long_value` INT, `primitive_byte_value` TINYINT, `day` ENUM ('SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY','THURSDAY', 'FRIDAY', 'SATURDAY'), PRIMARY KEY (`id`));

drop table if exists `articles`;
CREATE TABLE `articles` (`id` int (9) unsigned NOT NULL AUTO_INCREMENT, user_id INT, `title` TEXT, `content` TEXT, PRIMARY KEY (`id`));