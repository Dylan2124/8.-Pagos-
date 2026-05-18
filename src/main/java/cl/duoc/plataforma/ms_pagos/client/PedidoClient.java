package cl.duoc.plataforma.ms_pagos.client;

import cl.duoc.plataforma.ms_pagos.dto.PedidoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-pedido", url = "http://localhost:8086/api/pedidos")
public interface PedidoClient {

    @GetMapping("/{idPedido}")
    PedidoResponseDto obtenerPedidoPorId(@PathVariable("idPedido") Long idPedido);

    @PutMapping("/{idPedido}/estado")
    PedidoResponseDto actualizarEstado(@PathVariable("idPedido") Long idPedido, @RequestParam("estado") String estado);
}
