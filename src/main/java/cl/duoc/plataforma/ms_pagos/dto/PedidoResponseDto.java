package cl.duoc.plataforma.ms_pagos.dto;

import lombok.Data;

@Data
public class PedidoResponseDto {
    private Long idPedido;
    private Integer idUsuario;
    private String estado;
    private Integer total;
}
