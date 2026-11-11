package krustyburger.models;

import java.util.HashMap;
import java.util.Map;

public class Producto {
    private String codigo;
    private String nombre;
    private double precio;
    private String categoria;

    // Mapa que define la receta: Insumo -> Cantidad necesaria
    private Map<Insumo, Integer> receta;

    public Producto() {
        this.receta = new HashMap<>();
    }

    public Producto(String codigo, String nombre, double precio, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.receta = new HashMap<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Map<Insumo, Integer> getReceta() {
        return receta;
    }

    public void setReceta(Map<Insumo, Integer> receta) {
        this.receta = receta;
    }

    public void agregarIngrediente(Insumo ingrediente, int cantidad) {
        this.receta.put(ingrediente, cantidad);
    }

    @Override
    public String toString() {
        return nombre + " - " + precio + " €";
    }
}
