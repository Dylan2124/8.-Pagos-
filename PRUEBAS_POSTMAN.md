# Pruebas para Postman: Microservicio de Pagos

Aquí tienes los bloques de código JSON listos para probar el **ms-pagos** (Puerto 8082).

### 1. Procesar un Pago EXITOSO (Con Factura)
- **Método:** `POST`
- **URL:** `http://localhost:8082/api/pagos`
- **Body JSON:**
```json
{
  "idPedido": 1,
  "monto": 350000,
  "metodoPago": "WEBPAY",
  "rutCliente": "19.123.456-7"
}
```

### 2. Procesar un Pago EXITOSO (Sin Factura)
- **Método:** `POST`
- **URL:** `http://localhost:8082/api/pagos`
- **Body JSON:**
```json
{
  "idPedido": 2,
  "monto": 45000,
  "metodoPago": "TRANSFERENCIA",
  "rutCliente": ""
}
```

### 3. Procesar un Pago RECHAZADO (Simulado enviando monto 0)
- **Método:** `POST`
- **URL:** `http://localhost:8082/api/pagos`
- **Body JSON:**
```json
{
  "idPedido": 3,
  "monto": 0,
  "metodoPago": "WEBPAY",
  "rutCliente": "11.222.333-4"
}
```

### 4. Buscar historial de pagos de un Pedido
- **Método:** `GET`
- **URL:** `http://localhost:8082/api/pagos/pedido/1`
- **Body:** *(No requiere)*
