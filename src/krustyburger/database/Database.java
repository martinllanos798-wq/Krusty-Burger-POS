
package krustyburger.database;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Gestiona un pool de conexiones reutilizables a MySQL.
 * Mejora #3: en lugar de abrir/cerrar una conexión por operación,
 * se mantiene un pool de 5 conexiones que se prestan y devuelven.
 */
public class Database {

    // Cargar configuración privada desde db.properties
    private static final Properties CONFIG = loadConfig();

    private static final String URL = CONFIG.getProperty("db.url");
    private static final String USER = CONFIG.getProperty("db.user");
    private static final String PASSWORD = CONFIG.getProperty("db.password");

    private static final int POOL_SIZE = 5;

    private static final BlockingQueue<Connection> pool =
            new ArrayBlockingQueue<>(POOL_SIZE);

    /**
     * Carga las credenciales desde el archivo privado db.properties.
     */
    private static Properties loadConfig() {
        Properties properties = new Properties();
        Path configPath = Path.of("db.properties");

        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);

            String[] requiredProperties = {
                    "db.url",
                    "db.user",
                    "db.password"
            };

            for (String key : requiredProperties) {
                if (properties.getProperty(key) == null
                        || properties.getProperty(key).isBlank()) {
                    throw new IllegalStateException(
                            "Falta la propiedad '" + key + "' en db.properties"
                    );
                }
            }

            return properties;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo cargar db.properties. Comprueba que está en la carpeta raíz del proyecto.",
                    e
            );
        }
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            for (int i = 0; i < POOL_SIZE; i++) {
                pool.offer(DriverManager.getConnection(URL, USER, PASSWORD));
            }

            System.out.println("[DB] Pool inicializado con " + POOL_SIZE + " conexiones.");

            // Migración auto-sanadora para Fase 2
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

                boolean hasDescuento = false;
                boolean hasMetodoPago = false;

                try (java.sql.ResultSet rs =
                             con.getMetaData().getColumns(null, null, "pedidos", "descuento")) {
                    if (rs.next()) hasDescuento = true;
                }

                try (java.sql.ResultSet rs =
                             con.getMetaData().getColumns(null, null, "pedidos", "metodo_pago")) {
                    if (rs.next()) hasMetodoPago = true;
                }

                try (java.sql.Statement stmt = con.createStatement()) {

                    if (!hasDescuento) {
                        stmt.executeUpdate(
                                "ALTER TABLE pedidos ADD COLUMN descuento DECIMAL(10,2) DEFAULT 0.00"
                        );
                        System.out.println("[DB] Columna descuento agregada a pedidos.");
                    }

                    if (!hasMetodoPago) {
                        stmt.executeUpdate(
                                "ALTER TABLE pedidos ADD COLUMN metodo_pago VARCHAR(50) DEFAULT 'EFECTIVO'"
                        );
                        System.out.println("[DB] Columna metodo_pago agregada a pedidos.");
                    }

                    // Migración auto-sanadora para nuevos insumos
                    stmt.executeUpdate(
                            "INSERT IGNORE INTO insumos (id, codigo, nombre, precio, stock, categoria) VALUES (4, 'QUE01', 'Queso Cheddar', 0.15, 150, 'Lácteos')"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO insumos (id, codigo, nombre, precio, stock, categoria) VALUES (5, 'SAL01', 'Salsa Especial', 0.10, 200, 'Salsas')"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO insumos (id, codigo, nombre, precio, stock, categoria) VALUES (6, 'CEB01', 'Cebollas', 0.05, 120, 'Vegetales')"
                    );

                    // Migración auto-sanadora para nuevos productos
                    stmt.executeUpdate(
                            "INSERT IGNORE INTO productos (codigo, nombre, precio, categoria) VALUES ('KB02', 'Doble Krusty', 7.99, 'Hamburguesas')"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO productos (codigo, nombre, precio, categoria) VALUES ('KB03', 'Krusty Combo', 9.99, 'Hamburguesas')"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO productos (codigo, nombre, precio, categoria) VALUES ('PF02', 'Patatas Deluxe', 3.20, 'Acompañamientos')"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO productos (codigo, nombre, precio, categoria) VALUES ('PF03', 'Aros de Cebolla', 2.80, 'Acompañamientos')"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO productos (codigo, nombre, precio, categoria) VALUES ('BC02', 'Buzz Cola Zero', 1.99, 'Bebidas')"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO productos (codigo, nombre, precio, categoria) VALUES ('BE01', 'Batido Krusty', 3.50, 'Bebidas')"
                    );

                    // Migración auto-sanadora para recetas correspondientes
                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB02', 1, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB02', 2, 2)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB02', 4, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 1, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 2, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 3, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('KB03', 4, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('PF02', 3, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('PF02', 5, 1)"
                    );

                    stmt.executeUpdate(
                            "INSERT IGNORE INTO recetas (producto_codigo, insumo_id, cantidad) VALUES ('PF03', 6, 1)"
                    );

                    System.out.println(
                            "[DB] Migración de nuevos productos, insumos y recetas completada con éxito."
                    );
                }

            } catch (SQLException e) {
                System.err.println(
                        "[DB] Error al aplicar migraciones automáticas: " + e.getMessage()
                );
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Driver de MySQL no encontrado. Añade el JAR a /lib.", e
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error inicializando el pool: " + e.getMessage(), e
            );
        }
    }

    /**
     * Obtiene una conexión del pool.
     * Al llamar a connection.close(), la conexión se devuelve automáticamente
     * al pool (no se cierra realmente).
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection real = pool.poll(3, TimeUnit.SECONDS);

            if (real == null) {
                throw new SQLException(
                        "Tiempo de espera agotado: no hay conexiones disponibles en el pool."
                );
            }

            // Renovar conexión si está caída
            if (real.isClosed() || !real.isValid(1)) {
                real = DriverManager.getConnection(URL, USER, PASSWORD);
            }

            final Connection finalReal = real;

            // Proxy que intercepta close() para devolver al pool en vez de cerrar
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> {

                        if ("close".equals(method.getName())) {

                            // Restaurar autoCommit antes de devolver al pool
                            try {
                                if (!finalReal.getAutoCommit()) {
                                    finalReal.setAutoCommit(true);
                                }
                            } catch (SQLException ignored) {
                            }

                            pool.offer(finalReal);
                            return null;
                        }

                        return method.invoke(finalReal, args);
                    }
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new SQLException(
                    "Hilo interrumpido esperando conexión del pool.", e
            );
        }
    }
}
