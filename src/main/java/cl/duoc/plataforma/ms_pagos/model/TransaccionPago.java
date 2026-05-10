package cl.duoc.plataforma.ms_pagos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════
 * CLASE: TransaccionPago.java
 * Representa la entidad 'transacciones_pago' en la DB.
 * Guarda los registros de los intentos de pago.
 * ═══════════════════════════════════════════════════
 */
@Entity
@Table(name = "transacciones_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referencia lógica al microservicio de Pedidos (db_pedidos).
     * No es FK real para mantener independencia.
     */
    @Column(nullable = false)
    private Long idPedido;

    @Column(nullable = false)
    private Integer monto;

    @Column(nullable = false, length = 50)
    private String metodoPago; // EJ: WEBPAY, TRANSFERENCIA

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @Column(nullable = false, length = 20)
    private String estadoPago; // EJ: APROBADO, RECHAZADO

    /**
     * Relación Uno a Uno (1:1) con Factura.
     * Si se elimina el pago, se elimina la factura (CascadeType.ALL).
     */
    @OneToOne(mappedBy = "transaccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Factura factura;
    
    /**
     * Helper para enlazar la factura bidireccionalmente.
     */
    public void setFactura(Factura factura) {
        if (factura == null) {
            if (this.factura != null) {
                this.factura.setTransaccion(null);
            }
        } else {
            factura.setTransaccion(this);
        }
        this.factura = factura;
    }
}
