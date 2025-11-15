package monitores;

import java.util.concurrent.locks.ReentrantLock;

public interface Monitor {
    // Métodos para los incrementos y decrementos
    void incremento();
    void decremento();
    
    //Productor y consumidor
    void producir(int elemento) throws InterruptedException;
    int consumir() throws InterruptedException;
    
    //Lector y escritor
    void request_read() throws InterruptedException;
    void release_read();
    
    void request_write() throws InterruptedException;
    void release_write();
    
    int getNumero();
    int[] getVector();
}

