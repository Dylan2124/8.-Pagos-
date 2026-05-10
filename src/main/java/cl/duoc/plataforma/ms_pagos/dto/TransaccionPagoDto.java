package cl.duoc.plataforma.ms_pagos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionPagoDto {

    private Long id;

    @NotNull(message = "El idPedido es obligatorio")
    private Long idPedido;

    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor a 0")
    private Integer monto;

    @NotEmpty(message = "El metodo de pago es obligatorio")
    private String metodoPago;

    private LocalDateTime fechaPago;

    private String estadoPago;

    // Relación opcional en la respuesta
    private FacturaDto factura;
    
    // Campo adicional para simular la creación, el cliente debe mandar su RUT
    @NotEmpty(message = "El rutCliente es obligatorio para facturar")
    private String rutCliente;
}
