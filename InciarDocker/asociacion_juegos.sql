-- Asociacion de juegos de mesa (MariaDB 11.4)
-- Importacion completa y ampliada: crea la base de datos, tablas, relaciones y datos de ejemplo.
-- ATENCION: BORRA Y RECREA las cinco tablas; usar solo para instalaciones limpias.
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

-- ============================================================
-- DATOS ADICIONALES DE DEMOSTRACION
-- Insertados en orden para respetar todas las claves foraneas.
-- Ejemplos ficticios; NO ejecutar dos veces en la misma base de datos.
-- ============================================================

INSERT INTO `socios`
(`documento`, `nombre`, `apellidos`, `telefono`, `edad`, `email`)
VALUES
('20000001B', 'Carlos', 'Martinez Ruiz', '600200001', 34, 'carlos.martinez@example.com'),
('20000002C', 'Lucia', 'Fernandez Lopez', '600200002', 25, 'lucia.fernandez@example.com'),
('20000003D', 'Javier', 'Rodriguez Perez', '600200003', 41, 'javier.rodriguez@example.com'),
('20000004E', 'Marta', 'Lopez Alonso', '600200004', 31, 'marta.lopez@example.com'),
('20000005F', 'Diego', 'Sanchez Torres', '600200005', 22, 'diego.sanchez@example.com'),
('20000006G', 'Nerea', 'Gonzalez Garcia', '600200006', 29, 'nerea.gonzalez@example.com'),
('20000007H', 'Iker', 'Perez Martin', '600200007', 38, 'iker.perez@example.com'),
('20000008J', 'Elena', 'Gomez Vazquez', '600200008', 47, 'elena.gomez@example.com'),
('20000009K', 'Ander', 'Diaz Romero', '600200009', 19, 'ander.diaz@example.com'),
('20000010L', 'Sara', 'Moreno Castro', '600200010', 26, 'sara.moreno@example.com'),
('20000011M', 'Pablo', 'Alvarez Soto', '600200011', 36, 'pablo.alvarez@example.com'),
('20000012N', 'Ainhoa', 'Jimenez Ortiz', '600200012', 33, 'ainhoa.jimenez@example.com'),
('20000013P', 'Raul', 'Navarro Gil', '600200013', 44, 'raul.navarro@example.com'),
('20000014Q', 'Leire', 'Ruiz Salazar', '600200014', 27, 'leire.ruiz@example.com'),
('20000015R', 'Oscar', 'Blanco Molina', '600200015', 52, 'oscar.blanco@example.com');

