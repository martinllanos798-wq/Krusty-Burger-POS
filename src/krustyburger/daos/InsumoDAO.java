package krustyburger.daos;

import krustyburger.database.Database;
import krustyburger.models.Insumo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InsumoDAO {

    public List<Insumo> listarInsumos() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Insumo insumo = new Insumo();
                insumo.setId(rs.getInt("id"));
                insumo.setCodigo(rs.getString("codigo"));
                insumo.setNombre(rs.getString("nombre"));
                insumo.setPrecio(rs.getDouble("precio"));
                insumo.setStock(rs.getInt("stock"));
                insumo.setCategoria(rs.getString("categoria"));
                lista.add(insumo);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar insumos: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizarStock(int id, int nuevoStock) {
        String sql = "UPDATE insumos SET stock = ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, nuevoStock);
            ps.setInt(2, id);
            
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }

    public int getInsumosCriticosCount() {
        String sql = "SELECT COUNT(*) FROM insumos WHERE stock < 15";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar insumos críticos: " + e.getMessage());
        }
        return 0;
    }

    public List<Insumo> listarInsumosCriticos() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos WHERE stock < 15";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Insumo insumo = new Insumo();
                insumo.setId(rs.getInt("id"));
                insumo.setCodigo(rs.getString("codigo"));
                insumo.setNombre(rs.getString("nombre"));
                insumo.setPrecio(rs.getDouble("precio"));
                insumo.setStock(rs.getInt("stock"));
                insumo.setCategoria(rs.getString("categoria"));
                lista.add(insumo);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar insumos críticos: " + e.getMessage());
        }
        return lista;
    }
}
