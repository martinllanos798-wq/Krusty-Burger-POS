package krustyburger.daos;

import krustyburger.database.Database;
import krustyburger.models.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductoDAO {

    public List<Producto> listarProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Producto producto = new Producto();
                producto.setCodigo(rs.getString("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setPrecio(rs.getDouble("precio"));
                producto.setCategoria(rs.getString("categoria"));
                lista.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        cargarRecetas(lista);
        return lista;
    }

    private void cargarRecetas(List<Producto> productos) {
        String sql = "SELECT * FROM recetas WHERE producto_codigo = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            InsumoDAO insumoDAO = new InsumoDAO();
            List<krustyburger.models.Insumo> insumos = insumoDAO.listarInsumos();

            for (Producto p : productos) {
                ps.setString(1, p.getCodigo());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int insumoId = rs.getInt("insumo_id");
                        int cantidad = rs.getInt("cantidad");
                        
                        krustyburger.models.Insumo insumoEncontrado = insumos.stream()
                            .filter(i -> i.getId() == insumoId)
                            .findFirst()
                            .orElse(null);
                            
                        if (insumoEncontrado != null) {
                            p.agregarIngrediente(insumoEncontrado, cantidad);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar recetas: " + e.getMessage());
        }
    }

    public boolean registrarProducto(Producto p) {
        String sql = "INSERT INTO productos (codigo, nombre, precio, categoria) VALUES (?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setDouble(3, p.getPrecio());
            ps.setString(4, p.getCategoria());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarProducto(Producto p) {
        String sql = "UPDATE productos SET nombre = ?, precio = ?, categoria = ? WHERE codigo = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getCodigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarProducto(String codigo) {
        String sqlReceta = "DELETE FROM recetas WHERE producto_codigo = ?";
        String sqlProducto = "DELETE FROM productos WHERE codigo = ?";
        
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement ps = con.prepareStatement(sqlReceta)) {
                ps.setString(1, codigo);
                ps.executeUpdate();
            }
            
            boolean exito = false;
            try (PreparedStatement ps = con.prepareStatement(sqlProducto)) {
                ps.setString(1, codigo);
                exito = ps.executeUpdate() > 0;
            }
            
            con.commit();
            return exito;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ignored) {}
        }
    }

    public boolean guardarReceta(String productoCodigo, Map<Integer, Integer> receta) {
        String sqlDelete = "DELETE FROM recetas WHERE producto_codigo = ?";
        String sqlInsert = "INSERT INTO recetas (producto_codigo, insumo_id, cantidad) VALUES (?, ?, ?)";
        
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement psDelete = con.prepareStatement(sqlDelete)) {
                psDelete.setString(1, productoCodigo);
                psDelete.executeUpdate();
            }
            
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                for (Map.Entry<Integer, Integer> entry : receta.entrySet()) {
                    psInsert.setString(1, productoCodigo);
                    psInsert.setInt(2, entry.getKey());
                    psInsert.setInt(3, entry.getValue());
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al guardar receta: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ignored) {}
        }
    }
}
