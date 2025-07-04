# ************************************************************
# Sequel Ace SQL dump
# Version 20094
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# Host: avcrm.cwa46gzbqwbb.ap-southeast-2.rds.amazonaws.com (MySQL 5.7.44)
# Database: rpa_cfa
# Generation Time: 2025-07-04 03:43:26 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table dji_connect_manage_device_dictionary
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dji_connect_manage_device_dictionary`;

CREATE TABLE `dji_connect_manage_device_dictionary` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` int(11) NOT NULL COMMENT 'This parameter corresponds to the domain in the Product Type section of the document. 0: drone; 1: payload; 2: remote control; 3: dock;',
  `device_type` int(11) NOT NULL COMMENT 'This parameter corresponds to the type in the Product Type section of the document.',
  `sub_type` int(11) NOT NULL COMMENT 'This parameter corresponds to the sub_type in the Product Type section of the document.',
  `device_name` varchar(32) NOT NULL DEFAULT '' COMMENT 'This parameter corresponds to the name in the Product Type section of the document.',
  `device_desc` varchar(100) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Device product enum';

LOCK TABLES `dji_connect_manage_device_dictionary` WRITE;
/*!40000 ALTER TABLE `dji_connect_manage_device_dictionary` DISABLE KEYS */;

INSERT INTO `dji_connect_manage_device_dictionary` (`id`, `domain`, `device_type`, `sub_type`, `device_name`, `device_desc`)
VALUES
	(1,0,60,0,'Matrice 300 RTK',NULL),
	(2,0,67,0,'Matrice 30',NULL),
	(3,0,67,1,'Matrice 30T',NULL),
	(4,1,20,0,'Z30',NULL),
	(5,1,26,0,'XT2',NULL),
	(6,1,39,0,'FPV',NULL),
	(7,1,41,0,'XTS',NULL),
	(8,1,42,0,'H20',NULL),
	(9,1,43,0,'H20T',NULL),
	(10,1,50,65535,'P1','include 24 and 35 and 50mm'),
	(11,1,52,0,'M30 Camera',NULL),
	(12,1,53,0,'M30T Camera',NULL),
	(13,1,61,0,'H20N',NULL),
	(14,1,165,0,'DJI Dock Camera',NULL),
	(15,1,90742,0,'L1',NULL),
	(16,2,56,0,'DJI Smart Controller','Remote control for M300'),
	(17,2,119,0,'DJI RC Plus','Remote control for M30'),
	(18,3,1,0,'DJI Dock',''),
	(19,0,77,0,'Mavic 3E',NULL),
	(20,0,77,1,'Mavic 3T',NULL),
	(21,1,66,0,'Mavic 3E Camera',NULL),
	(22,1,67,0,'Mavic 3T Camera',NULL),
	(23,2,144,0,'DJI RC Pro','Remote control for Mavic 3E/T and Mavic 3M'),
	(24,0,77,2,'Mavic 3M',NULL),
	(25,1,68,0,'Mavic 3M Camera',NULL),
	(26,0,89,0,'Matrice 350 RTK',NULL),
	(27,3,2,0,'DJI Dock2',NULL),
	(28,0,91,0,'M3D',NULL),
	(29,0,91,1,'M3TD',NULL),
	(30,1,80,0,'M3D Camera',NULL)
	(31,1,81,0,'M3TD Camera',NULL),
	(33,0,99,0,'M4E',NULL),
	(34,0,99,1,'M4T',NULL),
	(35,1,88,0,'M4E Camera',NULL),
	(36,1,89,0,'M4T Camera ',NULL),
	(37,2,174,0,'DJI RC Plus 2','Remote control for Matrice 4 '),
	(38,0,100,0,'M4D',NULL),
	(39,0,100,1,'M4TD',NULL),
	(40,1,98,0,'M4D Camera',NULL),
	(41,1,99,0,'M4TD Camera',NULL),
	(42,3,3,0,'Dock3',NULL),
	(43,1,78,0,'H30',NULL),
	(44,1,78,1,'H30T',NULL),
	(45,0,181,0,'Matrice 400',NULL),
	(46, 1, 103, 0, 'M400 Camera', NULL);

/*!40000 ALTER TABLE `dji_connect_manage_device_dictionary` ENABLE KEYS */;
UNLOCK TABLES;


/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
