package krustyburger;

import krustyburger.views.LoginView;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (Exception e) {
                System.err.println("No se pudo cargar el tema oscuro: " + e.getMessage());
            }

            // Iniciar la primera vista (Login)
            LoginView login = new LoginView();
            login.setVisible(true);
        });
    }
}
