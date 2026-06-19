-- =========================================================
-- SCRIPT SQL: DATOS DE PRUEBA EXCLUSIVOS PARA PAGOS
-- Ejecuta esto en phpMyAdmin sobre la BD: db_pagos
-- =========================================================


-- ---------------------------------------------------------
-- CASUÍSTICA DE PAGOS
-- ---------------------------------------------------------

-- 1. Pago APROBADO (Con Factura - Webpay)
INSERT INTO transacciones_pago (id_pedido, monto, metodo_pago, fecha_pago, estado_pago) VALUES
(1, 500000, 'WEBPAY', NOW(), 'APROBADO');

INSERT INTO facturas (transaccion_id, rut_cliente, url_documento) VALUES
(1, '19.123.456-7', 'https://facturas.duoc.cl/1.pdf');


-- 2. Pago APROBADO (Con Factura - Transferencia)
INSERT INTO transacciones_pago (id_pedido, monto, metodo_pago, fecha_pago, estado_pago) VALUES
(2, 850000, 'TRANSFERENCIA', NOW(), 'APROBADO');

INSERT INTO facturas (transaccion_id, rut_cliente, url_documento) VALUES
(2, '98765432-1', 'https://facturas.duoc.cl/2.pdf');


-- 3. Pago RECHAZADO (Monto insuficiente o tarjeta bloqueada)
INSERT INTO transacciones_pago (id_pedido, monto, metodo_pago, fecha_pago, estado_pago) VALUES
(3, 120000, 'WEBPAY', NOW(), 'RECHAZADO');
-- (Al ser rechazado, lógicamente no genera factura)


-- 4. Pago APROBADO (MercadoPago)
INSERT INTO transacciones_pago (id_pedido, monto, metodo_pago, fecha_pago, estado_pago) VALUES
(4, 1500000, 'MERCADOPAGO', NOW(), 'APROBADO');

INSERT INTO facturas (transaccion_id, rut_cliente, url_documento) VALUES
(4, '11223344-5', 'https://facturas.duoc.cl/4.pdf');
