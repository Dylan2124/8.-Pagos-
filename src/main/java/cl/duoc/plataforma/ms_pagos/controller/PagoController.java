package cl.duoc.plataforma.ms_pagos.controller;

import cl.duoc.plataforma.ms_pagos.dto.TransaccionPagoDto;
import cl.duoc.plataforma.ms_pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════
 * CLASE: PagoController.java
 * Controla los endpoints HTTP del MS Pagos.
 * ═══════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<TransaccionPagoDto> procesarPago(@Valid @RequestBody TransaccionPagoDto dto) {
        TransaccionPagoDto pagoProcesado = pagoService.procesarPago(dto);
        return new ResponseEntity<>(pagoProcesado, HttpStatus.CREATED);
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<List<TransaccionPagoDto>> obtenerPagosPorPedido(@PathVariable Long idPedido) {
        List<TransaccionPagoDto> historial = pagoService.obtenerPagosPorPedido(idPedido);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/{idPago}")
    public ResponseEntity<TransaccionPagoDto> obtenerPagoPorId(@PathVariable Long idPago) {
        TransaccionPagoDto pago = pagoService.obtenerPagoPorId(idPago);
        return ResponseEntity.ok(pago);
    }
}
