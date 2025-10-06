DROP DATABASE IF EXISTS boossteam_db;
CREATE DATABASE boossteam_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE boossteam_db;
-- ==================
--  TABLA: usuario
-- ==================
DROP TABLE IF EXISTS usuario;
CREATE TABLE usuario (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre         VARCHAR(60)  NOT NULL,
  correo         VARCHAR(160) NOT NULL,
  password_hash  VARCHAR(72)  NOT NULL,
  rol            ENUM('ADMIN','VENDEDOR') NOT NULL DEFAULT 'VENDEDOR',
  creado_en      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_usuario_correo (correo),
  UNIQUE KEY uk_usuario_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- ==================
--  TABLA: estado
-- ==================
DROP TABLE IF EXISTS estado;
CREATE TABLE estado (
  id     TINYINT PRIMARY KEY,
  codigo VARCHAR(30) NOT NULL,
  nombre VARCHAR(50) NOT NULL,
  orden  TINYINT     NOT NULL,
  es_cierre ENUM('NO','GANADO','PERDIDO') NOT NULL DEFAULT 'NO',
  UNIQUE KEY uk_estado_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================
--  TABLA: cliente
-- ==================
DROP TABLE IF EXISTS cliente;
CREATE TABLE cliente (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre   VARCHAR(120) NOT NULL,
  empresa  VARCHAR(160) NOT NULL,
  contacto VARCHAR(160) NULL,
  vendedor_id      BIGINT  NOT NULL,
  estado_actual_id TINYINT NOT NULL,
  creado_en        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_cliente_vendedor  FOREIGN KEY (vendedor_id)      REFERENCES usuario(id),
  CONSTRAINT fk_cliente_estado    FOREIGN KEY (estado_actual_id)  REFERENCES estado(id),
  KEY ix_cliente_vendedor (vendedor_id),
  KEY ix_cliente_estado (estado_actual_id),
  KEY ix_cliente_empresa (empresa),
  KEY ix_cliente_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================
--  TABLA: etapa_historial
-- ==========================
DROP TABLE IF EXISTS etapa_historial;
CREATE TABLE etapa_historial (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cliente_id   BIGINT  NOT NULL,
  estado_id    TINYINT NOT NULL,
  fecha_cambio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  comentario   VARCHAR(500) NULL,
  usuario_id   BIGINT  NOT NULL,
  CONSTRAINT fk_hist_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
  CONSTRAINT fk_hist_estado FOREIGN KEY (estado_id)  REFERENCES estado(id),
  CONSTRAINT fk_hist_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  KEY ix_hist_cliente_fecha (cliente_id, fecha_cambio),
  KEY ix_hist_estado (estado_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================
--  TABLA: interaccion
-- ==================
DROP TABLE IF EXISTS interaccion;
CREATE TABLE interaccion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cliente_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo  ENUM('REUNION','LLAMADA','CORREO','OTRO') NOT NULL DEFAULT 'OTRO',
  nota  TEXT NULL,
  CONSTRAINT fk_int_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
  CONSTRAINT fk_int_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  KEY ix_int_cliente_fecha (cliente_id, fecha),
  KEY ix_int_usuario (usuario_id),
  KEY ix_int_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================
--  TABLA: adjunto
-- ==================
DROP TABLE IF EXISTS adjunto;
CREATE TABLE adjunto (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  interaccion_id BIGINT NOT NULL,
  archivo_url    VARCHAR(500) NOT NULL,
  nombre_archivo VARCHAR(255) NULL,
  creado_en      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_adj_inter FOREIGN KEY (interaccion_id) REFERENCES interaccion(id) ON DELETE CASCADE,
  KEY ix_adj_inter (interaccion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================
--  TRIGGERS (usar DELIMITER porque hay bloques)
-- ==================
DELIMITER $$

-- 1) No retroceder etapas (salvo cierre PERDIDO)
DROP TRIGGER IF EXISTS trg_historial_before_insert $$
CREATE TRIGGER trg_historial_before_insert
BEFORE INSERT ON etapa_historial
FOR EACH ROW
BEGIN
  DECLARE orden_actual TINYINT;
  DECLARE orden_nuevo  TINYINT;
  DECLARE cierre ENUM('NO','GANADO','PERDIDO');

  SELECT e.orden INTO orden_actual
  FROM cliente c JOIN estado e ON e.id = c.estado_actual_id
  WHERE c.id = NEW.cliente_id
  FOR UPDATE;

  SELECT e.orden, e.es_cierre INTO orden_nuevo, cierre
  FROM estado e WHERE e.id = NEW.estado_id;

  IF NOT (orden_nuevo > orden_actual OR cierre = 'PERDIDO') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede volver a etapas previas.';
  END IF;
END $$

-- 2) Actualizar estado_actual del cliente al insertar historial
DROP TRIGGER IF EXISTS trg_historial_after_insert $$
CREATE TRIGGER trg_historial_after_insert
AFTER INSERT ON etapa_historial
FOR EACH ROW
BEGIN
  UPDATE cliente
    SET estado_actual_id = NEW.estado_id,
        actualizado_en   = CURRENT_TIMESTAMP
  WHERE id = NEW.cliente_id;
END $$

DELIMITER ;

-- ==================
--  DATOS SEMILLA (UPSERT, se pueden ejecutar N veces)
-- ==================
INSERT INTO estado (id, codigo, nombre, orden, es_cierre) VALUES
  (1,'PRE_OFERTA','Pre-oferta',1,'NO'),
  (2,'OFERTA','Oferta',2,'NO'),
  (3,'ACEPTADO','Aceptado',3,'NO'),
  (4,'FIRMADO','Firmado',4,'NO'),
  (5,'EJECUCION','Ejecución',5,'NO'),
  (6,'FINALIZADO','Finalizado',6,'GANADO'),
  (7,'PERDIDO','Perdido',99,'PERDIDO')
ON DUPLICATE KEY UPDATE
  codigo=VALUES(codigo),
  nombre=VALUES(nombre),
  orden=VALUES(orden),
  es_cierre=VALUES(es_cierre);

INSERT INTO usuario (id, nombre, correo, password_hash, rol, creado_en)
VALUES (1,'Admin','admin@demo.local','bosslocal','ADMIN', NOW())
ON DUPLICATE KEY UPDATE
  nombre=VALUES(nombre),
  password_hash=VALUES(password_hash),
  rol=VALUES(rol);
