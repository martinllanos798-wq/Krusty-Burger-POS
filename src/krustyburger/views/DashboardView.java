package krustyburger.views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardView extends JFrame {

    private JPanel panelCarrito;
    private JPanel panelProductos;
    private JLabel lblTotal;
    private double totalActual = 0.0;

    public DashboardView() {
        // Configuración básica de la ventana
        setTitle("Krusty Burger - Punto de Venta");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Inicializar componentes
        initUI();
    }

    private void initUI() {
        // 1. PANEL LATERAL IZQUIERDO (Navegación)
        JPanel panelNavegacion = new JPanel();
        panelNavegacion.setLayout(new BoxLayout(panelNavegacion, BoxLayout.Y_AXIS));
        panelNavegacion.setPreferredSize(new Dimension(200, 0));
        panelNavegacion.setBackground(new Color(44, 62, 80)); // Color oscuro moderno
        panelNavegacion.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Título de la app en el panel
        JLabel lblTitulo = new JLabel("KRUSTY POS");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelNavegacion.add(lblTitulo);
        panelNavegacion.add(Box.createRigidArea(new Dimension(0, 40)));

        // Botones de navegación
        panelNavegacion.add(crearBotonNavegacion("Ventas"));
        panelNavegacion.add(Box.createRigidArea(new Dimension(0, 15)));
        panelNavegacion.add(crearBotonNavegacion("Inventario"));
        panelNavegacion.add(Box.createRigidArea(new Dimension(0, 15)));
        panelNavegacion.add(crearBotonNavegacion("Historial"));

        // 2. PANEL CENTRAL (Grid de Productos)
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblCategoria = new JLabel("Hamburguesas");
        lblCategoria.setFont(new Font("Segoe UI", Font.BOLD, 28));
        panelCentral.add(lblCategoria, BorderLayout.NORTH);

        panelProductos = new JPanel(new GridLayout(0, 3, 15, 15)); // 3 columnas, espacio 15px
        panelProductos.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Agregar algunos productos de prueba
        panelProductos.add(crearBotonProducto("Krusty Burger", "5.99 €"));
        panelProductos.add(crearBotonProducto("Doble Krusty", "7.99 €"));
        panelProductos.add(crearBotonProducto("Krusty Combo", "9.99 €"));
        panelProductos.add(crearBotonProducto("Papas Fritas", "2.50 €"));
        panelProductos.add(crearBotonProducto("Refresco", "1.50 €"));
        
        JScrollPane scrollProductos = new JScrollPane(panelProductos);
        scrollProductos.setBorder(null);
        panelCentral.add(scrollProductos, BorderLayout.CENTER);

        // 3. PANEL LATERAL DERECHO (Carrito de Compras)
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setPreferredSize(new Dimension(350, 0));
        panelDerecho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(20, 15, 20, 15)
        ));

        JLabel lblCarrito = new JLabel("Orden Actual");
        lblCarrito.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panelDerecho.add(lblCarrito, BorderLayout.NORTH);

        // Lista de items en el carrito
        panelCarrito = new JPanel();
        panelCarrito.setLayout(new BoxLayout(panelCarrito, BoxLayout.Y_AXIS));
        panelCarrito.setBackground(Color.WHITE);
        JScrollPane scrollCarrito = new JScrollPane(panelCarrito);
        scrollCarrito.setBorder(null);
        panelDerecho.add(scrollCarrito, BorderLayout.CENTER);

        // Panel inferior del carrito (Total y Botón de Pago)
        JPanel panelPago = new JPanel(new BorderLayout());
        panelPago.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        lblTotal = new JLabel("Total: 0.00 €");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelPago.add(lblTotal, BorderLayout.NORTH);

        JButton btnCobrar = new JButton("FINALIZAR VENTA");
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnCobrar.setBackground(new Color(46, 204, 113)); // Verde esmeralda
        btnCobrar.setForeground(Color.WHITE);
        btnCobrar.setFocusPainted(false);
        btnCobrar.setPreferredSize(new Dimension(0, 60)); // Botón grande
        btnCobrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover y clic en cobrar
        btnCobrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCobrar.setBackground(new Color(39, 174, 96));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCobrar.setBackground(new Color(46, 204, 113));
            }
        });
        
        btnCobrar.addActionListener(e -> {
            if (totalActual == 0) {
                JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, String.format("Venta por %.2f € completada con éxito.", totalActual), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            panelCarrito.removeAll();
            totalActual = 0;
            actualizarTotal();
            panelCarrito.revalidate();
            panelCarrito.repaint();
        });
        
        panelPago.add(btnCobrar, BorderLayout.SOUTH);
        panelDerecho.add(panelPago, BorderLayout.SOUTH);

        // Ensamblar todo en el JFrame
        add(panelNavegacion, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);
    }

    // Helper: Crea botones del menú lateral
    private JButton crearBotonNavegacion(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(180, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Helper: Crea botones de producto tipo tarjeta
    private JButton crearBotonProducto(String nombre, String precioStr) {
        JButton btn = new JButton("<html><center><h3 style='margin:0; padding:10px 0 5px 0;'>" + nombre + "</h3><p style='color:#27ae60; font-size:14px; font-weight:bold; margin:0; padding-bottom:10px;'>" + precioStr + "</p></center></html>");
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto Hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(245, 245, 245));
                btn.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            }
        });
        
        // Extraer el valor numérico del precio
        double precio = Double.parseDouble(precioStr.replace(" €", "").replace("€", "").replace("$", "").trim());
        
        // Simular agregar al carrito
        btn.addActionListener(e -> agregarAlCarrito(nombre, precio));
        
        return btn;
    }

    // Helper: Simula agregar un item al panel lateral derecho
    private void agregarAlCarrito(String nombre, double precio) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
            new EmptyBorder(10, 5, 10, 5)
        ));
        item.setBackground(Color.WHITE);
        
        JLabel lblNombre = new JLabel("1x " + nombre);
        lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblPrecio = new JLabel(String.format("%.2f €", precio));
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        item.add(lblNombre, BorderLayout.WEST);
        item.add(lblPrecio, BorderLayout.EAST);
        
        panelCarrito.add(item);
        panelCarrito.revalidate();
        panelCarrito.repaint();
        
        // Actualizar total
        totalActual += precio;
        actualizarTotal();
    }
    
    private void actualizarTotal() {
        lblTotal.setText(String.format("Total: %.2f €", totalActual));
    }
}
