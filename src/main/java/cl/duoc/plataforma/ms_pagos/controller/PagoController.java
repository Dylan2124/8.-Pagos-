package cl.duoc.plataforma.ms_pagos.controller;

import cl.duoc.plataforma.ms_pagos.dto.TransaccionPagoDto;
import cl.duoc.plataforma.ms_pagos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * * ═══════════════════════════════════════════════════
 * * CLASE: PagoController.java
 * * CAMBIOS RESPECTO A INICIO EA2:
 * *   1. No importa la entidad TransaccionPago en ningún lugar.
 * *   2. POST recibe @Valid TransaccionPagoDto.
 * *      Si la validación falla → GlobalExceptionHandler
 * *      devuelve 400 con mapa { campo: mensaje }.
 * *   3. Todos los métodos devuelven TransaccionPagoDto.
 * * ═══════════════════════════════════════════════════*/
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Gestión del procesamiento de pagos, emisión de facturas y notificaciones de transacciones.")
public class PagoController {

    private final PagoService pagoService;

    // POST /api/pagos → 201 Created
    // @Valid dispara las validaciones del DTO.
    // Si un campo falla → GlobalExceptionHandler → 400
    @PostMapping
    @Operation(summary = "Procesar una transacción de pago", description = "Registra un pago para un pedido, valida el monto contra el total del pedido en ms-pedidos, emite factura si el RUT es provisto y notifica el resultado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transacción procesada correctamente (puede ser APROBADO o RECHAZADO)", 
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransaccionPagoDto.class))),
        @ApiResponse(responseCode = "400", description = "Monto insuficiente, el pedido no existe o datos inválidos")
    })
    public ResponseEntity<TransaccionPagoDto> procesarPago(
            @Valid @RequestBody @Parameter(description = "DTO con la información del pago") TransaccionPagoDto dto) {
        return ResponseEntity.status(201).body(pagoService.procesarPago(dto));
    }

    // GET /api/pagos/pedido/{idPedido} → 200 OK con lista
    @GetMapping("pedido/{idPedido}")
    @Operation(summary = "Obtener pagos por ID de pedido", description = "Retorna el historial de pagos asociados a un pedido específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial de pagos obtenido exitosamente")
    })
    public ResponseEntity<List<TransaccionPagoDto>> obtenerPagosPorPedido(
            @PathVariable @Parameter(description = "ID del pedido a consultar", example = "1") Long idPedido) {
        return ResponseEntity.ok(pagoService.obtenerPagosPorPedido(idPedido));
    }

    // GET /api/pagos/{idPago} → 200 OK o 404 Not Found con cuerpo descriptivo
    @GetMapping("{idPago}")
    @Operation(summary = "Obtener pago por ID", description = "Obtiene los detalles de una transacción de pago específica por su ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transacción de pago encontrada y retornada", 
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransaccionPagoDto.class))),
        @ApiResponse(responseCode = "404", description = "La transacción de pago con el ID especificado no existe")
    })
    public ResponseEntity<?> obtenerPagoPorId(
            @PathVariable @Parameter(description = "ID del pago", example = "10") Long idPago) {
        return pagoService.obtenerPagoPorId(idPago)
                .map(dto -> ResponseEntity.ok((Object) dto))
                .orElseGet(() -> {
                    java.util.Map<String, Object> response = new java.util.HashMap<>();
                    response.put("status", 404);
                    response.put("timestamp", java.time.LocalDateTime.now());
                    response.put("error", "El pago con el ID especificado no existe");
                    return ResponseEntity.status(404).body(response);
                });
    }
}
