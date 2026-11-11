package krustyburger.models;

public class Empleado {
    private int id;
    private String nombre;
    private String puesto;
    private String clave;

    public Empleado() {
    }

    public Empleado(int id, String nombre, String puesto, String clave) {
        this.id = id;
        this.nombre = nombre;
        this.puesto = puesto;
        this.clave = clave;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
    
    @Override
    public String toString() {
        return nombre + " (" + puesto + ")";
    }
}
