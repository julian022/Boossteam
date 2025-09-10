DROP DATABASE IF EXISTS bossteam;
CREATE DATABASE bossteam CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE bossteam;

/* ========= TABLAS PRINCIPALES ========= */

-- USUARIO (vendedor/administrador)
CREATE TABLE usuario (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre        VARCHAR(30) NOT NULL,
  correo        varchar(160) NOT NULL,
  password_hash VARCHAR(16) NOT NULL,
  rol           ENUM('ADMIN','VENDEDOR',' ') NOT NULL DEFAULT ' ',
  creado_en     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_usuario_correo (correo),
  UNIQUE KEY uk_usuario_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CATÁLOGO DE ESTADOS (flujo de etapas con orden)
CREATE TABLE estado (
  id       TINYINT PRIMARY KEY,
  codigo   VARCHAR(30) NOT NULL UNIQUE,      -- PRE_OFERTA, OFERTA, ACEPTADO, FIRMADO, EJECUCION, FINALIZADO, PERDIDO
  nombre   VARCHAR(50) NOT NULL,             -- Etiqueta visible
  orden    TINYINT NOT NULL,                 -- Para validar avance (no retroceder)
  es_cierre ENUM('NO','GANADO','PERDIDO') NOT NULL DEFAULT 'NO'  -- marca cierres
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CLIENTE (cartera de cada vendedor)
CREATE TABLE cliente (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre          VARCHAR(120) NOT NULL,
  empresa         VARCHAR(160) NOT NULL,
  contacto        VARCHAR(160) NULL,         -- email/teléfono u otro dato de contacto
  vendedor_id     BIGINT NOT NULL,           -- dueño de la cartera
  estado_actual_id TINYINT NOT NULL,         -- estado vigente
  creado_en       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_cliente_vendedor  FOREIGN KEY (vendedor_id)     REFERENCES usuario(id),
  CONSTRAINT fk_cliente_estado    FOREIGN KEY (estado_actual_id) REFERENCES estado(id),
  KEY ix_cliente_vendedor (vendedor_id),
  KEY ix_cliente_estado (estado_actual_id),
  KEY ix_cliente_empresa (empresa),
  KEY ix_cliente_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- HISTORIAL DE ETAPAS (cambios con fecha/usuario/comentario)
CREATE TABLE etapa_historial (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  cliente_id   BIGINT NOT NULL,
  estado_id    TINYINT NOT NULL,
  fecha_cambio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  comentario   VARCHAR(500) NULL,
  usuario_id   BIGINT NOT NULL, -- quién hizo el cambio
  CONSTRAINT fk_hist_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
  CONSTRAINT fk_hist_estado  FOREIGN KEY (estado_id)  REFERENCES estado(id),
  CONSTRAINT fk_hist_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  KEY ix_hist_cliente_fecha (cliente_id, fecha_cambio),
  KEY ix_hist_estado (estado_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- INTERACCIONES (reunión, llamada, correo, etc.)
CREATE TABLE interaccion (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  cliente_id  BIGINT NOT NULL,
  usuario_id  BIGINT NOT NULL, -- autor (vendedor)
  fecha       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo        ENUM('REUNION','LLAMADA','CORREO','OTRO') NOT NULL DEFAULT 'OTRO',
  nota        TEXT NULL,
  CONSTRAINT fk_int_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
  CONSTRAINT fk_int_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  KEY ix_int_cliente_fecha (cliente_id, fecha),
  KEY ix_int_usuario (usuario_id),
  KEY ix_int_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ADJUNTOS por interacción (URLs o rutas)
CREATE TABLE adjunto (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  interaccion_id BIGINT NOT NULL,
  archivo_url    VARCHAR(500) NOT NULL,
  nombre_archivo VARCHAR(255) NULL,
  creado_en      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_adj_inter FOREIGN KEY (interaccion_id) REFERENCES interaccion(id) ON DELETE CASCADE,
  KEY ix_adj_inter (interaccion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* ========= DATA INICIAL ========= */

-- Estados según tu flujo: Pre-oferta, Oferta, Aceptado, Firmado, Ejecución, Finalizado, Perdido
INSERT INTO estado (id, codigo, nombre, orden, es_cierre) VALUES
  (1, 'PRE_OFERTA', 'Pre-oferta',  1, 'NO'),
  (2, 'OFERTA',     'Oferta',      2, 'NO'),
  (3, 'ACEPTADO',   'Aceptado',    3, 'NO'),
  (4, 'FIRMADO',    'Firmado',     4, 'NO'),
  (5, 'EJECUCION',  'Ejecución',   5, 'NO'),
  (6, 'FINALIZADO', 'Finalizado',  6, 'GANADO'),
  (7, 'PERDIDO',    'Perdido',   127, 'PERDIDO');  -- orden alto para permitir salto a Perdido

/* ========= REGLAS / TRIGGERS ========= */

-- 1) Al crear cliente, si no se especifica estado, poner PRE_OFERTA.
DELIMITER $$
CREATE TRIGGER trg_cliente_before_insert
BEFORE INSERT ON cliente
FOR EACH ROW
BEGIN
  IF NEW.estado_actual_id IS NULL THEN
    SET NEW.estado_actual_id = 1; -- PRE_OFERTA
  END IF;
END$$
DELIMITER ;

-- 2) Validar cambios de estado (no se puede retroceder salvo pasar a PERDIDO).
--    Y actualizar estado_actual del cliente cuando se inserta en historial.
DELIMITER $$
CREATE TRIGGER trg_historial_before_insert
BEFORE INSERT ON etapa_historial
FOR EACH ROW
BEGIN
  DECLARE orden_actual TINYINT;
  DECLARE orden_nuevo  TINYINT;
  DECLARE es_perdido   ENUM('NO','GANADO','PERDIDO');

  SELECT e.orden INTO orden_actual
    FROM cliente c
    JOIN estado e ON e.id = c.estado_actual_id
   WHERE c.id = NEW.cliente_id
   FOR UPDATE;

  SELECT e.orden, e.es_cierre INTO orden_nuevo, es_perdido
    FROM estado e
   WHERE e.id = NEW.estado_id;

  -- Permitir avanzar (orden_nuevo > orden_actual) o ir a PERDIDO; bloquear retroceso
  IF NOT (orden_nuevo > orden_actual OR es_perdido = 'PERDIDO') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede volver a etapas previas.';
  END IF;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_historial_after_insert
AFTER INSERT ON etapa_historial
FOR EACH ROW
BEGIN
  UPDATE cliente
     SET estado_actual_id = NEW.estado_id,
         actualizado_en   = CURRENT_TIMESTAMP
   WHERE id = NEW.cliente_id;
END$$
DELIMITER ;

/* ========= INDICES PARA REPORTES ========= */
-- Clientes por estado (ya hay ix_cliente_estado)
-- Proyectos cerrados (GANADOS/PERDIDOS): depende de estado.es_cierre
-- Rendimiento por vendedor: ix_cliente_vendedor + ix_hist_cliente_fecha cubren la mayoría de consultas

/* ========= USUARIO DEMO (opcional) ========= */
INSERT INTO usuario (nombre, correo, password_hash, rol)
VALUES ('Admin', 'admin@demo.local', 'bosslocal', 'ADMIN');
