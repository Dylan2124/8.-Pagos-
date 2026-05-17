package cl.duoc.plataforma.ms_pagos.service;

import cl.duoc.plataforma.ms_pagos.dto.FacturaDto;
import cl.duoc.plataforma.ms_pagos.dto.TransaccionPagoDto;
import cl.duoc.plataforma.ms_pagos.model.Factura;
import cl.duoc.plataforma.ms_pagos.model.TransaccionPago;
import cl.duoc.plataforma.ms_pagos.repository.TransaccionPagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public TransaccionPagoDto procesarPago(TransaccionPagoDto dto) {
        log.info("Procesando pago para el pedido ID: {} por monto: {}", dto.getIdPedido(), dto.getMonto());

        try {
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

            return mapToDto(saved);
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
