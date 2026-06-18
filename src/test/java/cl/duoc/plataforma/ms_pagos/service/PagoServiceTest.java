package cl.duoc.plataforma.ms_pagos.service;

import cl.duoc.plataforma.ms_pagos.client.NotificacionClient;
import cl.duoc.plataforma.ms_pagos.client.PedidoClient;
import cl.duoc.plataforma.ms_pagos.dto.NotificacionRequest;
import cl.duoc.plataforma.ms_pagos.dto.PedidoResponseDto;
import cl.duoc.plataforma.ms_pagos.dto.TransaccionPagoDto;
import cl.duoc.plataforma.ms_pagos.model.Factura;
import cl.duoc.plataforma.ms_pagos.model.TransaccionPago;
import cl.duoc.plataforma.ms_pagos.repository.TransaccionPagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private TransaccionPagoRepository transaccionRepository;

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private NotificacionClient notificacionClient;

    @InjectMocks
    private PagoService pagoService;

    // ── PRUEBAS: procesarPago ───────────────────────────────────────────────

    @Test
    void testProcesarPago_Success_ApprovedWithRut_ShouldGenerateFacturaAndNotify() {
        // Dado
        TransaccionPagoDto requestDto = TransaccionPagoDto.builder()
                .idPedido(1L)
                .monto(10000)
                .metodoPago("WEBPAY")
                .rutCliente("12.345.678-9")
                .build();

        PedidoResponseDto pedidoResponse = new PedidoResponseDto();
        pedidoResponse.setIdPedido(1L);
        pedidoResponse.setTotal(10000);

        TransaccionPago savedTransaccion = new TransaccionPago();
        savedTransaccion.setId(100L);
        savedTransaccion.setIdPedido(1L);
        savedTransaccion.setMonto(10000);
        savedTransaccion.setMetodoPago("WEBPAY");
        savedTransaccion.setFechaPago(LocalDateTime.now());
        savedTransaccion.setEstadoPago("APROBADO");

        Factura factura = new Factura();
        factura.setId(50L);
        factura.setRutCliente("12.345.678-9");
        factura.setUrlDocumento("https://storage.plataforma.com/facturas/1.pdf");
        savedTransaccion.setFactura(factura);

        when(pedidoClient.obtenerPedidoPorId(1L)).thenReturn(pedidoResponse);
        when(transaccionRepository.save(any(TransaccionPago.class))).thenReturn(savedTransaccion);

        // Cuando
        TransaccionPagoDto response = pagoService.procesarPago(requestDto);

        // Entonces
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("APROBADO", response.getEstadoPago());
        assertNotNull(response.getFactura());
        assertEquals("12.345.678-9", response.getFactura().getRutCliente());
        
        verify(pedidoClient, times(1)).obtenerPedidoPorId(1L);
        verify(pedidoClient, times(1)).actualizarEstado(1L, "PAGADO");
        verify(notificacionClient, times(1)).enviarNotificacion(any(NotificacionRequest.class));
        verify(transaccionRepository, times(1)).save(any(TransaccionPago.class));
    }

    @Test
    void testProcesarPago_Success_ApprovedWithoutRut_ShouldNotGenerateFactura() {
        // Dado
        TransaccionPagoDto requestDto = TransaccionPagoDto.builder()
                .idPedido(1L)
                .monto(10000)
                .metodoPago("WEBPAY")
                .rutCliente(null)
                .build();

        PedidoResponseDto pedidoResponse = new PedidoResponseDto();
        pedidoResponse.setIdPedido(1L);
        pedidoResponse.setTotal(10000);

        TransaccionPago savedTransaccion = new TransaccionPago();
        savedTransaccion.setId(100L);
        savedTransaccion.setIdPedido(1L);
        savedTransaccion.setMonto(10000);
        savedTransaccion.setMetodoPago("WEBPAY");
        savedTransaccion.setFechaPago(LocalDateTime.now());
        savedTransaccion.setEstadoPago("APROBADO");
        savedTransaccion.setFactura(null);

        when(pedidoClient.obtenerPedidoPorId(1L)).thenReturn(pedidoResponse);
        when(transaccionRepository.save(any(TransaccionPago.class))).thenReturn(savedTransaccion);

        // Cuando
        TransaccionPagoDto response = pagoService.procesarPago(requestDto);

        // Entonces
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("APROBADO", response.getEstadoPago());
        assertNull(response.getFactura());

        verify(pedidoClient, times(1)).actualizarEstado(1L, "PAGADO");
        verify(notificacionClient, times(1)).enviarNotificacion(any(NotificacionRequest.class));
    }

    @Test
    void testProcesarPago_Rejected_AmountZeroOrLess_ShouldNotGenerateFacturaOrNotify() {
        // Dado
        TransaccionPagoDto requestDto = TransaccionPagoDto.builder()
                .idPedido(1L)
                .monto(0)
                .metodoPago("WEBPAY")
                .rutCliente("12.345.678-9")
                .build();

        PedidoResponseDto pedidoResponse = new PedidoResponseDto();
        pedidoResponse.setIdPedido(1L);
        pedidoResponse.setTotal(0);

        TransaccionPago savedTransaccion = new TransaccionPago();
        savedTransaccion.setId(100L);
        savedTransaccion.setIdPedido(1L);
        savedTransaccion.setMonto(0);
        savedTransaccion.setMetodoPago("WEBPAY");
        savedTransaccion.setFechaPago(LocalDateTime.now());
        savedTransaccion.setEstadoPago("RECHAZADO");

        when(pedidoClient.obtenerPedidoPorId(1L)).thenReturn(pedidoResponse);
        when(transaccionRepository.save(any(TransaccionPago.class))).thenReturn(savedTransaccion);

        // Cuando
        TransaccionPagoDto response = pagoService.procesarPago(requestDto);

        // Entonces
        assertNotNull(response);
        assertEquals("RECHAZADO", response.getEstadoPago());
        assertNull(response.getFactura());

        verify(pedidoClient, never()).actualizarEstado(anyLong(), anyString());
        verify(notificacionClient, never()).enviarNotificacion(any(NotificacionRequest.class));
    }

    @Test
    void testProcesarPago_Failed_OrderNotFound_ShouldThrowException() {
        // Dado
        TransaccionPagoDto requestDto = TransaccionPagoDto.builder()
                .idPedido(1L)
                .monto(10000)
                .build();

        when(pedidoClient.obtenerPedidoPorId(1L)).thenReturn(null);

        // Cuando y Entonces
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pagoService.procesarPago(requestDto);
        });

        assertTrue(exception.getMessage().contains("El pedido no existe en el sistema"));
        verify(transaccionRepository, never()).save(any(TransaccionPago.class));
    }

    @Test
    void testProcesarPago_Failed_InsufficientAmount_ShouldThrowException() {
        // Dado
        TransaccionPagoDto requestDto = TransaccionPagoDto.builder()
                .idPedido(1L)
                .monto(5000) // Monto insuficiente (total del pedido es 10000)
                .build();

        PedidoResponseDto pedidoResponse = new PedidoResponseDto();
        pedidoResponse.setIdPedido(1L);
        pedidoResponse.setTotal(10000);

        when(pedidoClient.obtenerPedidoPorId(1L)).thenReturn(pedidoResponse);

        // Cuando y Entonces
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pagoService.procesarPago(requestDto);
        });

        assertTrue(exception.getMessage().contains("insuficiente"));
        verify(transaccionRepository, never()).save(any(TransaccionPago.class));
    }

    @Test
    void testProcesarPago_ClientExceptionGraceful_ShouldContinueAndSucceed() {
        // Dado
        TransaccionPagoDto requestDto = TransaccionPagoDto.builder()
                .idPedido(1L)
                .monto(10000)
                .rutCliente("12.345.678-9")
                .build();

        // Simula fallo de red / timeout en pedidoClient
        when(pedidoClient.obtenerPedidoPorId(1L)).thenThrow(new RuntimeException("Connection timeout"));

        TransaccionPago savedTransaccion = new TransaccionPago();
        savedTransaccion.setId(100L);
        savedTransaccion.setIdPedido(1L);
        savedTransaccion.setMonto(10000);
        savedTransaccion.setEstadoPago("APROBADO");

        Factura factura = new Factura();
        factura.setId(50L);
        factura.setRutCliente("12.345.678-9");
        savedTransaccion.setFactura(factura);

        when(transaccionRepository.save(any(TransaccionPago.class))).thenReturn(savedTransaccion);

        // Fallo al actualizar estado y al notificar (se simula servicio caído)
        doThrow(new RuntimeException("Feign client error")).when(pedidoClient).actualizarEstado(1L, "PAGADO");
        doThrow(new RuntimeException("Notification service offline")).when(notificacionClient).enviarNotificacion(any(NotificacionRequest.class));

        // Cuando
        TransaccionPagoDto response = pagoService.procesarPago(requestDto);

        // Entonces
        assertNotNull(response);
        assertEquals("APROBADO", response.getEstadoPago());
        assertNotNull(response.getFactura());
        verify(transaccionRepository, times(1)).save(any(TransaccionPago.class));
    }

    @Test
    void testProcesarPago_CustomBusinessExceptionPropagated_ShouldRethrow() {
        // Dado
        TransaccionPagoDto requestDto = TransaccionPagoDto.builder()
                .idPedido(1L)
                .monto(10000)
                .build();

        // Simula la propagación de una excepción con mensaje de negocio
        when(pedidoClient.obtenerPedidoPorId(1L)).thenThrow(new RuntimeException("El pedido no existe en el sistema."));

        // Cuando y Entonces
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pagoService.procesarPago(requestDto);
        });

        assertTrue(exception.getMessage().contains("El pedido no existe"));
    }

    // ── PRUEBAS: obtenerPagosPorPedido y obtenerPagoPorId ────────────────────

    @Test
    void testObtenerPagosPorPedido_ShouldReturnList() {
        // Dado
        TransaccionPago trans1 = new TransaccionPago();
        trans1.setId(1L);
        trans1.setIdPedido(100L);
        trans1.setMonto(5000);

        TransaccionPago trans2 = new TransaccionPago();
        trans2.setId(2L);
        trans2.setIdPedido(100L);
        trans2.setMonto(5000);

        when(transaccionRepository.findByIdPedido(100L)).thenReturn(Arrays.asList(trans1, trans2));

        // Cuando
        List<TransaccionPagoDto> results = pagoService.obtenerPagosPorPedido(100L);

        // Entonces
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }

    @Test
    void testObtenerPagoPorId_Found_ShouldReturnDto() {
        // Dado
        TransaccionPago trans = new TransaccionPago();
        trans.setId(10L);
        trans.setIdPedido(100L);
        trans.setMonto(5000);

        when(transaccionRepository.findById(10L)).thenReturn(Optional.of(trans));

        // Cuando
        Optional<TransaccionPagoDto> result = pagoService.obtenerPagoPorId(10L);

        // Entonces
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void testObtenerPagoPorId_NotFound_ShouldReturnEmpty() {
        // Dado
        when(transaccionRepository.findById(10L)).thenReturn(Optional.empty());

        // Cuando
        Optional<TransaccionPagoDto> result = pagoService.obtenerPagoPorId(10L);

        // Entonces
        assertFalse(result.isPresent());
    }
}