INSERT INTO `juegos`
(`id`, `nombre`, `autores`, `descripcion`, `min_jugadores`, `max_jugadores`, `duracion_min`, `tematica`, `tipo`, `ubicacion`, `estado`, `documento_propietario`, `fecha_alta`)
VALUES
(4, 'Dixit', 'Jean-Louis Roubira', 'Juego de pistas e imaginacion', 3, 6, 30, 'Fantasia', 'competitivo', 'Estanteria B1', 'disponible', NULL, '2026-01-10'),
(5, 'Pandemic', 'Matt Leacock', 'Salvar el mundo de varias epidemias', 2, 4, 45, 'Cooperacion', 'cooperativo', 'Estanteria B2', 'disponible', '20000001B', '2026-01-12'),
(6, 'Azul', 'Michael Kiesling', 'Colocar azulejos para sumar puntos', 2, 4, 40, 'Abstracto', 'competitivo', 'Estanteria B3', 'disponible', NULL, '2026-01-15'),
(7, '7 Wonders', 'Antoine Bauza', 'Desarrollar civilizaciones mediante cartas', 3, 7, 30, 'Civilizaciones', 'competitivo', 'Estanteria B4', 'disponible', '20000003D', '2026-02-02'),
(8, 'Ticket to Ride Europa', 'Alan R. Moon', 'Completar rutas ferroviarias', 2, 5, 60, 'Trenes', 'competitivo', 'Estanteria C1', 'disponible', NULL, '2026-02-05'),
(9, 'Terraforming Mars', 'Jacob Fryxelius', 'Desarrollar proyectos en Marte', 1, 5, 120, 'Ciencia ficcion', 'competitivo', 'Estanteria C2', 'prestado', '20000004E', '2026-02-10'),
(10, 'Codigo Secreto', 'Vlaada Chvatil', 'Descubrir palabras a partir de pistas', 2, 8, 20, 'Deduccion', 'competitivo', 'Estanteria C3', 'disponible', NULL, '2026-03-12'),
(11, 'Arkham Horror LCG', 'Nate French y Matthew Newman', 'Investigacion de misterios cooperativa', 1, 4, 90, 'Terror', 'cooperativo', 'Estanteria C4', 'disponible', '20000006G', '2026-03-14'),
(12, 'Exploding Kittens', 'Matthew Inman y Elan Lee', 'Cartas de humor y eliminacion', 2, 5, 15, 'Cartas', 'competitivo', 'Estanteria D1', 'disponible', NULL, '2026-04-01'),
(13, 'Wingspan', 'Elizabeth Hargrave', 'Crear una reserva de aves', 1, 5, 60, 'Naturaleza', 'competitivo', 'Estanteria D2', 'disponible', '20000008J', '2026-04-08'),
(14, 'Betrayal at House on the Hill', 'Bruce Glassco', 'Exploracion de una mansion encantada', 3, 6, 60, 'Terror', 'mixto', 'Estanteria D3', 'disponible', '20000010L', '2026-05-13'),
(15, 'The Crew', 'Thomas Sing', 'Completar misiones con bazas', 3, 5, 20, 'Espacio', 'cooperativo', 'Estanteria D4', 'disponible', NULL, '2026-06-01'),
(16, 'Sagrada', 'Adrian Adamescu y Daryl Andrews', 'Crear vidrieras con dados', 1, 4, 40, 'Abstracto', 'competitivo', 'Estanteria E1', 'disponible', '20000002C', '2026-06-16'),
(17, 'Brass Birmingham', 'Gavan Brown, Matt Tolman y Martin Wallace', 'Gestion industrial y economica', 2, 4, 120, 'Economia', 'competitivo', 'Estanteria E2', 'disponible', '20000007H', '2026-07-05'),
(18, 'MicroMacro Crime City', 'Johannes Sich', 'Resolver casos en un mapa', 1, 4, 30, 'Investigacion', 'cooperativo', 'Estanteria E3', 'disponible', NULL, '2026-07-18'),
(19, 'Root', 'Cole Wehrle', 'Conflicto entre facciones del bosque', 2, 4, 90, 'Estrategia', 'competitivo', 'Estanteria E4', 'reparacion', '20000011M', '2026-08-04'),
(20, 'Sushi Go!', 'Phil Walker-Harding', 'Coleccionar combinaciones de cartas', 2, 5, 20, 'Cartas', 'competitivo', 'Estanteria F1', 'disponible', NULL, '2026-08-12'),
(21, 'Mysterium', 'Oleksandr Nevskiy y Oleg Sidorenko', 'Resolver un misterio con visiones', 2, 7, 45, 'Misterio', 'cooperativo', 'Estanteria F2', 'disponible', '20000012N', '2026-09-01');

INSERT INTO `partidas`
(`id`, `id_juego`, `fecha`, `hora`, `jugadores_deseados`, `estado`)
VALUES
(2, 4, '2026-02-12', '18:00:00', 5, 'finalizada'),
(3, 5, '2026-03-20', '17:30:00', 4, 'finalizada'),
(4, 6, '2026-03-29', '17:00:00', 4, 'finalizada'),
(5, 7, '2026-04-18', '18:00:00', 5, 'finalizada'),
(6, 8, '2026-05-09', '11:00:00', 4, 'finalizada'),
(7, 9, '2026-06-02', '18:00:00', 3, 'finalizada'),
(8, 10, '2026-06-19', '19:00:00', 6, 'finalizada'),
(9, 11, '2026-07-11', '11:00:00', 3, 'finalizada'),
(10, 13, '2026-08-03', '17:00:00', 4, 'finalizada'),
(11, 14, '2026-09-12', '18:00:00', 5, 'finalizada'),
(12, 15, '2026-10-23', '17:00:00', 4, 'programada'),
(13, 16, '2026-10-25', '11:00:00', 4, 'programada'),
(14, 17, '2026-10-29', '18:00:00', 3, 'programada'),
(15, 18, '2026-11-02', '17:00:00', 4, 'programada'),
(16, 20, '2026-10-24', '12:00:00', 5, 'cancelada'),
(17, 21, '2026-11-04', '18:00:00', 5, 'programada'),
(18, 3, '2026-10-16', '17:00:00', 4, 'finalizada'),
(19, 1, '2026-10-17', '11:00:00', 4, 'finalizada'),
(20, 12, '2026-06-06', '17:00:00', 4, 'finalizada'),
(21, 19, '2026-10-26', '18:00:00', 4, 'programada'),
(22, 1, '2026-11-10', '18:00:00', 5, 'programada');

