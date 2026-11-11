package krustyburger.models;

import java.util.Date;

public class Pedido {
    private int id;
    private int clienteId;
    private Date fecha;
    private String estado;
    private double total;
    // Mejora #8: nombre del cliente obtenido mediante JOIN en la consulta
    private String clienteNombre;
    private double descuento;
    private String metodoPago;

    public Pedido() {
    }

    public Pedido(int id, int clienteId, Date fecha, String estado, double total) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
    }

    public Pedido(int id, int clienteId, Date fecha, String estado, double total, double descuento, String metodoPago) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
        this.descuento = descuento;
        this.metodoPago = metodoPago;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}
