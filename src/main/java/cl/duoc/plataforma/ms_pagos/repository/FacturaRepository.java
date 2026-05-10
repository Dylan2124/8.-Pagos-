package cl.duoc.plataforma.ms_pagos.repository;

import cl.duoc.plataforma.ms_pagos.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
}
