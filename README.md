# Microservicio: Pagos

## 1. Descripción del Microservicio
Este microservicio es responsable de la gestión financiera dentro de la plataforma. Se encarga de procesar las transacciones de las órdenes de compra, interactuar con las pasarelas de pago externas y coordinar la facturación electrónica.

**Rol dentro del Sistema:**
Recibe la intención de pago asociada a un Pedido y registra los intentos de pago. Interactúa de forma lógica con el microservicio de **Pedidos** para confirmar si la transacción fue exitosa o rechazada, permitiendo que el flujo de compra continúe o se detenga.

---

## 2. Base de Datos: `db_pagos`

De acuerdo con la rúbrica del proyecto, este microservicio posee su propia base de datos independiente, la cual no comparte tablas con otros microservicios.

### 2.1 Tablas y Estructura

#### Tabla: `transacciones_pago`
Almacena los intentos de pago asociados a un pedido específico.

| Atributo | Tipo de Dato | Descripción |
| :--- | :--- | :--- |
| `id_transaccion` | Primary Key | Identificador único y autoincremental de la transacción. |
| `id_pedido` | Integer | Referencia lógica al MS de Pedidos (NO es clave foránea). |
| `monto` | Integer | Monto total procesado en la transacción. |
| `metodo_pago` | Varchar | Pasarela o método utilizado (Ej: WEBPAY, TRANSFERENCIA). |
| `fecha_pago` | Datetime | Fecha y hora en la que se realizó la transacción. |
| `estado_pago` | Varchar | Estado resultante del pago (Ej: APROBADO, RECHAZADO). |

#### Tabla: `facturas`
Almacena la información de facturación electrónica (Añade complejidad y valor al modelo).

| Atributo | Tipo de Dato | Descripción |
| :--- | :--- | :--- |
| `id_factura` | Primary Key | Identificador único y autoincremental de la factura. |
| `transaccion_id` | Integer | Foreign Key referenciando a `transacciones_pago(id_transaccion)`. |
| `rut_cliente` | Varchar | RUT o identificación tributaria del cliente. |
| `url_documento` | Varchar | Enlace de descarga al documento PDF generado de la factura. |

### 2.2 Relaciones Internas
* **1 a 1:** Un registro en la tabla `transacciones_pago` tiene una única y exclusiva relación con un registro en la tabla `facturas`.

---

## 3. Tecnologías
* **Lenguaje:** Java 21
* **Framework:** Spring Boot (Spring Web, Spring Data JPA, Validation)
* **Base de Datos:** MySQL
* **Otros:** Lombok
