package semaforos;

import java.util.concurrent.Semaphore;

public class AlmacenTransferencias {
	// Mapa estático para garantizar una única instancia por proveedor
    
    private final Semaphore empty = new Semaphore(1);
    private final Semaphore full = new Semaphore(0);
    private Object archivo;

    // Privatizar constructor
    public AlmacenTransferencias() {}

    public void almacenar(Object archivo) throws InterruptedException {
        empty.acquire();
        this.archivo = archivo;
        full.release();
    }

    public Object extraer() throws InterruptedException {
        full.acquire();
        Object archivo = this.archivo;
        empty.release();
        return archivo;
    }
}
