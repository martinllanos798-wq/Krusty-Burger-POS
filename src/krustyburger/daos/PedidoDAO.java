package krustyburger.daos;

import krustyburger.database.Database;
import krustyburger.models.DetallePedido;
import krustyburger.models.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PedidoDAO {

    /**
     * Mejora #4: registra la venta Y descuenta el stock en la MISMA transacción SQL.
     * Si cualquier paso falla, se hace rollback de todo.
     *
     * @param pedido          cabecera del pedido
     * @param detalles        líneas del pedido
     * @param stockDescuentos mapa insumoId -> cantidad a descontar (calculado y validado previamente)
     */
    public boolean registrarVenta(Pedido pedido, List<DetallePedido> detalles, Map<Integer, Integer> stockDescuentos) {
        String sqlPedido  = "INSERT INTO pedidos (cliente_id, estado, total, descuento, metodo_pago) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        String sqlStock   = "UPDATE insumos SET stock = stock - ? WHERE id = ?";

        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            // 1. Insertar cabecera del pedido
            int idPedido = 0;
            try (PreparedStatement psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                if (pedido.getClienteId() > 0) {
                    psPedido.setInt(1, pedido.getClienteId());
                } else {
                    psPedido.setNull(1, Types.INTEGER); // Mejora #14: NULL para "Público General"
                }
                psPedido.setString(2, pedido.getEstado());
                psPedido.setDouble(3, pedido.getTotal());
                psPedido.setDouble(4, pedido.getDescuento());
                psPedido.setString(5, pedido.getMetodoPago() != null ? pedido.getMetodoPago() : "EFECTIVO");
                psPedido.executeUpdate();

                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    if (rs.next()) idPedido = rs.getInt(1);
                }
            }

            // 2. Insertar detalle del pedido
            if (idPedido > 0) {
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    for (DetallePedido detalle : detalles) {
                        psDetalle.setInt(1, idPedido);
                        psDetalle.setString(2, detalle.getProductoId());
                        psDetalle.setInt(3, detalle.getCantidad());
                        psDetalle.setDouble(4, detalle.getPrecioUnitario());
                        psDetalle.addBatch();
                    }
                    psDetalle.executeBatch();
                }

                // 3. Mejora #4: descontar stock en la misma transacción
                if (stockDescuentos != null && !stockDescuentos.isEmpty()) {
                    try (PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                        for (Map.Entry<Integer, Integer> entry : stockDescuentos.entrySet()) {
                            psStock.setInt(1, entry.getValue()); // cantidad a descontar
                            psStock.setInt(2, entry.getKey());   // insumo id
                            psStock.addBatch();
                        }
                        psStock.executeBatch();
                    }
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar la venta: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException e) { /* ignorar */ }
        }
    }

    /**
     * Mejora #8: lista pedidos con nombre del cliente mediante JOIN.
     */
    public List<Pedido> listarPedidos() {
        return listarPedidosFiltrados(null, null, null);
    }

    /**
     * Mejora #10: lista pedidos con filtros opcionales de fecha y estado.
     *
     * @param desde  fecha inicial (puede ser null)
     * @param hasta  fecha final (puede ser null)
     * @param estado "PAGADO", "PENDIENTE", o null/vacío para todos
     */
    public List<Pedido> listarPedidosFiltrados(Date desde, Date hasta, String estado) {
        List<Pedido> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.cliente_id, p.fecha, p.estado, p.total, p.descuento, p.metodo_pago, " +
            "COALESCE(c.nombre, 'Público General') AS cliente_nombre " +
            "FROM pedidos p LEFT JOIN clientes c ON p.cliente_id = c.id WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (desde != null) {
            sql.append(" AND p.fecha >= ?");
            params.add(new Timestamp(desde.getTime()));
        }
        if (hasta != null) {
            sql.append(" AND p.fecha <= ?");
            params.add(new Timestamp(hasta.getTime()));
        }
        if (estado != null && !estado.isEmpty() && !estado.equalsIgnoreCase("TODOS")) {
            sql.append(" AND p.estado = ?");
            params.add(estado);
        }

        sql.append(" ORDER BY p.fecha DESC");

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido p = new Pedido();
                    p.setId(rs.getInt("id"));
                    p.setClienteId(rs.getInt("cliente_id"));
                    p.setFecha(rs.getTimestamp("fecha"));
                    p.setEstado(rs.getString("estado"));
                    p.setTotal(rs.getDouble("total"));
                    p.setClienteNombre(rs.getString("cliente_nombre")); // Mejora #8
                    p.setDescuento(rs.getDouble("descuento"));
                    p.setMetodoPago(rs.getString("metodo_pago"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar los pedidos: " + e.getMessage());
        }

        return lista;
    }

    public double getVentasDeHoy() {
        String sql = "SELECT SUM(total) FROM pedidos WHERE DATE(fecha) = CURDATE() AND estado = 'PAGADO'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas de hoy: " + e.getMessage());
        }
        return 0.0;
    }

    public int getContadorPedidosDeHoy() {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE DATE(fecha) = CURDATE()";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar pedidos de hoy: " + e.getMessage());
        }
        return 0;
    }

    public double[] getVentasSemanales() {
        double[] ventas = new double[7]; // Lun(0) a Dom(6)
        String sql = "SELECT WEEKDAY(fecha) AS dia, SUM(total) " +
                     "FROM pedidos " +
                     "WHERE WEEK(fecha, 1) = WEEK(CURDATE(), 1) " +
                     "AND YEAR(fecha) = YEAR(CURDATE()) " +
                     "AND estado = 'PAGADO' " +
                     "GROUP BY WEEKDAY(fecha)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int dia = rs.getInt("dia"); // 0 a 6
                double total = rs.getDouble(2);
                if (dia >= 0 && dia < 7) {
                    ventas[dia] = total;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas semanales: " + e.getMessage());
        }
        return ventas;
    }
}
