package krustyburger.views;

import krustyburger.daos.ClienteDAO;
import krustyburger.daos.InsumoDAO;
import krustyburger.daos.PedidoDAO;
import krustyburger.daos.ProductoDAO;
import krustyburger.models.*;
import krustyburger.exceptions.StockInsuficienteException;
import krustyburger.services.VentaService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class MainMenuView extends JFrame {

    private Empleado empleadoActivo;

    // Paneles de CardLayout
    private JPanel panelDashboard;
    private JPanel panelVentas;
    private JPanel panelAlmacen;
    private JPanel panelClientes;
    private JPanel panelVentasTotales;
    private JPanel panelAdminProductos; // Gestión de productos y recetas
    
    private JPanel cardPanel; 
    private CardLayout cardLayout;

    // DAOs y Servicios
    private ProductoDAO productoDAO;
    private PedidoDAO pedidoDAO;
    private InsumoDAO insumoDAO;
    private ClienteDAO clienteDAO;
    private VentaService ventaService;

    // Componentes del Header
    private JButton btnTheme;
    private JButton btnAlertasCampana;

    // Componentes de Dashboard
    private JLabel lblKpiIngresos;
    private JLabel lblKpiPedidos;
    private JLabel lblKpiStockCritico;
    private SalesChartPanel salesChart;

    // Componentes de Ventas / Carrito
    private JComboBox<Cliente> cbClientesVenta;
    private JPanel panelProductosGrid;
    private JPanel panelCarrito;
    private JLabel lblSubtotalVentaActual;
    private JLabel lblDescuentoVentaActual;
    private JLabel lblTotalVentaActual;
    private JTextField txtCupoDescuento;
    private double subtotalVentaActual = 0.0;
    private double porcentajeDescuento = 0.0;
    private double totalVentaActual = 0.0;
    private Map<String, Object[]> carritoMap; // codigo -> [Producto, Cantidad(Integer), JPanel(Item)]
    private List<JButton> listaBotonesProductos; // Para filtrado interactivo
    private String categoriaFiltroActual = "Todos";
    private String queryBusquedaActual = "";

    // Componentes de Almacén
    private JTable tablaInsumos;
    private DefaultTableModel modeloInsumos;
    private JTextField txtIdInsumo;
    private JTextField txtNuevoStock;

    // Componentes de Clientes
    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;
    private JTextField txtNombreCliente;
    private JTextField txtTelefonoCliente;
    private JTextField txtDireccionCliente;
    private JTextField txtCorreoCliente;
    private JButton btnGuardarCliente;
    private JButton btnActualizarCliente;
    private JButton btnEliminarCliente;
    private int clienteSeleccionadoId = -1;

    // Componentes de Ventas Totales
    private JTable tablaHistorialVentas;
    private DefaultTableModel modeloHistorialVentas;
    private JLabel lblGananciaTotal;
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JComboBox<String> cbEstadoFiltro;

    // Componentes de Gestión de Productos y Recetas (Administrador)
    private JTable tablaAdminProductos;
    private DefaultTableModel modeloAdminProductos;
    private JTextField txtAdminCodigo;
    private JTextField txtAdminNombre;
    private JTextField txtAdminPrecio;
    private JComboBox<String> cbAdminCategoria;
    private JTable tablaRecetaIngredientes;
    private DefaultTableModel modeloRecetaIngredientes;
    private JComboBox<Insumo> cbRecetaInsumos;
    private JTextField txtRecetaCantidad;
    private Map<Integer, Integer> recetaEditandoMap; // insumoId -> cantidad

    // Elementos de Navegación del Sidebar
    private JPanel menuPanel;
    private List<JButton> botonesSidebar;

    public MainMenuView(Empleado empleado) {
        this.empleadoActivo = empleado;
        
        this.productoDAO = new ProductoDAO();
        this.pedidoDAO = new PedidoDAO();
        this.insumoDAO = new InsumoDAO();
        this.clienteDAO = new ClienteDAO();
        this.ventaService = new VentaService();
        this.carritoMap = new HashMap<>();
        this.listaBotonesProductos = new ArrayList<>();
        this.recetaEditandoMap = new HashMap<>();
        this.botonesSidebar = new ArrayList<>();
        
        initUI();
    }

    private void initUI() {
        setTitle("KrustyBurger POS - Sistema de Gestión e Inventario");
        setSize(1200, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PANEL SUPERIOR (Header Premium) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        headerPanel.setBackground(new Color(255, 204, 51));

        // Bienvenida
        JLabel lblInfo = new JLabel("KRUSTY POS  |  Bienvenido, " + empleadoActivo.getNombre() + " (" + empleadoActivo.getPuesto().toUpperCase() + ")");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInfo.setForeground(new Color(44, 62, 80));
        headerPanel.add(lblInfo, BorderLayout.WEST);

        // Controles derechos
        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeaderPanel.setOpaque(false);

        // Campana de Alertas de Stock
        btnAlertasCampana = new JButton("Alertas: 0");
        btnAlertasCampana.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAlertasCampana.setBackground(new Color(231, 76, 60));
        btnAlertasCampana.setForeground(Color.WHITE);
        btnAlertasCampana.setFocusPainted(false);
        btnAlertasCampana.addActionListener(e -> mostrarDialogoAlertasStock());
        rightHeaderPanel.add(btnAlertasCampana);

        // Toggle de Tema (Claro / Oscuro)
        btnTheme = new JButton("Tema Oscuro");
        btnTheme.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTheme.setFocusPainted(false);
        btnTheme.addActionListener(e -> toggleTheme());
        rightHeaderPanel.add(btnTheme);

        // Botón Salir
        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalir.setBackground(new Color(44, 62, 80));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });
        rightHeaderPanel.add(btnSalir);

        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- PANEL IZQUIERDO (Sidebar de Navegación) ---
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        menuPanel.setBackground(new Color(245, 247, 250));

        JButton btnModuloDashboard = crearBotonSidebar("Dashboard", "Dashboard");
        JButton btnModuloVentas = crearBotonSidebar("Ventas / Pedidos", "Ventas");
        JButton btnModuloAlmacen = crearBotonSidebar("Almacén / Insumos", "Almacen");
        JButton btnModuloClientes = crearBotonSidebar("Clientes", "Clientes");
        JButton btnModuloHistorial = crearBotonSidebar("Registro de Ventas", "Historial");
        JButton btnModuloAdmin = crearBotonSidebar("Productos & Recetas", "ProductosAdmin");

        menuPanel.add(btnModuloDashboard);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(btnModuloVentas);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(btnModuloAlmacen);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(btnModuloClientes);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(btnModuloHistorial);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(btnModuloAdmin);

        add(menuPanel, BorderLayout.WEST);

        // --- CONTROL DE ACCESO ---
        if (empleadoActivo.getPuesto().equalsIgnoreCase("camarero")) {
            btnModuloAlmacen.setEnabled(false);
            btnModuloAlmacen.setVisible(false);
            btnModuloClientes.setEnabled(false);
            btnModuloClientes.setVisible(false);
            btnModuloHistorial.setEnabled(false);
            btnModuloHistorial.setVisible(false);
            btnModuloAdmin.setEnabled(false);
            btnModuloAdmin.setVisible(false);
        }

        // --- PANEL CENTRAL (CardLayout) ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        crearPanelDashboard();
        crearPanelVentas();
        crearPanelAlmacen();
        crearPanelClientes();
        crearPanelVentasTotales();
        crearPanelAdminProductos();

        cardPanel.add(panelDashboard, "Dashboard");
        cardPanel.add(panelVentas, "Ventas");
        cardPanel.add(panelAlmacen, "Almacen");
        cardPanel.add(panelClientes, "Clientes");
        cardPanel.add(panelVentasTotales, "Historial");
        cardPanel.add(panelAdminProductos, "ProductosAdmin");

        add(cardPanel, BorderLayout.CENTER);

        // Eventos Sidebar
        btnModuloDashboard.addActionListener(e -> activarBotonSidebar(btnModuloDashboard, "Dashboard"));
        btnModuloVentas.addActionListener(e -> activarBotonSidebar(btnModuloVentas, "Ventas"));
        btnModuloAlmacen.addActionListener(e -> activarBotonSidebar(btnModuloAlmacen, "Almacen"));
        btnModuloClientes.addActionListener(e -> activarBotonSidebar(btnModuloClientes, "Clientes"));
        btnModuloHistorial.addActionListener(e -> activarBotonSidebar(btnModuloHistorial, "Historial"));
        btnModuloAdmin.addActionListener(e -> activarBotonSidebar(btnModuloAdmin, "ProductosAdmin"));

        // Seleccionar inicial
        activarBotonSidebar(btnModuloDashboard, "Dashboard");
        actualizarAlertasCampana();
    }

    private JButton crearBotonSidebar(String texto, String cardName) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setMaximumSize(new Dimension(190, 42));
        btn.setPreferredSize(new Dimension(190, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(64, 78, 103));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("cardName", cardName);
        botonesSidebar.add(btn);
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(new Color(255, 204, 51))) {
                    btn.setBackground(new Color(240, 242, 245));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(new Color(255, 204, 51))) {
                    btn.setBackground(Color.WHITE);
                }
            }
        });
        
        return btn;
    }

    private void activarBotonSidebar(JButton botonActivo, String cardName) {
        for (JButton btn : botonesSidebar) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(64, 78, 103));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
            ));
        }
        botonActivo.setBackground(new Color(255, 204, 51));
        botonActivo.setForeground(new Color(44, 62, 80));
        botonActivo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 179, 35), 2),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        // Cargar datos según la pestaña
        if ("Dashboard".equals(cardName)) {
            cargarDatosDashboard();
        } else if ("Ventas".equals(cardName)) {
            cargarClientesEnCombo();
            cargarProductosGrid();
        } else if ("Almacen".equals(cardName)) {
            cargarInsumos();
        } else if ("Clientes".equals(cardName)) {
            cargarClientes();
        } else if ("Historial".equals(cardName)) {
            cargarHistorialVentas();
        } else if ("ProductosAdmin".equals(cardName)) {
            cargarAdminProductos();
            cargarAdminInsumosCombo();
        }

        cardLayout.show(cardPanel, cardName);
        actualizarAlertasCampana();
    }

    private void toggleTheme() {
        try {
            if (UIManager.getLookAndFeel().getClass().getName().contains("Dark")) {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                btnTheme.setText("Tema Oscuro");
                menuPanel.setBackground(new Color(245, 247, 250));
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                btnTheme.setText("Tema Claro");
                menuPanel.setBackground(new Color(30, 30, 30));
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void actualizarAlertasCampana() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return insumoDAO.getInsumosCriticosCount();
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    btnAlertasCampana.setText("Alertas: " + count);
                    if (count > 0) {
                        btnAlertasCampana.setBackground(new Color(231, 76, 60)); // Rojo
                        btnAlertasCampana.setVisible(true);
                    } else {
                        btnAlertasCampana.setBackground(new Color(46, 204, 113)); // Verde
                        if (empleadoActivo.getPuesto().equalsIgnoreCase("camarero")) {
                            btnAlertasCampana.setVisible(false); // Ocultar para camareros si no hay alertas
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void mostrarDialogoAlertasStock() {
        new SwingWorker<List<Insumo>, Void>() {
            @Override
            protected List<Insumo> doInBackground() throws Exception {
                return insumoDAO.listarInsumosCriticos();
            }

            @Override
            protected void done() {
                try {
                    List<Insumo> criticos = get();
                    if (criticos.isEmpty()) {
                        JOptionPane.showMessageDialog(MainMenuView.this, "¡Excelente! Todos los insumos tienen stock óptimo.", "Inventario Óptimo", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("INSUMOS EN ESTADO CRÍTICO (< 15 unidades):\n\n");
                    for (Insumo i : criticos) {
                        sb.append(String.format("• ID %d: %s | Stock Actual: %d unidades\n", i.getId(), i.getNombre(), i.getStock()));
                    }
                    sb.append("\nPor favor, reabastezca estos ingredientes en el módulo de Almacén.");
                    
                    JOptionPane.showMessageDialog(MainMenuView.this, sb.toString(), "Alerta de Stock Crítico", JOptionPane.WARNING_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // ==========================================
    // 1. MÓDULO DE DASHBOARD (NUEVO)
    // ==========================================
    private void crearPanelDashboard() {
        panelDashboard = new JPanel(new BorderLayout());
        panelDashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Panel de Control General");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        panelDashboard.add(lblTitulo, BorderLayout.NORTH);

        // Panel de Tarjetas KPI
        JPanel panelKpis = new JPanel(new GridLayout(1, 3, 20, 0));
        panelKpis.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        lblKpiIngresos = crearKpiCard(panelKpis, "INGRESOS DE HOY", "0.00 €", new Color(46, 204, 113));
        lblKpiPedidos = crearKpiCard(panelKpis, "PEDIDOS DE HOY", "0", new Color(52, 152, 219));
        lblKpiStockCritico = crearKpiCard(panelKpis, "ALERTAS DE STOCK", "0 Insumos", new Color(231, 76, 60));

        panelDashboard.add(panelKpis, BorderLayout.CENTER);

        // Panel inferior para Gráficos
        JPanel panelGrafico = new JPanel(new BorderLayout());
        panelGrafico.setBorder(BorderFactory.createTitledBorder("Rendimiento de Ventas de la Semana"));
        
        salesChart = new SalesChartPanel();
        panelGrafico.add(salesChart, BorderLayout.CENTER);
        
        panelDashboard.add(panelGrafico, BorderLayout.SOUTH);
    }

    private JLabel crearKpiCard(JPanel container, String titulo, String valorInicial, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setBackground(Color.WHITE);

        // Línea superior de color distintivo
        JPanel borderTop = new JPanel();
        borderTop.setBackground(accentColor);
        borderTop.setPreferredSize(new Dimension(0, 5));
        card.add(borderTop, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(2, 1));
        content.setOpaque(false);
        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.GRAY);
        content.add(lblTitle);

        JLabel lblVal = new JLabel(valorInicial);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblVal.setForeground(new Color(44, 62, 80));
        content.add(lblVal);

        card.add(content, BorderLayout.CENTER);
        container.add(card);
        
        return lblVal;
    }

    private void cargarDatosDashboard() {
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                Map<String, Object> data = new HashMap<>();
                data.put("ventasHoy", pedidoDAO.getVentasDeHoy());
                data.put("pedidosHoy", pedidoDAO.getContadorPedidosDeHoy());
                data.put("criticosCount", insumoDAO.getInsumosCriticosCount());
                
                // Obtener ventas semanales reales de la base de datos
                double[] historico = pedidoDAO.getVentasSemanales();
                data.put("grafico", historico);
                return data;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> res = get();
                    lblKpiIngresos.setText(String.format("%.2f €", (double) res.get("ventasHoy")));
                    lblKpiPedidos.setText(String.valueOf((int) res.get("pedidosHoy")));
                    
                    int criticos = (int) res.get("criticosCount");
                    lblKpiStockCritico.setText(criticos + " Insumos");
                    if (criticos > 0) {
                        lblKpiStockCritico.setForeground(new Color(231, 76, 60));
                    } else {
                        lblKpiStockCritico.setForeground(new Color(46, 204, 113));
                    }
                    
                    salesChart.setValues((double[]) res.get("grafico"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // ==========================================
    // 2. MÓDULO DE VENTAS Y CARRITO
    // ==========================================
    private void crearPanelVentas() {
        panelVentas = new JPanel(new BorderLayout());
        
        // --- 1. CENTRO: Menú de Productos y Búsqueda ---
        JPanel panelCentralVentas = new JPanel(new BorderLayout());
        panelCentralVentas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Fila superior de filtros: Búsqueda y Categorías
        JPanel panelFiltrosVentas = new JPanel(new BorderLayout(15, 0));
        panelFiltrosVentas.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel lblMenu = new JLabel("Menú");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panelFiltrosVentas.add(lblMenu, BorderLayout.WEST);

        // Barra de búsqueda interactiva
        JTextField txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(220, 32));
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar producto...");
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrarProductosGrid(txtBuscar.getText()); }
            public void removeUpdate(DocumentEvent e) { filtrarProductosGrid(txtBuscar.getText()); }
            public void changedUpdate(DocumentEvent e) { filtrarProductosGrid(txtBuscar.getText()); }
        });
        panelFiltrosVentas.add(txtBuscar, BorderLayout.CENTER);

        // Categorías (Botones segmentados)
        JPanel panelCategorias = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        String[] categorias = {"Todos", "Hamburguesas", "Bebidas", "Acompañamientos"};
        ButtonGroup bgCats = new ButtonGroup();
        for (String cat : categorias) {
            JToggleButton btnCat = new JToggleButton(cat);
            btnCat.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCat.setFocusPainted(false);
            btnCat.addActionListener(e -> {
                categoriaFiltroActual = cat;
                filtrarProductosGrid(txtBuscar.getText());
            });
            bgCats.add(btnCat);
            if (cat.equals("Todos")) btnCat.setSelected(true);
            panelCategorias.add(btnCat);
        }
        panelFiltrosVentas.add(panelCategorias, BorderLayout.EAST);

        panelCentralVentas.add(panelFiltrosVentas, BorderLayout.NORTH);

        // Grid de productos
        panelProductosGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        panelProductosGrid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JScrollPane scrollProductos = new JScrollPane(panelProductosGrid);
        scrollProductos.setBorder(null);
        panelCentralVentas.add(scrollProductos, BorderLayout.CENTER);

        // --- 2. DERECHA: Carrito de Compras (Orden Actual) ---
        JPanel panelDerechoVentas = new JPanel(new BorderLayout());
        panelDerechoVentas.setPreferredSize(new Dimension(400, 0));
        panelDerechoVentas.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        JPanel panelHeaderCarrito = new JPanel(new BorderLayout());
        JLabel lblCarrito = new JLabel("Orden Actual");
        lblCarrito.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panelHeaderCarrito.add(lblCarrito, BorderLayout.NORTH);
        
        // Selector de Cliente
        JPanel panelClienteCombo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        panelClienteCombo.add(new JLabel("Cliente: "));
        cbClientesVenta = new JComboBox<>();
        cbClientesVenta.setPreferredSize(new Dimension(220, 30));
        panelClienteCombo.add(cbClientesVenta);
        
        JButton btnNuevoClienteRapido = new JButton("+");
        btnNuevoClienteRapido.setToolTipText("Registrar Cliente Rápido");
        btnNuevoClienteRapido.setPreferredSize(new Dimension(35, 30));
        btnNuevoClienteRapido.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNuevoClienteRapido.addActionListener(e -> mostrarDialogoNuevoCliente());
        panelClienteCombo.add(btnNuevoClienteRapido);

        panelHeaderCarrito.add(panelClienteCombo, BorderLayout.CENTER);
        panelDerechoVentas.add(panelHeaderCarrito, BorderLayout.NORTH);

        // Scroll del Carrito
        panelCarrito = new JPanel();
        panelCarrito.setLayout(new BoxLayout(panelCarrito, BoxLayout.Y_AXIS));
        panelCarrito.setBackground(Color.WHITE);
        JScrollPane scrollCarrito = new JScrollPane(panelCarrito);
        scrollCarrito.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        panelDerechoVentas.add(scrollCarrito, BorderLayout.CENTER);

        // Descuentos y Cuentas
        JPanel panelPago = new JPanel(new BorderLayout());
        panelPago.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Caja de Cupón
        JPanel panelCupon = new JPanel(new BorderLayout(5, 0));
        panelCupon.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        txtCupoDescuento = new JTextField();
        txtCupoDescuento.putClientProperty("JTextField.placeholderText", "Código de cupón (ej: KRUSTY10)");
        JButton btnAplicarCupon = new JButton("Aplicar");
        btnAplicarCupon.addActionListener(e -> aplicarCuponDescuento());
        panelCupon.add(txtCupoDescuento, BorderLayout.CENTER);
        panelCupon.add(btnAplicarCupon, BorderLayout.EAST);
        panelPago.add(panelCupon, BorderLayout.NORTH);

        // Subtotales
        JPanel panelDesglose = new JPanel(new GridLayout(3, 2, 0, 5));
        panelDesglose.add(new JLabel("Subtotal:"));
        lblSubtotalVentaActual = new JLabel("0.00 €", SwingConstants.RIGHT);
        panelDesglose.add(lblSubtotalVentaActual);
        
        panelDesglose.add(new JLabel("Descuento:"));
        lblDescuentoVentaActual = new JLabel("0% (0.00 €)", SwingConstants.RIGHT);
        panelDesglose.add(lblDescuentoVentaActual);

        JLabel lblTotalLabel = new JLabel("Total a Pagar:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelDesglose.add(lblTotalLabel);
        
        lblTotalVentaActual = new JLabel("0.00 €", SwingConstants.RIGHT);
        lblTotalVentaActual.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalVentaActual.setForeground(new Color(46, 204, 113));
        panelDesglose.add(lblTotalVentaActual);

        panelPago.add(panelDesglose, BorderLayout.CENTER);

        JButton btnCobrar = new JButton("FINALIZAR Y COBRAR");
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCobrar.setBackground(new Color(46, 204, 113));
        btnCobrar.setForeground(Color.WHITE);
        btnCobrar.setFocusPainted(false);
        btnCobrar.setPreferredSize(new Dimension(0, 55));
        btnCobrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrar.addActionListener(e -> procesarCobroConModal());
        
        panelPago.add(btnCobrar, BorderLayout.SOUTH);
        panelDerechoVentas.add(panelPago, BorderLayout.SOUTH);

        panelVentas.add(panelCentralVentas, BorderLayout.CENTER);
        panelVentas.add(panelDerechoVentas, BorderLayout.EAST);
    }

    private void cargarProductosGrid() {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoDAO.listarProductos();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    panelProductosGrid.removeAll();
                    listaBotonesProductos.clear();
                    
                    for (Producto p : productos) {
                        JButton btnProd = crearBotonProducto(p);
                        panelProductosGrid.add(btnProd);
                        listaBotonesProductos.add(btnProd);
                    }
                    panelProductosGrid.revalidate();
                    panelProductosGrid.repaint();
                    filtrarProductosGrid(""); // Ejecutar filtro inicial
                } catch (Exception e) {
                    System.err.println("Error al cargar productos en el grid: " + e.getMessage());
                }
            }
        }.execute();
    }

    private JButton crearBotonProducto(Producto p) {
        JButton btn = new JButton("<html><center><h3 style='margin:0; padding:10px 0 5px 0; font-family:Segoe UI;'>" + p.getNombre() + "</h3><p style='color:#27ae60; font-size:14px; font-weight:bold; margin:0; padding-bottom:10px;'>" + p.getPrecio() + " €</p></center></html>");
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("producto", p);
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(new Color(248, 249, 250));
                btn.setBorder(BorderFactory.createLineBorder(new Color(255, 204, 51), 2));
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(Color.WHITE);
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            }
        });
        
        btn.addActionListener(e -> agregarAlCarritoGrid(p));
        return btn;
    }

    private void filtrarProductosGrid(String query) {
        queryBusquedaActual = query.toLowerCase().trim();
        for (JButton btn : listaBotonesProductos) {
            Producto p = (Producto) btn.getClientProperty("producto");
            boolean coincideNombre = p.getNombre().toLowerCase().contains(queryBusquedaActual);
            boolean coincideCategoria = categoriaFiltroActual.equals("Todos") || p.getCategoria().equalsIgnoreCase(categoriaFiltroActual);
            
            if (coincideNombre && coincideCategoria) {
                btn.setVisible(true);
            } else {
                btn.setVisible(false);
            }
        }
        panelProductosGrid.revalidate();
        panelProductosGrid.repaint();
    }

    private void agregarAlCarritoGrid(Producto p) {
        String input = JOptionPane.showInputDialog(this, "¿Cuántas unidades de " + p.getNombre() + " desea agregar?", "Cantidad", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        
        int cantidad = 0;
        try {
            cantidad = Integer.parseInt(input);
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un número entero válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (carritoMap.containsKey(p.getCodigo())) {
            Object[] data = carritoMap.get(p.getCodigo());
            int cantExistente = (int) data[1];
            int nuevaCant = cantExistente + cantidad;
            data[1] = nuevaCant;
            
            JPanel itemPanel = (JPanel) data[2];
            JLabel lblNombre = (JLabel) itemPanel.getClientProperty("lblNombre");
            lblNombre.setText(nuevaCant + "x " + p.getNombre());
            
            JLabel lblPrecio = (JLabel) itemPanel.getClientProperty("lblPrecio");
            lblPrecio.setText(String.format("%.2f €", p.getPrecio() * nuevaCant));
            
            actualizarCuentas(p.getPrecio() * cantidad);
        } else {
            JPanel item = new JPanel(new BorderLayout());
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            item.setBackground(Color.WHITE);
            
            JLabel lblNombre = new JLabel(cantidad + "x " + p.getNombre());
            lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.putClientProperty("lblNombre", lblNombre);
            
            JPanel panelDerechoItem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            panelDerechoItem.setBackground(Color.WHITE);
            
            double sub = p.getPrecio() * cantidad;
            JLabel lblPrecio = new JLabel(String.format("%.2f €", sub));
            lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));
            item.putClientProperty("lblPrecio", lblPrecio);
            
            JButton btnEliminar = new JButton("X");
            btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btnEliminar.setForeground(Color.WHITE);
            btnEliminar.setBackground(new Color(231, 76, 60));
            btnEliminar.setFocusPainted(false);
            btnEliminar.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            panelDerechoItem.add(lblPrecio);
            panelDerechoItem.add(btnEliminar);
            
            item.add(lblNombre, BorderLayout.WEST);
            item.add(panelDerechoItem, BorderLayout.EAST);
            
            panelCarrito.add(item);
            
            Object[] data = new Object[]{p, cantidad, item};
            carritoMap.put(p.getCodigo(), data);
            
            actualizarCuentas(sub);
            
            btnEliminar.addActionListener(e -> {
                panelCarrito.remove(item);
                carritoMap.remove(p.getCodigo());
                actualizarCuentas(-p.getPrecio() * (int)data[1]);
                panelCarrito.revalidate();
                panelCarrito.repaint();
            });
        }
        
        panelCarrito.revalidate();
        panelCarrito.repaint();
    }

    private void aplicarCuponDescuento() {
        String cupon = txtCupoDescuento.getText().trim().toUpperCase();
        if (cupon.isEmpty()) {
            porcentajeDescuento = 0.0;
            JOptionPane.showMessageDialog(this, "Cupón removido.");
        } else if (cupon.equals("KRUSTY10")) {
            porcentajeDescuento = 0.10;
            JOptionPane.showMessageDialog(this, "Cupón KRUSTY10 aplicado: 10% de Descuento.");
        } else if (cupon.equals("GERMAN15")) {
            porcentajeDescuento = 0.15;
            JOptionPane.showMessageDialog(this, "Cupón GERMAN15 aplicado: 15% de Descuento.");
        } else if (cupon.equals("PROMO5")) {
            porcentajeDescuento = 0.05;
            JOptionPane.showMessageDialog(this, "Cupón PROMO5 aplicado: 5% de Descuento.");
        } else {
            porcentajeDescuento = 0.0;
            JOptionPane.showMessageDialog(this, "Código de cupón inválido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
        actualizarCuentas(0.0);
    }

    private void actualizarCuentas(double deltaSubtotal) {
        subtotalVentaActual += deltaSubtotal;
        if (subtotalVentaActual < 0.01) subtotalVentaActual = 0.0;
        
        double descuento = subtotalVentaActual * porcentajeDescuento;
        totalVentaActual = subtotalVentaActual - descuento;
        if (totalVentaActual < 0.01) totalVentaActual = 0.0;

        lblSubtotalVentaActual.setText(String.format("%.2f €", subtotalVentaActual));
        lblDescuentoVentaActual.setText(String.format("%.0f%% (%.2f €)", porcentajeDescuento * 100, descuento));
        lblTotalVentaActual.setText(String.format("%.2f €", totalVentaActual));
    }

    // ==========================================
    // 2.A DIÁLOGO DE COBRO MODAL (NUEVO)
    // ==========================================
    private void procesarCobroConModal() {
        if (carritoMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue productos al pedido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente clienteSelec = (Cliente) cbClientesVenta.getSelectedItem();
        int idCliente = (clienteSelec != null) ? clienteSelec.getId() : 0;

        // Crear ventana JDialog de Cobro interactiva
        JDialog diagCobro = new JDialog(this, "Procesar Cobro de Venta", true);
        diagCobro.setSize(450, 480);
        diagCobro.setLocationRelativeTo(this);
        diagCobro.setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        // Monto Total
        JLabel lblTituloMonto = new JLabel("TOTAL A COBRAR");
        lblTituloMonto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTituloMonto.setForeground(Color.GRAY);
        gbc.gridy = 0;
        panelCentro.add(lblTituloMonto, gbc);

        JLabel lblMontoTotal = new JLabel(String.format("%.2f €", totalVentaActual));
        lblMontoTotal.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblMontoTotal.setForeground(new Color(46, 204, 113));
        gbc.gridy = 1;
        panelCentro.add(lblMontoTotal, gbc);

        // Método de pago
        JLabel lblTituloPago = new JLabel("MÉTODO DE PAGO");
        lblTituloPago.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTituloPago.setForeground(Color.GRAY);
        gbc.gridy = 2;
        panelCentro.add(lblTituloPago, gbc);

        JComboBox<String> cbMetodoPago = new JComboBox<>(new String[]{"EFECTIVO", "TARJETA", "BIZUM"});
        cbMetodoPago.setPreferredSize(new Dimension(0, 35));
        cbMetodoPago.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 3;
        panelCentro.add(cbMetodoPago, gbc);

        // Efectivo recibido y Cambio
        JLabel lblTituloRecibido = new JLabel("EFECTIVO RECIBIDO");
        lblTituloRecibido.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTituloRecibido.setForeground(Color.GRAY);
        gbc.gridy = 4;
        panelCentro.add(lblTituloRecibido, gbc);

        JTextField txtRecibido = new JTextField();
        txtRecibido.setPreferredSize(new Dimension(0, 40));
        txtRecibido.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridy = 5;
        panelCentro.add(txtRecibido, gbc);

        JLabel lblTituloCambio = new JLabel("CAMBIO A ENTREGAR");
        lblTituloCambio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTituloCambio.setForeground(Color.GRAY);
        gbc.gridy = 6;
        panelCentro.add(lblTituloCambio, gbc);

        JLabel lblCambioCalculado = new JLabel("0.00 €");
        lblCambioCalculado.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblCambioCalculado.setForeground(Color.GRAY);
        gbc.gridy = 7;
        panelCentro.add(lblCambioCalculado, gbc);

        // Botones de acción
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 15, 0));
        JButton btnCancelar = new JButton("CANCELAR");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelar.setBackground(new Color(231, 76, 60));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.addActionListener(e -> diagCobro.dispose());

        JButton btnConfirmar = new JButton("CONFIRMAR PAGO");
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirmar.setBackground(new Color(46, 204, 113));
        btnConfirmar.setForeground(Color.WHITE);
        
        panelBotones.add(btnCancelar);
        panelBotones.add(btnConfirmar);
        gbc.gridy = 8;
        gbc.insets = new Insets(20, 0, 0, 0);
        panelCentro.add(panelBotones, gbc);

        diagCobro.add(panelCentro, BorderLayout.CENTER);

        // Lógica interactiva de cambio
        Runnable calcularCambio = () -> {
            try {
                String recText = txtRecibido.getText().trim();
                if (recText.isEmpty()) {
                    lblCambioCalculado.setText("0.00 €");
                    lblCambioCalculado.setForeground(Color.GRAY);
                    btnConfirmar.setEnabled(cbMetodoPago.getSelectedItem().toString().equals("EFECTIVO") ? false : true);
                    return;
                }
                double recibido = Double.parseDouble(recText);
                double cambio = recibido - totalVentaActual;
                if (cambio >= 0) {
                    lblCambioCalculado.setText(String.format("%.2f €", cambio));
                    lblCambioCalculado.setForeground(new Color(39, 174, 96));
                    btnConfirmar.setEnabled(true);
                } else {
                    lblCambioCalculado.setText("Insuficiente");
                    lblCambioCalculado.setForeground(Color.RED);
                    btnConfirmar.setEnabled(false);
                }
            } catch (NumberFormatException ex) {
                lblCambioCalculado.setText("Inválido");
                lblCambioCalculado.setForeground(Color.RED);
                btnConfirmar.setEnabled(false);
            }
        };

        txtRecibido.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calcularCambio.run(); }
            public void removeUpdate(DocumentEvent e) { calcularCambio.run(); }
            public void changedUpdate(DocumentEvent e) { calcularCambio.run(); }
        });

        cbMetodoPago.addActionListener(e -> {
            String metodo = cbMetodoPago.getSelectedItem().toString();
            if (metodo.equals("EFECTIVO")) {
                txtRecibido.setEnabled(true);
                btnConfirmar.setEnabled(false);
                calcularCambio.run();
            } else {
                txtRecibido.setEnabled(false);
                txtRecibido.setText("");
                lblCambioCalculado.setText("No Aplica (Pago Electrónico)");
                lblCambioCalculado.setForeground(Color.BLUE);
                btnConfirmar.setEnabled(true);
            }
        });

        // Configuración inicial de confirmación
        btnConfirmar.setEnabled(false);

        // Confirmación final
        btnConfirmar.addActionListener(e -> {
            String metodo = cbMetodoPago.getSelectedItem().toString();
            double recibido = 0.0;
            if (metodo.equals("EFECTIVO")) {
                recibido = Double.parseDouble(txtRecibido.getText().trim());
            } else {
                recibido = totalVentaActual;
            }
            double cambioFinal = recibido - totalVentaActual;
            
            diagCobro.dispose();
            completarRegistroPedidoVenta(idCliente, clienteSelec, metodo, recibido, cambioFinal);
        });

        diagCobro.setVisible(true);
    }

    private void completarRegistroPedidoVenta(int idCliente, Cliente clienteSelec, String metodoPago, double recibido, double cambio) {
        List<DetallePedido> detalles = new ArrayList<>();
        for (Map.Entry<String, Object[]> entry : carritoMap.entrySet()) {
            Producto p = (Producto) entry.getValue()[0];
            int cant = (int) entry.getValue()[1];
            detalles.add(new DetallePedido(0, 0, p.getCodigo(), cant, p.getPrecio()));
        }

        new SwingWorker<Map<Integer, Object>, Void>() {
            @Override
            protected Map<Integer, Object> doInBackground() throws Exception {
                Map<Integer, Object> result = new HashMap<>();
                try {
                    Map<Integer, Integer> stockDescuentos = ventaService.calcularDescuentosTotales(carritoMap);
                    
                    double valorDescuento = subtotalVentaActual * porcentajeDescuento;
                    Pedido pedido = new Pedido(0, idCliente, new Date(), "PAGADO", totalVentaActual, valorDescuento, metodoPago);
                    boolean exito = pedidoDAO.registrarVenta(pedido, detalles, stockDescuentos);
                    
                    result.put(1, exito);
                    result.put(2, pedido);
                } catch (StockInsuficienteException e) {
                    result.put(1, false);
                    result.put(3, e.getMessage());
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    Map<Integer, Object> res = get();
                    boolean exito = (boolean) res.get(1);
                    if (exito) {
                        Pedido pedido = (Pedido) res.get(2);
                        
                        new SwingWorker<List<Producto>, Void>() {
                            private List<DetallePedido> detallesVentaLocal = new ArrayList<>(detalles);
                            @Override
                            protected List<Producto> doInBackground() throws Exception {
                                return productoDAO.listarProductos();
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<Producto> catalogo = get();
                                    generarTicketPDFCompleto(pedido, clienteSelec != null ? clienteSelec : new Cliente(0, "Público General", "-", "-", "-"), catalogo, detallesVentaLocal, recibido, cambio);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }.execute();

                        JOptionPane.showMessageDialog(MainMenuView.this, String.format("Venta por %.2f € registrada con éxito.", totalVentaActual), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Limpiar orden
                        panelCarrito.removeAll();
                        carritoMap.clear();
                        subtotalVentaActual = 0.0;
                        porcentajeDescuento = 0.0;
                        totalVentaActual = 0.0;
                        txtCupoDescuento.setText("");
                        actualizarCuentas(0.0);
                        
                        panelCarrito.revalidate();
                        panelCarrito.repaint();
                        cbClientesVenta.setSelectedIndex(0);
                        actualizarAlertasCampana();
                    } else {
                        if (res.containsKey(3)) {
                            JOptionPane.showMessageDialog(MainMenuView.this, res.get(3).toString(), "Error de Stock", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Error al registrar pedido.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void generarTicketPDFCompleto(Pedido pedido, Cliente cliente, List<Producto> catalogo, List<DetallePedido> detallesTicket, double recibido, double cambio) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Ticket PDF");
        fileChooser.setSelectedFile(new File("Ticket_" + System.currentTimeMillis() + ".pdf"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Guardado del archivo PDF cancelado.");
            return;
        }
        
        File fileToSave = fileChooser.getSelectedFile();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(100, 720);
                contentStream.showText("--- KRUSTY BURGER TICKET ---");
                contentStream.newLineAtOffset(0, -30);

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Cliente: " + cliente.getNombre());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Metodo Pago: " + pedido.getMetodoPago());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("---------------------------------------------------");
                contentStream.newLineAtOffset(0, -20);

                double subt = 0.0;
                for (DetallePedido dp : detallesTicket) {
                    String nombreProd = "Producto ID: " + dp.getProductoId();
                    for (Producto p : catalogo) {
                        if (p.getCodigo().equals(dp.getProductoId())) {
                            nombreProd = p.getNombre();
                            break;
                        }
                    }
                    double itemSub = dp.getCantidad() * dp.getPrecioUnitario();
                    subt += itemSub;
                    contentStream.showText(dp.getCantidad() + "x " + nombreProd + "  " + String.format("%.2f", dp.getPrecioUnitario()) + " EUR  (Sub: " + String.format("%.2f", itemSub) + " EUR)");
                    contentStream.newLineAtOffset(0, -15);
                }

                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("SUBTOTAL: " + String.format("%.2f", subt) + " EUR");
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("DESCUENTO APLICADO: " + String.format("%.2f", pedido.getDescuento()) + " EUR");
                contentStream.newLineAtOffset(0, -15);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.showText("TOTAL NETO: " + String.format("%.2f", pedido.getTotal()) + " EUR");
                
                contentStream.newLineAtOffset(0, -20);
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.showText("EFECTIVO ENTREGADO: " + String.format("%.2f", recibido) + " EUR");
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("CAMBIO DEVUELTO: " + String.format("%.2f", cambio) + " EUR");
                
                contentStream.endText();
            }

            document.save(fileToSave);
            JOptionPane.showMessageDialog(this, "Ticket PDF generado en:\n" + fileToSave.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar ticket PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoNuevoCliente() {
        JTextField txtNom = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtDir = new JTextField();
        JTextField txtCor = new JTextField();

        Object[] message = {
            "Nombre:", txtNom,
            "Teléfono:", txtTel,
            "Dirección:", txtDir,
            "Correo:", txtCor
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Registrar Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nom = txtNom.getText().trim();
            String tel = txtTel.getText().trim();
            String dir = txtDir.getText().trim();
            String cor = txtCor.getText().trim();

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!tel.isEmpty() && !tel.matches("[+0-9\\s-]+")) {
                JOptionPane.showMessageDialog(this, "El teléfono solo debe contener números, espacios o guiones.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!cor.isEmpty() && !cor.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, "El correo electrónico no es válido.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Cliente c = new Cliente(0, nom, tel, dir, cor);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return clienteDAO.registrarCliente(c);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Cliente registrado.");
                            cargarClientesEnCombo();
                            cargarClientes();
                        } else {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        }
    }

    private void cargarClientesEnCombo() {
        new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() throws Exception {
                return clienteDAO.listarClientes();
            }

            @Override
            protected void done() {
                try {
                    List<Cliente> clientes = get();
                    cbClientesVenta.removeAllItems();
                    cbClientesVenta.addItem(new Cliente(0, "Público General", "-", "-", "-"));
                    for (Cliente c : clientes) {
                        cbClientesVenta.addItem(c);
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar clientes en combo: " + e.getMessage());
                }
            }
        }.execute();
    }

    // ==========================================
    // 3. MÓDULO DE ALMACÉN
    // ==========================================
    private void crearPanelAlmacen() {
        panelAlmacen = new JPanel(new BorderLayout());
        panelAlmacen.setBorder(BorderFactory.createTitledBorder("Control de Almacén e Insumos"));

        modeloInsumos = new DefaultTableModel(new Object[]{"ID", "Código", "Nombre", "Precio", "Stock", "Categoría"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaInsumos = new JTable(modeloInsumos);
        
        tablaInsumos.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    try {
                        int stock = Integer.parseInt(table.getValueAt(row, 4).toString());
                        if (stock < 15) {
                            c.setBackground(new Color(255, 204, 204));
                            c.setForeground(Color.RED);
                        } else {
                            c.setBackground(table.getBackground());
                            c.setForeground(table.getForeground());
                        }
                    } catch (Exception e) {
                        c.setBackground(table.getBackground());
                        c.setForeground(table.getForeground());
                    }
                }
                return c;
            }
        });
        
        panelAlmacen.add(new JScrollPane(tablaInsumos), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(new JLabel("ID Insumo:"));
        txtIdInsumo = new JTextField(5);
        txtIdInsumo.setEditable(false);
        bottomPanel.add(txtIdInsumo);

        bottomPanel.add(new JLabel("Nuevo Stock:"));
        txtNuevoStock = new JTextField(5);
        bottomPanel.add(txtNuevoStock);

        JButton btnActualizar = new JButton("Actualizar Stock");
        bottomPanel.add(btnActualizar);

        panelAlmacen.add(bottomPanel, BorderLayout.SOUTH);

        tablaInsumos.getSelectionModel().addListSelectionListener(e -> {
            int row = tablaInsumos.getSelectedRow();
            if (row >= 0) {
                txtIdInsumo.setText(tablaInsumos.getValueAt(row, 0).toString());
                txtNuevoStock.setText(tablaInsumos.getValueAt(row, 4).toString());
                txtNuevoStock.requestFocus();
                txtNuevoStock.selectAll();
            }
        });

        btnActualizar.addActionListener(e -> {
            try {
                String idText = txtIdInsumo.getText();
                String stockText = txtNuevoStock.getText().trim();
                if (idText.isEmpty() || stockText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Seleccione un insumo de la tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int id = Integer.parseInt(idText);
                int stock = Integer.parseInt(stockText);
                if (stock < 0) {
                    JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return insumoDAO.actualizarStock(id, stock);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Stock actualizado.");
                                cargarInsumos();
                                txtIdInsumo.setText("");
                                txtNuevoStock.setText("");
                                actualizarAlertasCampana();
                            } else {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Error al actualizar stock.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valores numéricos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void cargarInsumos() {
        new SwingWorker<List<Insumo>, Void>() {
            @Override
            protected List<Insumo> doInBackground() throws Exception {
                return insumoDAO.listarInsumos();
            }

            @Override
            protected void done() {
                try {
                    List<Insumo> insumos = get();
                    modeloInsumos.setRowCount(0);
                    for (Insumo i : insumos) {
                        modeloInsumos.addRow(new Object[]{i.getId(), i.getCodigo(), i.getNombre(), i.getPrecio(), i.getStock(), i.getCategoria()});
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar insumos: " + e.getMessage());
                }
            }
        }.execute();
    }

    // ==========================================
    // 4. MÓDULO DE CLIENTES
    // ==========================================
    private void crearPanelClientes() {
        panelClientes = new JPanel(new BorderLayout());
        panelClientes.setBorder(BorderFactory.createTitledBorder("Directorio de Clientes"));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        formPanel.add(new JLabel("Nombre:"));
        txtNombreCliente = new JTextField();
        formPanel.add(txtNombreCliente);

        formPanel.add(new JLabel("Teléfono:"));
        txtTelefonoCliente = new JTextField();
        formPanel.add(txtTelefonoCliente);

        formPanel.add(new JLabel("Dirección:"));
        txtDireccionCliente = new JTextField();
        formPanel.add(txtDireccionCliente);

        formPanel.add(new JLabel("Correo:"));
        txtCorreoCliente = new JTextField();
        formPanel.add(txtCorreoCliente);

        JPanel panelBotones = new JPanel(new GridLayout(1, 4, 10, 0));
        btnGuardarCliente = new JButton("Guardar");
        btnGuardarCliente.setBackground(new Color(46, 204, 113));
        btnGuardarCliente.setForeground(Color.WHITE);
        
        btnActualizarCliente = new JButton("Actualizar");
        btnActualizarCliente.setEnabled(false);
        btnActualizarCliente.setBackground(new Color(52, 152, 219));
        btnActualizarCliente.setForeground(Color.WHITE);
        
        btnEliminarCliente = new JButton("Eliminar");
        btnEliminarCliente.setEnabled(false);
        btnEliminarCliente.setBackground(new Color(231, 76, 60));
        btnEliminarCliente.setForeground(Color.WHITE);
        
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setBackground(new Color(149, 165, 166));
        btnLimpiar.setForeground(Color.WHITE);

        panelBotones.add(btnGuardarCliente);
        panelBotones.add(btnActualizarCliente);
        panelBotones.add(btnEliminarCliente);
        panelBotones.add(btnLimpiar);

        formPanel.add(new JLabel());
        formPanel.add(panelBotones);

        panelClientes.add(formPanel, BorderLayout.NORTH);

        modeloClientes = new DefaultTableModel(new Object[]{"ID", "Nombre", "Teléfono", "Dirección", "Correo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaClientes = new JTable(modeloClientes);
        panelClientes.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            int row = tablaClientes.getSelectedRow();
            if (row >= 0) {
                clienteSeleccionadoId = Integer.parseInt(tablaClientes.getValueAt(row, 0).toString());
                txtNombreCliente.setText(tablaClientes.getValueAt(row, 1).toString());
                txtTelefonoCliente.setText(tablaClientes.getValueAt(row, 2).toString());
                txtDireccionCliente.setText(tablaClientes.getValueAt(row, 3).toString());
                txtCorreoCliente.setText(tablaClientes.getValueAt(row, 4).toString());
                
                btnGuardarCliente.setEnabled(false);
                btnActualizarCliente.setEnabled(true);
                btnEliminarCliente.setEnabled(true);
            }
        });

        btnLimpiar.addActionListener(e -> limpiarFormularioCliente());

        btnGuardarCliente.addActionListener(e -> {
            String nom = txtNombreCliente.getText().trim();
            String tel = txtTelefonoCliente.getText().trim();
            String dir = txtDireccionCliente.getText().trim();
            String cor = txtCorreoCliente.getText().trim();

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!tel.isEmpty() && !tel.matches("[+0-9\\s-]+")) {
                JOptionPane.showMessageDialog(this, "Teléfono inválido.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!cor.isEmpty() && !cor.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, "Correo inválido.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Cliente c = new Cliente(0, nom, tel, dir, cor);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return clienteDAO.registrarCliente(c);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Cliente guardado.");
                            limpiarFormularioCliente();
                            cargarClientes();
                        } else {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        btnActualizarCliente.addActionListener(e -> {
            if (clienteSeleccionadoId == -1) return;
            String nom = txtNombreCliente.getText().trim();
            String tel = txtTelefonoCliente.getText().trim();
            String dir = txtDireccionCliente.getText().trim();
            String cor = txtCorreoCliente.getText().trim();

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!tel.isEmpty() && !tel.matches("[+0-9\\s-]+")) {
                JOptionPane.showMessageDialog(this, "Teléfono inválido.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!cor.isEmpty() && !cor.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, "Correo inválido.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Cliente c = new Cliente(clienteSeleccionadoId, nom, tel, dir, cor);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return clienteDAO.actualizarCliente(c);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Cliente actualizado.");
                            limpiarFormularioCliente();
                            cargarClientes();
                        } else {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        btnEliminarCliente.addActionListener(e -> {
            if (clienteSeleccionadoId == -1) return;
            int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar cliente?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return clienteDAO.eliminarCliente(clienteSeleccionadoId);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Cliente eliminado.");
                                limpiarFormularioCliente();
                                cargarClientes();
                            } else {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Error al eliminar cliente.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            }
        });
    }

    private void limpiarFormularioCliente() {
        clienteSeleccionadoId = -1;
        txtNombreCliente.setText("");
        txtTelefonoCliente.setText("");
        txtDireccionCliente.setText("");
        txtCorreoCliente.setText("");
        tablaClientes.clearSelection();
        
        btnGuardarCliente.setEnabled(true);
        btnActualizarCliente.setEnabled(false);
        btnEliminarCliente.setEnabled(false);
    }

    private void cargarClientes() {
        new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() throws Exception {
                return clienteDAO.listarClientes();
            }

            @Override
            protected void done() {
                try {
                    List<Cliente> clientes = get();
                    modeloClientes.setRowCount(0);
                    for (Cliente c : clientes) {
                        modeloClientes.addRow(new Object[]{c.getId(), c.getNombre(), c.getTelefono(), c.getDireccion(), c.getCorreo()});
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar clientes: " + e.getMessage());
                }
            }
        }.execute();
    }

    // ==========================================
    // 5. MÓDULO DE REGISTRO / HISTORIAL DE VENTAS
    // ==========================================
    private void crearPanelVentasTotales() {
        panelVentasTotales = new JPanel(new BorderLayout());
        panelVentasTotales.setBorder(BorderFactory.createTitledBorder("Historial de Ventas"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.add(new JLabel("Desde (yyyy-MM-dd):"));
        txtFechaDesde = new JTextField(8);
        filterPanel.add(txtFechaDesde);

        filterPanel.add(new JLabel("Hasta (yyyy-MM-dd):"));
        txtFechaHasta = new JTextField(8);
        filterPanel.add(txtFechaHasta);

        filterPanel.add(new JLabel("Estado:"));
        cbEstadoFiltro = new JComboBox<>(new String[]{"TODOS", "PAGADO", "PENDIENTE"});
        filterPanel.add(cbEstadoFiltro);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltrar.setBackground(new Color(52, 152, 219));
        btnFiltrar.setForeground(Color.WHITE);
        filterPanel.add(btnFiltrar);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiar.setBackground(new Color(149, 165, 166));
        btnLimpiar.setForeground(Color.WHITE);
        filterPanel.add(btnLimpiar);

        panelVentasTotales.add(filterPanel, BorderLayout.NORTH);

        modeloHistorialVentas = new DefaultTableModel(new Object[]{"ID Pedido", "Cliente", "Fecha y Hora", "Estado", "Descuento", "Método Pago", "Monto Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaHistorialVentas = new JTable(modeloHistorialVentas);
        panelVentasTotales.add(new JScrollPane(tablaHistorialVentas), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblGananciaTotal = new JLabel("Ganancia Total: 0.00 €");
        lblGananciaTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGananciaTotal.setForeground(new Color(39, 174, 96));
        bottomPanel.add(lblGananciaTotal);
        panelVentasTotales.add(bottomPanel, BorderLayout.SOUTH);

        btnFiltrar.addActionListener(e -> cargarHistorialVentas());
        btnLimpiar.addActionListener(e -> {
            txtFechaDesde.setText("");
            txtFechaHasta.setText("");
            cbEstadoFiltro.setSelectedIndex(0);
            cargarHistorialVentas();
        });
    }

    private void cargarHistorialVentas() {
        String desdeText = txtFechaDesde.getText().trim();
        String hastaText = txtFechaHasta.getText().trim();
        String estadoSelec = (String) cbEstadoFiltro.getSelectedItem();

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");

        new SwingWorker<List<Pedido>, Void>() {
            private Date desdeDate = null;
            private Date hastaDate = null;
            private boolean formatError = false;

            @Override
            protected List<Pedido> doInBackground() throws Exception {
                try {
                    if (!desdeText.isEmpty()) desdeDate = parser.parse(desdeText);
                    if (!hastaText.isEmpty()) {
                        Date rawHasta = parser.parse(hastaText);
                        hastaDate = new Date(rawHasta.getTime() + 86399999L);
                    }
                } catch (ParseException ex) {
                    formatError = true;
                    return new ArrayList<>();
                }
                return pedidoDAO.listarPedidosFiltrados(desdeDate, hastaDate, estadoSelec);
            }

            @Override
            protected void done() {
                if (formatError) {
                    JOptionPane.showMessageDialog(MainMenuView.this, "Fechas inválidas. Formato: yyyy-MM-dd", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    List<Pedido> pedidos = get();
                    modeloHistorialVentas.setRowCount(0);
                    double suma = 0.0;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    for (Pedido p : pedidos) {
                        String fechaStr = p.getFecha() != null ? sdf.format(p.getFecha()) : "-";
                        modeloHistorialVentas.addRow(new Object[]{
                            p.getId(),
                            p.getClienteNombre(),
                            fechaStr,
                            p.getEstado(),
                            String.format("%.2f €", p.getDescuento()),
                            p.getMetodoPago() != null ? p.getMetodoPago() : "EFECTIVO",
                            String.format("%.2f €", p.getTotal())
                        });
                        suma += p.getTotal();
                    }
                    lblGananciaTotal.setText(String.format("Ganancia Total Acumulada: %.2f €", suma));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // ==========================================
    // 6. GESTIÓN DE PRODUCTOS Y RECETAS (NUEVO)
    // ==========================================
    private void crearPanelAdminProductos() {
        panelAdminProductos = new JPanel(new BorderLayout(15, 0));
        panelAdminProductos.setBorder(BorderFactory.createTitledBorder("Administración de Menú y Recetario"));

        // --- PARTE IZQUIERDA: CRUD de Productos ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setPreferredSize(new Dimension(500, 0));
        panelIzquierdo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Formulario CRUD Producto
        JPanel formProducto = new JPanel(new GridLayout(5, 2, 5, 5));
        formProducto.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        formProducto.add(new JLabel("Código:"));
        txtAdminCodigo = new JTextField();
        formProducto.add(txtAdminCodigo);

        formProducto.add(new JLabel("Nombre:"));
        txtAdminNombre = new JTextField();
        formProducto.add(txtAdminNombre);

        formProducto.add(new JLabel("Precio (€):"));
        txtAdminPrecio = new JTextField();
        formProducto.add(txtAdminPrecio);

        formProducto.add(new JLabel("Categoría:"));
        cbAdminCategoria = new JComboBox<>(new String[]{"Hamburguesas", "Bebidas", "Acompañamientos"});
        formProducto.add(cbAdminCategoria);

        // Botones CRUD
        JPanel panelBotonesProd = new JPanel(new GridLayout(1, 3, 8, 0));
        JButton btnCrearProd = new JButton("Agregar");
        btnCrearProd.setBackground(new Color(46, 204, 113));
        btnCrearProd.setForeground(Color.WHITE);
        
        JButton btnModificarProd = new JButton("Editar");
        btnModificarProd.setBackground(new Color(52, 152, 219));
        btnModificarProd.setForeground(Color.WHITE);
        
        JButton btnBorrarProd = new JButton("Borrar");
        btnBorrarProd.setBackground(new Color(231, 76, 60));
        btnBorrarProd.setForeground(Color.WHITE);

        panelBotonesProd.add(btnCrearProd);
        panelBotonesProd.add(btnModificarProd);
        panelBotonesProd.add(btnBorrarProd);

        formProducto.add(new JLabel());
        formProducto.add(panelBotonesProd);
        panelIzquierdo.add(formProducto, BorderLayout.NORTH);

        // Tabla de Productos
        modeloAdminProductos = new DefaultTableModel(new Object[]{"Código", "Nombre", "Precio", "Categoría"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaAdminProductos = new JTable(modeloAdminProductos);
        panelIzquierdo.add(new JScrollPane(tablaAdminProductos), BorderLayout.CENTER);

        panelAdminProductos.add(panelIzquierdo, BorderLayout.WEST);

        // --- PARTE DERECHA: Receta e Ingredientes ---
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblRecetaTitle = new JLabel("Ingredientes de la Receta (Seleccione un producto a la izquierda)");
        lblRecetaTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRecetaTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panelDerecho.add(lblRecetaTitle, BorderLayout.NORTH);

        // Formulario Ingrediente
        JPanel formIngrediente = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        formIngrediente.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        formIngrediente.add(new JLabel("Insumo:"));
        cbRecetaInsumos = new JComboBox<>();
        cbRecetaInsumos.setPreferredSize(new Dimension(180, 28));
        formIngrediente.add(cbRecetaInsumos);

        formIngrediente.add(new JLabel("Cant:"));
        txtRecetaCantidad = new JTextField(4);
        formIngrediente.add(txtRecetaCantidad);

        JButton btnAddIngrediente = new JButton("Añadir");
        btnAddIngrediente.setBackground(new Color(52, 152, 219));
        btnAddIngrediente.setForeground(Color.WHITE);
        formIngrediente.add(btnAddIngrediente);

        JButton btnDelIngrediente = new JButton("Remover");
        btnDelIngrediente.setBackground(new Color(231, 76, 60));
        btnDelIngrediente.setForeground(Color.WHITE);
        formIngrediente.add(btnDelIngrediente);

        JPanel panelArribaDerecho = new JPanel(new BorderLayout());
        panelArribaDerecho.add(formIngrediente, BorderLayout.CENTER);
        panelDerecho.add(panelArribaDerecho, BorderLayout.NORTH);

        // Tabla de Ingredientes Receta
        modeloRecetaIngredientes = new DefaultTableModel(new Object[]{"Insumo ID", "Nombre", "Cantidad Requerida"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaRecetaIngredientes = new JTable(modeloRecetaIngredientes);
        panelDerecho.add(new JScrollPane(tablaRecetaIngredientes), BorderLayout.CENTER);

        // Botón de Guardar Receta Completa
        JButton btnGuardarReceta = new JButton("GUARDAR RECETA COMPLETA");
        btnGuardarReceta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardarReceta.setBackground(new Color(46, 204, 113));
        btnGuardarReceta.setForeground(Color.WHITE);
        btnGuardarReceta.setPreferredSize(new Dimension(0, 45));
        panelDerecho.add(btnGuardarReceta, BorderLayout.SOUTH);

        panelAdminProductos.add(panelDerecho, BorderLayout.CENTER);

        // --- LÓGICA GESTIÓN PRODUCTOS Y RECETAS ---
        
        // Listener selección tabla productos
        tablaAdminProductos.getSelectionModel().addListSelectionListener(e -> {
            int row = tablaAdminProductos.getSelectedRow();
            if (row >= 0) {
                String codigo = tablaAdminProductos.getValueAt(row, 0).toString();
                txtAdminCodigo.setText(codigo);
                txtAdminCodigo.setEditable(false); // No se puede editar llave primaria
                txtAdminNombre.setText(tablaAdminProductos.getValueAt(row, 1).toString());
                txtAdminPrecio.setText(tablaAdminProductos.getValueAt(row, 2).toString());
                cbAdminCategoria.setSelectedItem(tablaAdminProductos.getValueAt(row, 3).toString());
                
                lblRecetaTitle.setText("Receta de: " + tablaAdminProductos.getValueAt(row, 1).toString());
                cargarRecetaProducto(codigo);
            }
        });

        // Crear Producto
        btnCrearProd.addActionListener(e -> {
            String cod = txtAdminCodigo.getText().trim();
            String nom = txtAdminNombre.getText().trim();
            String preText = txtAdminPrecio.getText().trim();
            String cat = cbAdminCategoria.getSelectedItem().toString();

            if (cod.isEmpty() || nom.isEmpty() || preText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos del producto.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double pre = Double.parseDouble(preText);
                Producto p = new Producto(cod, nom, pre, cat);
                
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return productoDAO.registrarProducto(p);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Producto agregado.");
                                cargarAdminProductos();
                                txtAdminCodigo.setText("");
                                txtAdminNombre.setText("");
                                txtAdminPrecio.setText("");
                            } else {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Error al guardar. ¿Código duplicado?", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Editar Producto
        btnModificarProd.addActionListener(e -> {
            String cod = txtAdminCodigo.getText().trim();
            String nom = txtAdminNombre.getText().trim();
            String preText = txtAdminPrecio.getText().trim();
            String cat = cbAdminCategoria.getSelectedItem().toString();

            if (cod.isEmpty() || nom.isEmpty() || preText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double pre = Double.parseDouble(preText);
                Producto p = new Producto(cod, nom, pre, cat);
                
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return productoDAO.actualizarProducto(p);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Producto modificado.");
                                cargarAdminProductos();
                            } else {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Error al modificar producto.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Eliminar Producto
        btnBorrarProd.addActionListener(e -> {
            String cod = txtAdminCodigo.getText().trim();
            if (cod.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto para borrar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este producto y su receta?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return productoDAO.eliminarProducto(cod);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Producto eliminado.");
                                txtAdminCodigo.setText("");
                                txtAdminCodigo.setEditable(true);
                                txtAdminNombre.setText("");
                                txtAdminPrecio.setText("");
                                modeloRecetaIngredientes.setRowCount(0);
                                recetaEditandoMap.clear();
                                cargarAdminProductos();
                            } else {
                                JOptionPane.showMessageDialog(MainMenuView.this, "Error al eliminar producto.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            }
        });

        // Añadir Ingrediente en Memoria
        btnAddIngrediente.addActionListener(e -> {
            Insumo ins = (Insumo) cbRecetaInsumos.getSelectedItem();
            String cantText = txtRecetaCantidad.getText().trim();
            if (ins == null || cantText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un insumo e ingrese cantidad.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int cant = Integer.parseInt(cantText);
                if (cant <= 0) {
                    JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                recetaEditandoMap.put(ins.getId(), cant);
                refrescarTablaRecetaEditando();
                txtRecetaCantidad.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cantidad numérica inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Remover Ingrediente en Memoria
        btnDelIngrediente.addActionListener(e -> {
            int row = tablaRecetaIngredientes.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un ingrediente de la tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int insumoId = (int) tablaRecetaIngredientes.getValueAt(row, 0);
            recetaEditandoMap.remove(insumoId);
            refrescarTablaRecetaEditando();
        });

        // Guardar Receta en base de datos
        btnGuardarReceta.addActionListener(e -> {
            String prodCod = txtAdminCodigo.getText().trim();
            if (prodCod.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto a la izquierda antes de guardar la receta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return productoDAO.guardarReceta(prodCod, recetaEditandoMap);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(MainMenuView.this, "¡Receta del producto guardada exitosamente!");
                            cargarRecetaProducto(prodCod);
                        } else {
                            JOptionPane.showMessageDialog(MainMenuView.this, "Error al persistir la receta en base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });
    }

    private void cargarAdminProductos() {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoDAO.listarProductos();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    modeloAdminProductos.setRowCount(0);
                    for (Producto p : productos) {
                        modeloAdminProductos.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getPrecio(), p.getCategoria()});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void cargarAdminInsumosCombo() {
        new SwingWorker<List<Insumo>, Void>() {
            @Override
            protected List<Insumo> doInBackground() throws Exception {
                return insumoDAO.listarInsumos();
            }

            @Override
            protected void done() {
                try {
                    List<Insumo> insumos = get();
                    cbRecetaInsumos.removeAllItems();
                    for (Insumo i : insumos) {
                        cbRecetaInsumos.addItem(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void cargarRecetaProducto(String productoCodigo) {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoDAO.listarProductos();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> list = get();
                    recetaEditandoMap.clear();
                    
                    Producto prodSelected = list.stream()
                        .filter(p -> p.getCodigo().equals(productoCodigo))
                        .findFirst()
                        .orElse(null);
                        
                    if (prodSelected != null) {
                        for (Map.Entry<Insumo, Integer> entry : prodSelected.getReceta().entrySet()) {
                            recetaEditandoMap.put(entry.getKey().getId(), entry.getValue());
                        }
                    }
                    refrescarTablaRecetaEditando();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void refrescarTablaRecetaEditando() {
        modeloRecetaIngredientes.setRowCount(0);
        new SwingWorker<List<Insumo>, Void>() {
            @Override
            protected List<Insumo> doInBackground() throws Exception {
                return insumoDAO.listarInsumos();
            }

            @Override
            protected void done() {
                try {
                    List<Insumo> catInsumos = get();
                    for (Map.Entry<Integer, Integer> entry : recetaEditandoMap.entrySet()) {
                        int id = entry.getKey();
                        int cant = entry.getValue();
                        String nombre = catInsumos.stream()
                            .filter(i -> i.getId() == id)
                            .map(Insumo::getNombre)
                            .findFirst()
                            .orElse("Insumo Desconocido");
                            
                        modeloRecetaIngredientes.addRow(new Object[]{id, nombre, cant});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // =======================================================
    // PANEL DEL GRÁFICO DE BARRAS DE VENTAS 2D (Premium)
    // =======================================================
    class SalesChartPanel extends JPanel {
        private double[] values = {320.50, 480.00, 390.20, 520.00, 610.60, 750.00, 120.00};
        private String[] days = {"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"};

        public SalesChartPanel() {
            setPreferredSize(new Dimension(0, 240));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        public void setValues(double[] values) {
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 30;
            int labelPadding = 20;

            // Dibujar fondo de cuadrículas sutiles
            g2.setColor(new Color(240, 242, 245));
            for (int i = 0; i < 5; i++) {
                int y = padding + i * (height - 2 * padding - labelPadding) / 4;
                g2.drawLine(padding + labelPadding, y, width - padding, y);
            }

            // Encontrar el valor máximo para escalar las barras
            double max = 1.0;
            for (double v : values) {
                if (v > max) max = v;
            }

            // Ancho de las barras
            int barWidth = (width - 2 * padding - labelPadding) / values.length - 24;
            if (barWidth < 5) barWidth = 5;

            // Dibujar barras y etiquetas
            for (int i = 0; i < values.length; i++) {
                int x = padding + labelPadding + i * (width - 2 * padding - labelPadding) / values.length + 12;
                int barHeight = (int) ((values[i] / max) * (height - 2 * padding - labelPadding - 10));
                if (barHeight < 0) barHeight = 0;
                int y = height - padding - labelPadding - barHeight;

                // Gradiente Krusty espectacular (Naranja/Amarillo)
                GradientPaint gp = new GradientPaint(x, y, new Color(255, 204, 51), x, y + barHeight, new Color(230, 126, 34));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                // Dibujar texto del valor arriba de cada barra
                g2.setColor(new Color(44, 62, 80));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String valStr = String.format("%.0f €", values[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 6);

                // Dibujar el día abajo de la barra
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(days[i], x + (barWidth - fm.stringWidth(days[i])) / 2, height - padding / 2);
            }
        }
    }
}
