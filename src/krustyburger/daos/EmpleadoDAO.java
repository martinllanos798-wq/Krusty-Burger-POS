
package krustyburger.daos;

import krustyburger.database.Database;
import krustyburger.models.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpleadoDAO {

    /**
     * Login seguro mediante BCrypt.
     */
    public Empleado login(String nombre, String clave) {

        String sql = "SELECT * FROM empleados WHERE nombre = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    // Recuperar el hash almacenado en MySQL
                    String hashGuardado = rs.getString("clave");

                    // Verificar la contraseña mediante BCrypt
                    if (hashGuardado != null &&
                            PasswordUtil.checkPassword(clave, hashGuardado)) {

                        Empleado empleado = new Empleado();

                        empleado.setId(rs.getInt("id"));
                        empleado.setNombre(rs.getString("nombre"));
                        empleado.setPuesto(rs.getString("puesto"));

                        // No guardamos el hash en el objeto empleado
                        return empleado;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al intentar iniciar sesión: " + e.getMessage()
            );
        }

        return null;
    }
}
