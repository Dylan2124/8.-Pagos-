package cl.duoc.plataforma.ms_pagos.controller;

import cl.duoc.plataforma.ms_pagos.dto.TransaccionPagoDto;
import cl.duoc.plataforma.ms_pagos.service.PagoService;
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
public class PagoController {

    private final PagoService pagoService;

    // POST /api/pagos → 201 Created
    // @Valid dispara las validaciones del DTO.
    // Si un campo falla → GlobalExceptionHandler → 400
    @PostMapping
    public ResponseEntity<TransaccionPagoDto> procesarPago(
            @Valid @RequestBody TransaccionPagoDto dto) {
        return ResponseEntity.status(201).body(pagoService.procesarPago(dto));
    }

    // GET /api/pagos/pedido/{idPedido} → 200 OK con lista
    @GetMapping("pedido/{idPedido}")
    public ResponseEntity<List<TransaccionPagoDto>> obtenerPagosPorPedido(
            @PathVariable Long idPedido) {
        return ResponseEntity.ok(pagoService.obtenerPagosPorPedido(idPedido));
    }

    // GET /api/pagos/{idPago} → 200 OK o 404 Not Found con cuerpo descriptivo
    @GetMapping("{idPago}")
    public ResponseEntity<?> obtenerPagoPorId(
            @PathVariable Long idPago) {
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
