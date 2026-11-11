CREATE DATABASE IF NOT EXISTS empresa;
USE empresa;

DROP TABLE IF EXISTS recetas;
DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS insumos;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS empleados;
DROP TABLE IF EXISTS clientes;

CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    direccion TEXT,
    correo VARCHAR(100)
);

CREATE TABLE empleados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    puesto VARCHAR(20) NOT NULL,
    clave VARCHAR(60) NOT NULL,
    CHECK (puesto IN ('administrador', 'camarero'))
);

CREATE TABLE productos (
    codigo VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(50)
);

CREATE TABLE insumos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2),
    stock INT DEFAULT 0,
    categoria VARCHAR(50)
);

CREATE TABLE pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    total DECIMAL(10,2),
    descuento DECIMAL(10,2) DEFAULT 0.00,
    metodo_pago VARCHAR(50) DEFAULT 'EFECTIVO',
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE detalle_pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT,
    producto_id VARCHAR(50),
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (producto_id) REFERENCES productos(codigo)
);

-- Datos de prueba
INSERT INTO empleados (nombre, puesto, clave) VALUES ('Martin', 'administrador', '$2a$12$E6jr1mc2cP8lNFHp9Y.8Iu7WCvg.QedqCAawYKVZxCrV6hdtFN2JG');
INSERT INTO empleados (nombre, puesto, clave) VALUES ('Juan', 'camarero', '$2a$12$jPOVqUxjcJ0SsPDdkfkeeePzmpSU7wpP3Xi2oOq5D6QGIVnXLP9ne');
INSERT INTO empleados (nombre, puesto, clave) VALUES ('German', 'administrador', '$2a$12$3th9i8SCy4ku86RPCELCnufiCnOhG3sgPK6Hd.TlcoPmEDsHa6R/K');
INSERT INTO empleados (nombre, puesto, clave) VALUES ('Ander', 'administrador', '$2a$12$R8/arCKOZu995EX57igJQei0siXQhseUNatUmnNPpkz4kC182P0A.');

INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('KB01', 'Krusty Burger', 5.99, 'Hamburguesas');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('KB02', 'Doble Krusty', 7.99, 'Hamburguesas');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('KB03', 'Krusty Combo', 9.99, 'Hamburguesas');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('PF01', 'Patatas Fritas', 2.50, 'Acompañamientos');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('PF02', 'Patatas Deluxe', 3.20, 'Acompañamientos');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('PF03', 'Aros de Cebolla', 2.80, 'Acompañamientos');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('BC01', 'Buzz Cola', 1.99, 'Bebidas');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('BC02', 'Buzz Cola Zero', 1.99, 'Bebidas');
INSERT INTO productos (codigo, nombre, precio, categoria) VALUES ('BE01', 'Batido Krusty', 3.50, 'Bebidas');

INSERT INTO insumos (codigo, nombre, precio, stock, categoria) VALUES ('PAN01', 'Pan de sésamo', 0.20, 100, 'Panadería');
INSERT INTO insumos (codigo, nombre, precio, stock, categoria) VALUES ('CAR01', 'Carne de vacuno', 1.00, 50, 'Carnes');
INSERT INTO insumos (codigo, nombre, precio, stock, categoria) VALUES ('PAT01', 'Patatas crudas', 0.50, 200, 'Vegetales');
INSERT INTO insumos (codigo, nombre, precio, stock, categoria) VALUES ('QUE01', 'Queso Cheddar', 0.15, 150, 'Lácteos');
INSERT INTO insumos (codigo, nombre, precio, stock, categoria) VALUES ('SAL01', 'Salsa Especial', 0.10, 200, 'Salsas');
INSERT INTO insumos (codigo, nombre, precio, stock, categoria) VALUES ('CEB01', 'Cebollas', 0.05, 120, 'Vegetales');

INSERT INTO clientes (nombre, telefono, direccion, correo) VALUES ('Homer Simpson', '555-7334', 'Avenida Siempreviva 742', 'chunkylover53@aol.com');

CREATE TABLE recetas (
    producto_codigo VARCHAR(50),
    insumo_id INT,
    cantidad INT NOT NULL,
    PRIMARY KEY (producto_codigo, insumo_id),
    FOREIGN KEY (producto_codigo) REFERENCES productos(codigo),
    FOREIGN KEY (insumo_id) REFERENCES insumos(id)
);

-- Receta para Krusty Burger (1 Pan, 1 Carne)
-- PAN01 -> id 1, CAR01 -> id 2
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB01', 1, 1);
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB01', 2, 1);

-- Receta para Doble Krusty (1 Pan, 2 Carne, 1 Queso Cheddar)
-- QUE01 -> id 4
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB02', 1, 1);
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB02', 2, 2);
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB02', 4, 1);

-- Receta para Krusty Combo (1 Pan, 1 Carne, 1 Patatas, 1 Queso Cheddar)
-- PAT01 -> id 3
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 1, 1);
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 2, 1);
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 3, 1);
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 4, 1);

-- Receta para Patatas Fritas (1 unidad de Patatas)
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('PF01', 3, 1);

-- Receta para Patatas Deluxe (1 Patatas, 1 Salsa Especial)
-- SAL01 -> id 5
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('PF02', 3, 1);
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('PF02', 5, 1);

-- Receta para Aros de Cebolla (1 Cebollas)
-- CEB01 -> id 6
INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('PF03', 6, 1);
