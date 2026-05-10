package cl.duoc.plataforma.ms_pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDto {
    private Long id;
    private String rutCliente;
    private String urlDocumento;
}
