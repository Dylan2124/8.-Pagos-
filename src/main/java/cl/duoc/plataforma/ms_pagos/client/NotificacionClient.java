package cl.duoc.plataforma.ms_pagos.client;

import cl.duoc.plataforma.ms_pagos.dto.NotificacionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-notificacion", url = "http://localhost:8090/api/notificaciones")
public interface NotificacionClient {

    @PostMapping("/enviar")
    void enviarNotificacion(@RequestBody NotificacionRequest request);
}
