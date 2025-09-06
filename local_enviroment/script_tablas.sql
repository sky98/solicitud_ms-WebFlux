CREATE TABLE solicitud.`estados` (
  `estado_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `nombre` VARCHAR(50) NULL,
  `descripcion` VARCHAR(100) NULL
);

CREATE TABLE solicitud.`tipo_prestamos` (
  `tipo_prestamo_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `nombre` VARCHAR(50) NULL,
  `monto_minimo` DECIMAL(19, 2) NOT NULL,
  `monto_maximo` DECIMAL(19, 2) NOT NULL,
  `tasa_interes` DECIMAL(5, 2) NOT NULL,
  `validacion_automatica` VARCHAR(50) NULL
);

CREATE TABLE solicitud.`solicitudes` (
  `solicitud_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `monto` DECIMAL(19, 2) NOT NULL,
  `plazo` BIGINT NOT NULL,
  `estado_id` BIGINT NOT NULL,
  `tipo_prestamo_id` BIGINT NOT NULL,
  `documento_id` BIGINT NOT NULL,
  CONSTRAINT `fk_solicitud_estados` FOREIGN KEY (`estado_id`) REFERENCES `estados` (`estado_id`),
  CONSTRAINT `fk_solicitud_tipo_prestamos` FOREIGN KEY (`tipo_prestamo_id`) REFERENCES `tipo_prestamos` (`tipo_prestamo_id`)
);

INSERT INTO `estados` (`nombre`, `descripcion`) VALUES
('Pendiente de revision', 'La solicitud ha sido recibida y está en espera de revisión.'),
('Rechazada', 'La solicitud ha sido rechazada y no se concedera el prestamo.'),
('Revision manual', 'La solicitud esta en estado pendiente de revision manual.');

INSERT INTO `tipo_prestamos` (`nombre`, `monto_minimo`, `monto_maximo`, `tasa_interes`, `validacion_automatica`) VALUES
('Personal', 1000.00, 50000.00, 10.50, 'SI'),
('Hipotecario', 50000.00, 500000.00, 5.75, 'NO'),
('Automotriz', 5000.00, 100000.00, 8.25, 'SI'),
('Educativo', 1000.00, 20000.00, 7.00, 'NO');