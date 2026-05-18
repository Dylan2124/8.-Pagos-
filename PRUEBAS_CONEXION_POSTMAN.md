# Pruebas de Conexión (OpenFeign) - MS Pagos

Este documento detalla cómo probar la integración de `ms-pagos` con otros microservicios mediante Postman. 
La comunicación está protegida por un patrón de Tolerancia a Fallos, por lo que el sistema no crasheará si los microservicios destino están apagados, sino que lo registrará en la consola.

---

## 1. Notificar a Pedidos y Enviar Correo (Tras Pago Exitoso)

**Objetivo:** Validar que al procesar un pago exitoso, `ms-pagos` contacta a `ms-pedido` para actualizar el estado de la compra y a `ms-notificacion` para emitir el comprobante.

*   **Método:** `POST`
*   **URL:** `http://localhost:8088/api/pagos`
*   **Body (JSON):**
    ```json
    {
      "idPedido": 1,
      "monto": 500000,
      "metodoPago": "WEBPAY"
    }
    ```

### Resultados Esperados en Consola de MS-PAGOS:

Si los microservicios externos están **ENCENDIDOS**:
> *"Estado del pedido ID 1 actualizado a PAGADO remotamente en ms-pedido."*
> *"Notificacion enviada exitosamente."*

Si los microservicios externos están **APAGADOS** (Demostración de Tolerancia a Fallos):
> *"El pago se guardó pero hubo un error comunicando con ms-pedido..."*
> *"ATENCION: No se pudo conectar con ms-notificaciones. El pago es valido pero no se envió correo..."*

En ambos casos, Postman debe devolver `201 Created` o `200 OK` porque la transacción de pago local sí fue exitosa.
