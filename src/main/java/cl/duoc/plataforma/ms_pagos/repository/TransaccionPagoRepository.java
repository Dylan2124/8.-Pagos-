package cl.duoc.plataforma.ms_pagos.repository;

import cl.duoc.plataforma.ms_pagos.model.TransaccionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Long> {
    List<TransaccionPago> findByIdPedido(Long idPedido);
}
