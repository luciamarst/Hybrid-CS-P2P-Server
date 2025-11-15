package semaforos;


import parte2.Consola;

public class Consumidor extends Thread {
    private AlmacenTransferencias almacen;
    public Consumidor(AlmacenTransferencias almacen) {
        this.almacen = almacen;
    }

    @Override
    public void run() {
        try {
            Object archivo = almacen.extraer();
            Consola.imprimir("CONSUMIDOR" , "Elemento extraido: " + (String)archivo);
        } catch (Exception e) {
            System.err.println("Error en ConsumidorArchivo: " + e.getMessage());
        } finally {
        }
    }
}
