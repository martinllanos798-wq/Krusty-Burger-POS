package krustyburger.views;

import krustyburger.daos.EmpleadoDAO;
import krustyburger.models.Empleado;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {

    private JTextField txtNombre;   // Mejora #5: campo nombre en lugar de ID
    private JPasswordField txtClave;
    private JButton btnEntrar;
    private EmpleadoDAO empleadoDAO;

    public LoginView() {
        empleadoDAO = new EmpleadoDAO();
        initUI();
    }

    private void initUI() {
        setTitle("Krusty Burger - Acceso");
        setSize(450, 460);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Panel superior (Header)
        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(new Color(44, 62, 80));
        panelHeader.setPreferredSize(new Dimension(0, 100));
        panelHeader.setLayout(new GridBagLayout());

        JLabel lblTitulo = new JLabel("KRUSTY POS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitulo.setForeground(Color.WHITE);
        panelHeader.add(lblTitulo);
        add(panelHeader, BorderLayout.NORTH);

        // Panel central (Formulario)
        JPanel panelCentro = new JPanel();
        panelCentro.setLayout(new GridBagLayout());
        panelCentro.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0;

        JLabel lblInstrucciones = new JLabel("Ingrese sus credenciales");
        lblInstrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblInstrucciones.setForeground(Color.GRAY);
        gbc.gridy = 0;
        panelCentro.add(lblInstrucciones, gbc);

        // Campo Nombre de usuario — Mejora #5
        JLabel lblNombre = new JLabel("Nombre de usuario");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 20, 5, 20);
        panelCentro.add(lblNombre, gbc);

        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtNombre.setPreferredSize(new Dimension(250, 40));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 20, 15, 20);
        panelCentro.add(txtNombre, gbc);

        // Campo Contraseña
        JLabel lblClave = new JLabel("Contraseña");
        lblClave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 20, 5, 20);
        panelCentro.add(lblClave, gbc);

        txtClave = new JPasswordField();
        txtClave.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtClave.setPreferredSize(new Dimension(250, 40));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 20, 20, 20);
        panelCentro.add(txtClave, gbc);

        // Botón Entrar
        btnEntrar = new JButton("INICIAR SESIÓN");
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnEntrar.setBackground(new Color(46, 204, 113));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setPreferredSize(new Dimension(250, 45));
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 20, 10, 20);
        panelCentro.add(btnEntrar, gbc);

        add(panelCentro, BorderLayout.CENTER);

        // Permitir login también pulsando Enter en el campo de clave
        txtClave.addActionListener(e -> validarLogin());
        txtNombre.addActionListener(e -> txtClave.requestFocus());
        btnEntrar.addActionListener(e -> validarLogin());

        // Mejora #6: botón Demo ELIMINADO
    }

    private void validarLogin() {
        String nombre = txtNombre.getText().trim();
        String clave  = new String(txtClave.getPassword()).trim();

        if (nombre.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor, complete todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Empleado empleado = empleadoDAO.login(nombre, clave);

        if (empleado != null) {
            MainMenuView menu = new MainMenuView(empleado);
            menu.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Nombre de usuario o contraseña incorrectos.", "Error de acceso", JOptionPane.ERROR_MESSAGE);
        }
    }
}
