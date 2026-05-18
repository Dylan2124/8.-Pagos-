package cl.duoc.plataforma.ms_pagos.service;

import cl.duoc.plataforma.ms_pagos.dto.FacturaDto;
import cl.duoc.plataforma.ms_pagos.dto.TransaccionPagoDto;
import cl.duoc.plataforma.ms_pagos.dto.PedidoResponseDto;
import cl.duoc.plataforma.ms_pagos.model.Factura;
import cl.duoc.plataforma.ms_pagos.model.TransaccionPago;
import cl.duoc.plataforma.ms_pagos.repository.TransaccionPagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cl.duoc.plataforma.ms_pagos.client.NotificacionClient;
import cl.duoc.plataforma.ms_pagos.client.PedidoClient;
import cl.duoc.plataforma.ms_pagos.dto.NotificacionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════
 * CLASE: PagoService.java
 * Lógica de negocio para procesar los pagos y 
 * generar las facturas de forma automática.
 * ═══════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final TransaccionPagoRepository transaccionRepository;
    private final PedidoClient pedidoClient;
    private final NotificacionClient notificacionClient;

    @Transactional
    public TransaccionPagoDto procesarPago(TransaccionPagoDto dto) {
        log.info("Procesando pago para el pedido ID: {} por monto: {}", dto.getIdPedido(), dto.getMonto());

        try {
            // Verificar existencia y total del pedido a traves del Feign Client (Tolerancia a fallos si ms-pedido está offline)
            try {
                log.info("Consultando informacion del pedido en ms-pedido...");
                PedidoResponseDto pedido = pedidoClient.obtenerPedidoPorId(dto.getIdPedido());
                if (pedido == null) {
                    throw new RuntimeException("El pedido no existe en el sistema.");
                }
                if (pedido.getTotal() != null && dto.getMonto() < pedido.getTotal()) {
                    throw new RuntimeException("El monto (" + dto.getMonto() + ") es insuficiente para pagar el total del pedido (" + pedido.getTotal() + ").");
                }
            } catch (RuntimeException e) {
                // Si el mensaje es uno de nuestros errores de negocio, lo propagamos
                if (e.getMessage() != null && (e.getMessage().contains("El pedido no existe") || e.getMessage().contains("insuficiente"))) {
                    throw e;
                }
                log.error("El pago se guardó pero hubo un error comunicando con ms-pedido: {}", e.getMessage());
            } catch (Exception e) {
                log.error("El pago se guardó pero hubo un error comunicando con ms-pedido: {}", e.getMessage());
            }

            TransaccionPago transaccion = new TransaccionPago();
            transaccion.setIdPedido(dto.getIdPedido());
            transaccion.setMonto(dto.getMonto());
            transaccion.setMetodoPago(dto.getMetodoPago());
            transaccion.setFechaPago(LocalDateTime.now());
            
            // Simulación básica de aprobación: si el monto es mayor a 0 se aprueba
            boolean aprobado = dto.getMonto() > 0;
            transaccion.setEstadoPago(aprobado ? "APROBADO" : "RECHAZADO");

            // Si se aprueba y se nos envió un RUT, generamos una Factura
            if (aprobado && dto.getRutCliente() != null && !dto.getRutCliente().isEmpty()) {
                log.info("Pago aprobado. Generando factura para RUT: {}", dto.getRutCliente());
                Factura factura = new Factura();
                factura.setRutCliente(dto.getRutCliente());
                // URL simulada de un S3 o sistema de almacenamiento
                factura.setUrlDocumento("https://storage.plataforma.com/facturas/" + dto.getIdPedido() + ".pdf");
                
                // Enlazar bidireccionalmente
                transaccion.setFactura(factura);
            }

            TransaccionPago saved = transaccionRepository.save(transaccion);
            log.info("Transacción guardada exitosamente con ID: {}", saved.getId());

            // Si el pago es exitoso, actualizamos el estado del pedido en ms-pedido y enviamos notificacion
            if (aprobado) {
                try {
                    pedidoClient.actualizarEstado(dto.getIdPedido(), "PAGADO");
                    log.info("Estado del pedido ID {} actualizado a PAGADO remotamente en ms-pedido.", dto.getIdPedido());
                } catch (Exception e) {
                    log.error("El pago se guardó pero hubo un error comunicando con ms-pedido: {}", e.getMessage());
                }

                try {
                    log.info("Enviando notificacion de pago exitoso...");
                    NotificacionRequest notificacion = NotificacionRequest.builder()
                            .idPedido(dto.getIdPedido())
                            .tipo("PAGO_APROBADO")
                            .mensaje("Su pago por " + dto.getMonto() + " ha sido aprobado y su pedido está en proceso.")
                            .build();
                    notificacionClient.enviarNotificacion(notificacion);
                    log.info("Notificacion enviada exitosamente.");
                } catch (Exception e) {
                    log.warn("ATENCION: No se pudo conectar con ms-notificaciones. El pago es valido pero no se envió correo. Error: {}", e.getMessage());
                }
            }

            return mapToDto(saved);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && (e.getMessage().contains("El pedido no existe") || e.getMessage().contains("insuficiente"))) {
                throw e;
            }
            log.error("Error al procesar el pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al procesar transacción", e);
        } catch (Exception e) {
            log.error("Error al procesar el pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al procesar transacción", e);
        }
    }

    @Transactional(readOnly = true)
    public List<TransaccionPagoDto> obtenerPagosPorPedido(Long idPedido) {
        log.info("Obteniendo historial de pagos para el pedido ID: {}", idPedido);
        return transaccionRepository.findByIdPedido(idPedido).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TransaccionPagoDto> obtenerPagoPorId(Long idPago) {
        return transaccionRepository.findById(idPago)
                .map(this::mapToDto);
    }

    private TransaccionPagoDto mapToDto(TransaccionPago transaccion) {
        FacturaDto facturaDto = null;
        if (transaccion.getFactura() != null) {
            facturaDto = FacturaDto.builder()
                    .id(transaccion.getFactura().getId())
                    .rutCliente(transaccion.getFactura().getRutCliente())
                    .urlDocumento(transaccion.getFactura().getUrlDocumento())
                    .build();
        }

        return TransaccionPagoDto.builder()
                .id(transaccion.getId())
                .idPedido(transaccion.getIdPedido())
                .monto(transaccion.getMonto())
                .metodoPago(transaccion.getMetodoPago())
                .fechaPago(transaccion.getFechaPago())
                .estadoPago(transaccion.getEstadoPago())
                .factura(facturaDto)
                .build();
    }
}