INSERT INTO `inscripciones`
(`id`, `id_partida`, `documento_socio`, `nombre_invitado`, `ganador`)
VALUES
(3, 2, '20000001B', NULL, 0),
(4, 2, '20000002C', NULL, 1),
(5, 2, '20000003D', NULL, 0),
(6, 2, '20000004E', NULL, 0),
(7, 2, NULL, 'David Perez', 0),
(8, 3, '20000001B', NULL, 1),
(9, 3, '20000005F', NULL, 1),
(10, 3, '20000006G', NULL, 1),
(11, 3, '20000007H', NULL, 1),
(12, 4, '20000002C', NULL, 0),
(13, 4, '20000008J', NULL, 0),
(14, 4, '20000009K', NULL, 0),
(15, 4, '20000010L', NULL, 1),
(16, 5, '20000003D', NULL, 0),
(17, 5, '20000004E', NULL, 0),
(18, 5, '20000011M', NULL, 1),
(19, 5, '20000012N', NULL, 0),
(20, 5, '20000013P', NULL, 0),
(21, 6, '20000001B', NULL, 0),
(22, 6, '20000006G', NULL, 1),
(23, 6, '20000010L', NULL, 0),
(24, 6, '20000014Q', NULL, 0),
(25, 7, '20000004E', NULL, 0),
(26, 7, '20000007H', NULL, 0),
(27, 7, '20000015R', NULL, 1),
(28, 8, '20000005F', NULL, 0),
(29, 8, '20000009K', NULL, 0),
(30, 8, '20000010L', NULL, 0),
(31, 8, '20000012N', NULL, 0),
(32, 8, NULL, 'Raquel Torres', 1),
(33, 8, NULL, 'Adrian Vega', 0),
(34, 9, '20000006G', NULL, 1),
(35, 9, '20000011M', NULL, 1),
(36, 9, '20000013P', NULL, 1),
(37, 10, '20000002C', NULL, 0),
(38, 10, '20000004E', NULL, 1),
(39, 10, '20000008J', NULL, 0),
(40, 10, '20000014Q', NULL, 0),
(41, 11, '20000003D', NULL, 1),
(42, 11, '20000007H', NULL, 0),
(43, 11, '20000012N', NULL, 0),
(44, 11, NULL, 'Julia Santos', 1),
(45, 11, NULL, 'Mario Herrero', 0),
(46, 12, '20000001B', NULL, 0),
(47, 12, '20000005F', NULL, 0),
(48, 12, '20000008J', NULL, 0),
(49, 13, '20000002C', NULL, 0),
(50, 13, '20000009K', NULL, 0),
(51, 13, '20000011M', NULL, 0),
(52, 13, NULL, 'Beatriz Luna', 0),
(53, 14, '20000003D', NULL, 0),
(54, 14, '20000004E', NULL, 0),
(55, 15, '20000006G', NULL, 0),
(56, 15, '20000010L', NULL, 0),
(57, 15, '20000012N', NULL, 0),
(58, 17, '20000007H', NULL, 0),
(59, 17, '20000013P', NULL, 0),
(60, 17, '20000015R', NULL, 0),
(61, 18, '20000001B', NULL, 0),
(62, 18, '20000002C', NULL, 0),
(63, 18, '20000003D', NULL, 1),
(64, 18, NULL, 'Pilar Medina', 0),
(65, 19, '20000001B', NULL, 0),
(66, 19, '20000004E', NULL, 0),
(67, 19, '20000007H', NULL, 0),
(68, 19, '20000008J', NULL, 1),
(69, 20, '20000005F', NULL, 1),
(70, 20, '20000009K', NULL, 0),
(71, 20, '20000013P', NULL, 0),
(72, 20, '20000015R', NULL, 0),
(73, 21, '20000003D', NULL, 0),
(74, 21, '20000006G', NULL, 0),
(75, 21, '20000011M', NULL, 0),
(76, 22, '20000001B', NULL, 0),
(77, 22, '20000002C', NULL, 0),
(78, 22, NULL, 'Sergio Blanco', 0);

-- Verificacion rapida.
SELECT 'asociacion' AS tabla, COUNT(*) AS registros FROM `asociacion`
UNION ALL SELECT 'socios', COUNT(*) FROM `socios`
UNION ALL SELECT 'juegos', COUNT(*) FROM `juegos`
UNION ALL SELECT 'partidas', COUNT(*) FROM `partidas`
UNION ALL SELECT 'inscripciones', COUNT(*) FROM `inscripciones`;
