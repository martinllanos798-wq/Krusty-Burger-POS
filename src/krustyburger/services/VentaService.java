package krustyburger.services;

import krustyburger.models.Insumo;
import krustyburger.models.Producto;
import krustyburger.exceptions.StockInsuficienteException;

import java.util.HashMap;
import java.util.Map;

/**
 * Mejora #4: el VentaService ya NO escribe en la base de datos.
 * Solo valida el stock en memoria y calcula los descuentos necesarios.
 * El descuento real se realiza dentro de la transacción de PedidoDAO.registrarVenta().
 */
public class VentaService {

    /**
     * Valida que hay stock suficiente para todos los ingredientes del producto
     * y acumula los descuentos en el mapa proporcionado.
     *
     * @param producto         producto a vender (con su receta cargada)
     * @param cantidadVendida  unidades del producto
     * @param descuentos       mapa acumulador: insumoId -> total a descontar
     * @throws StockInsuficienteException si algún ingrediente no tiene stock suficiente
     */
    public void validarYCalcularDescuentos(Producto producto, int cantidadVendida,
                                           Map<Integer, Integer> descuentos)
            throws StockInsuficienteException {

        for (Map.Entry<Insumo, Integer> entry : producto.getReceta().entrySet()) {
            Insumo ingrediente          = entry.getKey();
            int cantPorProducto         = entry.getValue();
            int cantTotal               = cantPorProducto * cantidadVendida;

            if (ingrediente.getStock() < cantTotal) {
                throw new StockInsuficienteException(
                    "Stock insuficiente para: " + ingrediente.getNombre() +
                    ". Stock actual: " + ingrediente.getStock() +
                    ", requerido: " + cantTotal
                );
            }

            // Acumular el descuento (varios productos pueden compartir el mismo insumo)
            descuentos.merge(ingrediente.getId(), cantTotal, Integer::sum);
        }
    }

    /**
     * Calcula los descuentos para toda la lista del carrito de una vez.
     *
     * @param detallesCarrito  mapa productoId -> (producto, cantidad)
     * @return mapa insumoId -> cantidad total a descontar
     * @throws StockInsuficienteException si algún ingrediente falta
     */
    public Map<Integer, Integer> calcularDescuentosTotales(
            Map<String, Object[]> detallesCarrito) throws StockInsuficienteException {

        Map<Integer, Integer> descuentos = new HashMap<>();
        for (Object[] entry : detallesCarrito.values()) {
            Producto p  = (Producto) entry[0];
            int cantidad = (int) entry[1];
            validarYCalcularDescuentos(p, cantidad, descuentos);
        }
        return descuentos;
    }
}
