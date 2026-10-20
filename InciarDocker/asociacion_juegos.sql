-- Asociacion de juegos de mesa (MariaDB 11.4)
-- Importacion completa: crea la base de datos, tablas, relaciones y datos de ejemplo.
-- ATENCION: sustituye los datos actuales de estas cinco tablas si ya existen.
-- Uso: importar con el cliente mariadb conectado como un usuario con permisos DDL.

CREATE DATABASE IF NOT EXISTS `asociacion_juegos`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_uca1400_ai_ci;

USE `asociacion_juegos`;
SET NAMES utf8mb4;

-- Borrar primero las tablas hijas para no romper las claves foraneas.
DROP TABLE IF EXISTS `inscripciones`;
DROP TABLE IF EXISTS `partidas`;
DROP TABLE IF EXISTS `juegos`;
DROP TABLE IF EXISTS `socios`;
DROP TABLE IF EXISTS `asociacion`;

-- 1. Datos de la asociacion.
CREATE TABLE `asociacion` (
    `id` INT NOT NULL,
    `razon_social` VARCHAR(100) NOT NULL,
    `cif` VARCHAR(20) NOT NULL,
    `direccion` VARCHAR(150) DEFAULT NULL,
    `telefono` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `fecha_apertura` DATE DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_asociacion_cif` (`cif`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 2. Socios.
CREATE TABLE `socios` (
    `documento` VARCHAR(20) NOT NULL,
    `nombre` VARCHAR(50) NOT NULL,
    `apellidos` VARCHAR(100) NOT NULL,
    `telefono` VARCHAR(20) DEFAULT NULL,
    `edad` INT NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`documento`),
    UNIQUE KEY `uq_socios_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 3. Juegos; propietario nulo significa que pertenece a la asociacion.
CREATE TABLE `juegos` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(100) NOT NULL,
    `autores` VARCHAR(255) NOT NULL,
    `descripcion` TEXT DEFAULT NULL,
    `min_jugadores` INT NOT NULL,
    `max_jugadores` INT NOT NULL,
    `duracion_min` INT NOT NULL,
    `tematica` VARCHAR(100) DEFAULT NULL,
    `tipo` ENUM('competitivo','cooperativo','mixto') NOT NULL,
    `ubicacion` VARCHAR(100) DEFAULT NULL,
    `estado` ENUM('disponible','prestado','reparacion') NOT NULL DEFAULT 'disponible',
    `documento_propietario` VARCHAR(20) DEFAULT NULL,
    `fecha_alta` DATE NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_juegos_propietario` (`documento_propietario`),
    CONSTRAINT `fk_juegos_socios`
        FOREIGN KEY (`documento_propietario`) REFERENCES `socios` (`documento`),
    CONSTRAINT `chk_juegos_min` CHECK (`min_jugadores` >= 1),
    CONSTRAINT `chk_juegos_max` CHECK (`max_jugadores` >= `min_jugadores`),
    CONSTRAINT `chk_juegos_duracion` CHECK (`duracion_min` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 4. Partidas.
CREATE TABLE `partidas` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `id_juego` INT NOT NULL,
    `fecha` DATE NOT NULL,
    `hora` TIME NOT NULL,
    `jugadores_deseados` INT NOT NULL,
    `estado` ENUM('programada','finalizada','cancelada') NOT NULL DEFAULT 'programada',
    PRIMARY KEY (`id`),
    KEY `idx_partidas_juego` (`id_juego`),
    CONSTRAINT `fk_partidas_juegos`
        FOREIGN KEY (`id_juego`) REFERENCES `juegos` (`id`),
    CONSTRAINT `chk_partidas_jugadores` CHECK (`jugadores_deseados` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 5. Inscripciones: socio O invitado (nunca ambos).
CREATE TABLE `inscripciones` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `id_partida` INT NOT NULL,
    `documento_socio` VARCHAR(20) DEFAULT NULL,
    `nombre_invitado` VARCHAR(100) DEFAULT NULL,
    `ganador` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_partida_socio` (`id_partida`, `documento_socio`),
    KEY `idx_inscripciones_socio` (`documento_socio`),
    CONSTRAINT `fk_inscripciones_partidas`
        FOREIGN KEY (`id_partida`) REFERENCES `partidas` (`id`),
    CONSTRAINT `fk_inscripciones_socios`
        FOREIGN KEY (`documento_socio`) REFERENCES `socios` (`documento`),
    CONSTRAINT `chk_inscripcion_participante` CHECK (
        (`documento_socio` IS NOT NULL AND `nombre_invitado` IS NULL)
        OR
        (`documento_socio` IS NULL AND `nombre_invitado` IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Insertar en orden: asociacion y socios, despues juegos, partidas e inscripciones.
INSERT INTO `asociacion`
(`id`, `razon_social`, `cif`, `direccion`, `telefono`, `email`, `fecha_apertura`)
VALUES
(1, 'Asociacion Dados y Dragones', 'G00000000',
 'Calle Dato 10, Vitoria-Gasteiz', '945123456', 'info@ejemplo.com', '2024-01-15');

INSERT INTO `socios`
(`documento`, `nombre`, `apellidos`, `telefono`, `edad`, `email`)
VALUES
('12345678A', 'Ana', 'Garcia', '600123456', 28, 'ana@ejemplo.com');

INSERT INTO `juegos`
(`id`, `nombre`, `autores`, `descripcion`, `min_jugadores`,
 `max_jugadores`, `duracion_min`, `tematica`, `tipo`, `ubicacion`,
 `estado`, `documento_propietario`, `fecha_alta`)
VALUES
(1, 'Carcassonne', 'Klaus-Jürgen Wrede', 'Juego de colocacion de losetas',
 2, 5, 45, 'Estrategia', 'competitivo', 'Estanteria A1', 'disponible',
 '12345678A', '2026-10-13'),
(3, 'Catan', 'Klaus Teuber', NULL,
 3, 4, 90, NULL, 'competitivo', 'Estanteria A2', 'disponible',
 NULL, '2026-10-13');

INSERT INTO `partidas`
(`id`, `id_juego`, `fecha`, `hora`, `jugadores_deseados`, `estado`)
VALUES
(1, 3, '2026-10-20', '17:00:00', 4, 'programada');

INSERT INTO `inscripciones`
(`id`, `id_partida`, `documento_socio`, `nombre_invitado`, `ganador`)
VALUES
(1, 1, '12345678A', NULL, 0),
(2, 1, NULL, 'Pedro Martinez', 0);

-- Mantener el siguiente AUTO_INCREMENT del volcado original.
ALTER TABLE `juegos` AUTO_INCREMENT = 4;
ALTER TABLE `partidas` AUTO_INCREMENT = 2;
ALTER TABLE `inscripciones` AUTO_INCREMENT = 3;

-- Verificacion rapida.
SELECT 'asociacion' AS tabla, COUNT(*) AS registros FROM `asociacion`
UNION ALL SELECT 'socios', COUNT(*) FROM `socios`
UNION ALL SELECT 'juegos', COUNT(*) FROM `juegos`
UNION ALL SELECT 'partidas', COUNT(*) FROM `partidas`
UNION ALL SELECT 'inscripciones', COUNT(*) FROM `inscripciones`;
