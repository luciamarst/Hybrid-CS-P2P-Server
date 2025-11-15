package semaforos;

import parte2.Consola;

public class Productor extends Thread {
    private AlmacenTransferencias almacen;
    private final String nombreArchivo;

    public Productor(AlmacenTransferencias almacen, String nombreArchivo) {
        this.almacen = almacen;
        this.nombreArchivo = nombreArchivo;
    }

    @Override
    public void run() {
        try {
            almacen.almacenar(nombreArchivo);
            Consola.imprimir("PRODUCTOR" , nombreArchivo + " almacenado");
        } catch (InterruptedException e) {
            System.err.println("Error en ProductorArchivo: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}