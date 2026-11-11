package krustyburger.daos;

import krustyburger.database.Database;
import krustyburger.models.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public boolean registrarCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, telefono, direccion, correo) VALUES (?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getDireccion());
            ps.setString(4, cliente.getCorreo());

            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mejora #9: actualizar datos de un cliente existente.
     */
    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre=?, telefono=?, direccion=?, correo=? WHERE id=?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getDireccion());
            ps.setString(4, cliente.getCorreo());
            ps.setInt(5, cliente.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mejora #9: eliminar un cliente por ID.
     */
    public boolean eliminarCliente(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombre";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setDireccion(rs.getString("direccion"));
                cliente.setCorreo(rs.getString("correo"));
                lista.add(cliente);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }
}
