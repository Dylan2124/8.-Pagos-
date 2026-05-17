# 🧪 Guía de Pruebas de API y Control de Errores: Microservicio de Pagos (`ms-pagos`)

Esta guía detalla la suite completa de pruebas para validar el correcto funcionamiento, las reglas de negocio y el control de excepciones en el microservicio **ms-pagos** (Puerto `8088`).

---

## ⚙️ Configuración del Entorno de Pruebas
* **Base URL:** `http://localhost:8088`
* **Content-Type:** `application/json`
* **Base de Datos:** MySQL (`db_pagos` en puerto `3306`)

---

## 🎯 Resumen de Validaciones de Negocio (Bean Validation) y Control de Recursos
El microservicio utiliza **Jakarta Bean Validation** (JSR 380) para asegurar la integridad de los datos de entrada y el tipo de retorno **`java.util.Optional`** para el control de recursos existentes:

| Campo / Operación | Restricción / Estructura | Código HTTP Esperado | Mensaje de Error / Comportamiento |
| :--- | :--- | :--- | :--- |
| `idPedido` | `@NotNull` | `400 Bad Request` | `"El idPedido es obligatorio"` |
| `monto` | `@NotNull`, `@Min(1)` | `400 Bad Request` | `"El monto es obligatorio"` / `"El monto debe ser mayor a 0"` |
| `metodoPago` | `@NotEmpty` | `400 Bad Request` | `"El metodo de pago es obligatorio"` |
| `rutCliente` | `@NotEmpty` | `400 Bad Request` | `"El rutCliente es obligatorio para facturar"` |
| **Buscar inexistente** | `Optional.map().orElse(404)` | `404 Not Found` | Respuesta vacía (HTTP 404 sin cuerpo, alineado con `LibroController`) |

---

# 🟢 Escenarios Exitosos (Happy Paths)
Pruebas diseñadas para comprobar que el flujo operativo estándar de la aplicación funciona correctamente.

### 1. Procesar un Pago Válido y Generar Factura Dinámicamente
Valida que la transacción de pago se guarde en la BD, que el estado sea calculado como `"APROBADO"`, y que la factura sea generada dinámicamente con la URL simulada en S3 si el RUT no viene vacío.

* **Método:** `POST`
* **URL:** `http://localhost:8088/api/pagos`
* **Body JSON:**
  ```json
  {
    "idPedido": 101,
    "monto": 250000,
    "metodoPago": "WEBPAY",
    "rutCliente": "19.123.456-7"
  }
  ```
* **Respuesta Esperada (HTTP 201 Created):**
  ```json
  {
    "id": 1,
    "idPedido": 101,
    "monto": 250000,
    "metodoPago": "WEBPAY",
    "fechaPago": "2026-05-17T11:24:06",
    "estadoPago": "APROBADO",
    "factura": {
      "id": 1,
      "rutCliente": "19.123.456-7",
      "urlDocumento": "https://storage.plataforma.com/facturas/101.pdf"
    },
    "rutCliente": "19.123.456-7"
  }
  ```

### 2. Buscar un Pago Específico por su ID
* **Método:** `GET`
* **URL:** `http://localhost:8088/api/pagos/1`
* **Respuesta Esperada (HTTP 200 OK):**
  *(Retorna el objeto JSON completo del pago con el ID `1`).*

### 3. Obtener Historial de Pagos por ID de Pedido
* **Método:** `GET`
* **URL:** `http://localhost:8088/api/pagos/pedido/101`
* **Respuesta Esperada (HTTP 200 OK):**
  ```json
  [
    {
      "id": 1,
      "idPedido": 101,
      "monto": 250000,
      "metodoPago": "WEBPAY",
      "fechaPago": "2026-05-17T11:24:06",
      "estadoPago": "APROBADO",
      "factura": {
        "id": 1,
        "rutCliente": "19.123.456-7",
        "urlDocumento": "https://storage.plataforma.com/facturas/101.pdf"
      },
      "rutCliente": "19.123.456-7"
    }
  ]
  ```

---

# 🔴 Escenarios de Error: Validaciones de Datos (HTTP 400 Bad Request)
Casos diseñados para forzar los límites del sistema utilizando datos incorrectos, vacíos o fuera de rango. Todos son capturados por el `MethodArgumentNotValidException` de `GlobalExceptionHandler`.

### 4. Error: Procesar Pago sin ID de Pedido (`idPedido` Nulo)
* **Método:** `POST`
* **URL:** `http://localhost:8088/api/pagos`
* **Body JSON:**
  ```json
  {
    "monto": 250000,
    "metodoPago": "WEBPAY",
    "rutCliente": "19.123.456-7"
  }
  ```
