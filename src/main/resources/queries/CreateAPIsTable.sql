CREATE TABLE IF NOT EXISTS `tb_apis` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(200) NOT NULL,
    `host` varchar(200) NOT NULL,
    `port` int NOT NULL,
    `healthcheck_route` varchar(200) NOT NULL,
    `status` char NOT NULL DEFAULT 'W',
    `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8
