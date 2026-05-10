-- =========================================================
-- SCRIPT SQL: DATOS DE PRUEBA EXCLUSIVOS PARA PAGOS
-- Ejecuta esto en phpMyAdmin sobre la BD: db_pagos
-- =========================================================

USE db_pagos;

-- ---------------------------------------------------------
-- CASUÍSTICA DE PAGOS
-- ---------------------------------------------------------

-- 1. Pago APROBADO (Con Factura)
INSERT INTO transacciones_pago (id_pedido, monto, metodo_pago, fecha_pago, estado_pago) VALUES
(1, 350000, 'WEBPAY', NOW(), 'APROBADO');

INSERT INTO facturas (transaccion_id, rut_cliente, url_documento) VALUES
(1, '19.123.456-7', 'https://storage.plataforma.com/facturas/1.pdf');


-- 2. Pago APROBADO (Sin Factura, el cliente no pidió comprobante o dio RUT vacío)
INSERT INTO transacciones_pago (id_pedido, monto, metodo_pago, fecha_pago, estado_pago) VALUES
(2, 45000, 'TRANSFERENCIA', NOW(), 'APROBADO');
-- (No se inserta registro en la tabla facturas para este ID)


-- 3. Pago RECHAZADO (Monto insuficiente o tarjeta bloqueada)
INSERT INTO transacciones_pago (id_pedido, monto, metodo_pago, fecha_pago, estado_pago) VALUES
(3, 120000, 'WEBPAY', NOW(), 'RECHAZADO');
-- (Al ser rechazado, lógicamente no genera factura)
