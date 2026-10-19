/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.4.13-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: asociacion_juegos
-- ------------------------------------------------------
-- Server version	11.4.13-MariaDB-ubu2404

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `asociacion`
--

DROP TABLE IF EXISTS `asociacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `asociacion` (
  `id` int(11) NOT NULL,
  `razon_social` varchar(100) NOT NULL,
  `cif` varchar(20) NOT NULL,
  `direccion` varchar(150) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `fecha_apertura` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cif` (`cif`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asociacion`
--

LOCK TABLES `asociacion` WRITE;
/*!40000 ALTER TABLE `asociacion` DISABLE KEYS */;
INSERT INTO `asociacion` VALUES
(1,'Asociacion Dados y Dragones','G00000000','Calle Dato 10, Vitoria-Gasteiz','945123456','info@ejemplo.com','2024-01-15');
/*!40000 ALTER TABLE `asociacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inscripciones`
--

DROP TABLE IF EXISTS `inscripciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `inscripciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_partida` int(11) NOT NULL,
  `documento_socia` varchar(20) DEFAULT NULL,
  `nombre_invitada` varchar(100) DEFAULT NULL,
  `ganadora` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_partida` (`id_partida`,`documento_socia`),
  KEY `documento_socia` (`documento_socia`),
  CONSTRAINT `inscripciones_ibfk_1` FOREIGN KEY (`id_partida`) REFERENCES `partidas` (`id`),
  CONSTRAINT `inscripciones_ibfk_2` FOREIGN KEY (`documento_socia`) REFERENCES `socios` (`documento`),
  CONSTRAINT `CONSTRAINT_1` CHECK (`documento_socia` is not null and `nombre_invitada` is null or `documento_socia` is null and `nombre_invitada` is not null)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inscripciones`
--

LOCK TABLES `inscripciones` WRITE;
/*!40000 ALTER TABLE `inscripciones` DISABLE KEYS */;
INSERT INTO `inscripciones` VALUES
(1,1,'12345678A',NULL,0),
(2,1,NULL,'Pedro Martinez',0);
/*!40000 ALTER TABLE `inscripciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `juegos`
--

DROP TABLE IF EXISTS `juegos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `juegos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `autores` varchar(255) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `min_jugadores` int(11) NOT NULL,
  `max_jugadores` int(11) NOT NULL,
  `duracion_min` int(11) NOT NULL,
  `tematica` varchar(100) DEFAULT NULL,
  `tipo` enum('competitivo','cooperativo','mixto') NOT NULL,
  `ubicacion` varchar(100) DEFAULT NULL,
  `estado` enum('disponible','prestado','reparacion') NOT NULL DEFAULT 'disponible',
  `documento_propietaria` varchar(20) DEFAULT NULL,
  `fecha_alta` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `documento_propietaria` (`documento_propietaria`),
  CONSTRAINT `juegos_ibfk_1` FOREIGN KEY (`documento_propietaria`) REFERENCES `socios` (`documento`),
  CONSTRAINT `CONSTRAINT_1` CHECK (`min_jugadores` >= 1),
  CONSTRAINT `CONSTRAINT_2` CHECK (`max_jugadores` >= `min_jugadores`),
  CONSTRAINT `CONSTRAINT_3` CHECK (`duracion_min` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `juegos`
--

LOCK TABLES `juegos` WRITE;
/*!40000 ALTER TABLE `juegos` DISABLE KEYS */;
INSERT INTO `juegos` VALUES
(1,'Carcassonne','Klaus-Jürgen Wrede','Juego de colocacion de losetas',2,5,45,'Estrategia','competitivo','Estanteria A1','disponible','12345678A','2026-10-13'),
(3,'Catan','Klaus Teuber',NULL,3,4,90,NULL,'competitivo','Estanteria A2','disponible',NULL,'2026-10-13');
/*!40000 ALTER TABLE `juegos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `partidas`
--

DROP TABLE IF EXISTS `partidas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `partidas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_juego` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `jugadores_deseados` int(11) NOT NULL,
  `estado` enum('programada','finalizada','cancelada') NOT NULL DEFAULT 'programada',
  PRIMARY KEY (`id`),
  KEY `id_juego` (`id_juego`),
  CONSTRAINT `partidas_ibfk_1` FOREIGN KEY (`id_juego`) REFERENCES `juegos` (`id`),
  CONSTRAINT `CONSTRAINT_1` CHECK (`jugadores_deseados` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `partidas`
--

LOCK TABLES `partidas` WRITE;
/*!40000 ALTER TABLE `partidas` DISABLE KEYS */;
INSERT INTO `partidas` VALUES
(1,3,'2026-10-20','17:00:00',4,'programada');
/*!40000 ALTER TABLE `partidas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `socios`
--

DROP TABLE IF EXISTS `socios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `socios` (
  `documento` varchar(20) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `edad` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  PRIMARY KEY (`documento`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `socios`
--

LOCK TABLES `socios` WRITE;
/*!40000 ALTER TABLE `socios` DISABLE KEYS */;
INSERT INTO `socios` VALUES
('12345678A','Ana','Garcia','600123456',28,'ana@ejemplo.com');
/*!40000 ALTER TABLE `socios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-10-13 13:45:01
