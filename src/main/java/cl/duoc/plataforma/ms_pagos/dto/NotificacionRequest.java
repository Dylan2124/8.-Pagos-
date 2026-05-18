package cl.duoc.plataforma.ms_pagos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificacionRequest {
    private Long idUsuario;
    private Long idPedido;
    private String tipo;
    private String mensaje;
}