* **Respuesta de Error Esperada (HTTP 400 Bad Request):**
  ```json
  {
    "status": 400,
    "timestamp": "2026-05-17T11:25:00.123456",
    "errors": {
      "idPedido": "El idPedido es obligatorio"
    }
  }
  ```

### 5. Error: Procesar Pago sin Monto (`monto` Nulo)
* **Método:** `POST`
* **URL:** `http://localhost:8088/api/pagos`
* **Body JSON:**
  ```json
  {
    "idPedido": 101,
    "metodoPago": "WEBPAY",
    "rutCliente": "19.123.456-7"
  }
  ```
* **Respuesta de Error Esperada (HTTP 400 Bad Request):**
  ```json
  {
    "status": 400,
    "timestamp": "2026-05-17T11:26:00.123456",
    "errors": {
      "monto": "El monto es obligatorio"
    }
  }
  ```

### 6. Error: Enviar Monto Inválido (Cero o Negativo)
Valida la restricción crítica que impide que un pago tenga montos negativos o iguales a cero.

* **Método:** `POST`
* **URL:** `http://localhost:8088/api/pagos`
* **Body JSON:**
  ```json
  {
    "idPedido": 101,
    "monto": -5000,
    "metodoPago": "WEBPAY",
    "rutCliente": "19.123.456-7"
  }
  ```
* **Respuesta de Error Esperada (HTTP 400 Bad Request):**
  ```json
  {
    "status": 400,
    "timestamp": "2026-05-17T11:27:00.123456",
    "errors": {
      "monto": "El monto debe ser mayor a 0"
    }
  }
  ```

### 7. Error: Enviar Método de Pago Vacío (`metodoPago` Vacío)
* **Método:** `POST`
* **URL:** `http://localhost:8088/api/pagos`
* **Body JSON:**
  ```json
  {
    "idPedido": 101,
    "monto": 250000,
    "metodoPago": "",
    "rutCliente": "19.123.456-7"
  }
  ```
* **Respuesta de Error Esperada (HTTP 400 Bad Request):**
  ```json
  {
    "status": 400,
    "timestamp": "2026-05-17T11:28:00.123456",
    "errors": {
      "metodoPago": "El metodo de pago es obligatorio"
    }
  }
  ```

### 8. Error: Enviar RUT de Cliente Vacío (`rutCliente` Vacío)
* **Método:** `POST`
* **URL:** `http://localhost:8088/api/pagos`
* **Body JSON:**
  ```json
  {
    "idPedido": 101,
    "monto": 250000,
    "metodoPago": "WEBPAY",
    "rutCliente": ""
  }
  ```
* **Respuesta de Error Esperada (HTTP 400 Bad Request):**
  ```json
  {
    "status": 400,
    "timestamp": "2026-05-17T11:29:00.123456",
    "errors": {
      "rutCliente": "El rutCliente es obligatorio para facturar"
    }
  }
  ```

### 9. Error de Validación Múltiple Combinado (Varios Errores a la vez)
Prueba la capacidad del `GlobalExceptionHandler` para acumular todos los fallos en una sola respuesta detallada en lugar de detenerse en el primero.

* **Método:** `POST`
* **URL:** `http://localhost:8088/api/pagos`
* **Body JSON:**
  ```json
  {
    "monto": 0,
    "metodoPago": "",
    "rutCliente": ""
  }
  ```
* **Respuesta de Error Esperada (HTTP 400 Bad Request):**
  ```json
  {
    "status": 400,
    "timestamp": "2026-05-17T11:30:00.123456",
    "errors": {
      "idPedido": "El idPedido es obligatorio",
      "monto": "El monto debe ser mayor a 0",
      "metodoPago": "El metodo de pago es obligatorio",
      "rutCliente": "El rutCliente es obligatorio para facturar"
    }
  }
  ```

---

# 🔴 Escenarios de Error: Recursos Inexistentes (HTTP 404 Not Found)
Casos diseñados para validar que cuando se busca un recurso que no existe, la API responda con un código de estado semántico `404` y un cuerpo JSON descriptivo para mejorar la claridad de cara al usuario.

### 10. Error: Buscar un Pago Inexistente
* **Método:** `GET`
* **URL:** `http://localhost:8088/api/pagos/9999`
* **Respuesta de Error Esperada (HTTP 404 Not Found):**
  ```json
  {
    "status": 404,
    "timestamp": "2026-05-17T13:52:40.123456",
    "error": "El pago con el ID especificado no existe"
  }
  ```


---
