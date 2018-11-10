-- MySQL dump 10.13  Distrib 8.0.11, for macos10.13 (x86_64)
--
-- Host: 127.0.0.1    Database: testDB
-- ------------------------------------------------------
-- Server version	8.0.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8mb4 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Passenger`
--

DROP TABLE IF EXISTS `Passenger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `Passenger` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(1024) DEFAULT NULL,
  `age` int(11) DEFAULT '18',
  `sex` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Passenger_id_uindex` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Passenger`
--

LOCK TABLES `Passenger` WRITE;
/*!40000 ALTER TABLE `Passenger` DISABLE KEYS */;
INSERT INTO `Passenger` (`id`, `name`, `age`, `sex`) VALUES (1,'towhid',37,'male'),(2,'sohana',28,'female'),(4,'tanvir',31,'male'),(5,'Tanvirul Islam',26,'male'),(6,'tanvir Islam',32,'male'),(75,'Akib Hassan',31,'male'),(76,'Sohana 2',29,'female'),(85,'Sohana Islam Khan',28,'female'),(87,'Jahab Ali',28,'male'),(88,'tanvir Islam',29,'male'),(91,'Tusin',15,NULL),(92,'Tanvir',18,NULL);
/*!40000 ALTER TABLE `Passenger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Person`
--

DROP TABLE IF EXISTS `Person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `Person` (
  `uuid` varchar(512) NOT NULL,
  `name` varchar(512) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `salary` double DEFAULT NULL,
  `dob` datetime DEFAULT NULL,
  `height` float DEFAULT NULL,
  `createDate` timestamp NULL DEFAULT NULL,
  `dobDate` date DEFAULT NULL,
  `createTime` time DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Person`
--

LOCK TABLES `Person` WRITE;
/*!40000 ALTER TABLE `Person` DISABLE KEYS */;
INSERT INTO `Person` (`uuid`, `name`, `age`, `active`, `salary`, `dob`, `height`, `createDate`, `dobDate`, `createTime`) VALUES ('40accccf-996c-4530-aea0-8c13b364f92c','Sohana',28,0,200,'2018-11-07 00:00:00',NULL,'2010-06-21 15:01:01','2010-06-21','21:01:01'),('482a8497-a5a6-4320-a9fd-b1287b0122d1#2','sohana',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL),('71f7874e-e6af-4e8c-b443-69d4fa4ff3bf','Towhid',34,0,200,'2018-11-10 00:00:00',NULL,'2010-06-21 15:01:01','2010-06-21','21:01:01'),('809f6133-9fcd-4598-bb79-1c2289a227d6','Tusin',18,0,200,'2018-11-07 00:00:00',NULL,'2010-06-21 15:01:01','2010-06-21','21:01:01');
/*!40000 ALTER TABLE `Person` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-11-10 11:13:48
