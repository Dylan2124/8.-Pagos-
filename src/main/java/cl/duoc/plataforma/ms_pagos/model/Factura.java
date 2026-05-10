package cl.duoc.plataforma.ms_pagos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ═══════════════════════════════════════════════════
 * CLASE: Factura.java
 * Representa la entidad 'facturas' en la DB.
 * ═══════════════════════════════════════════════════
 */
@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    private TransaccionPago transaccion;

    @Column(nullable = false, length = 20)
    private String rutCliente;

    @Column(nullable = false, length = 500)
    private String urlDocumento;
}
