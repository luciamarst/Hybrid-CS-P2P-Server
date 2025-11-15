package monitores;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorComplejoEscritorLector{
	
	 private ReentrantLock lock;
	 private int nr; //Numero de lectores
	 private int dw; //Numeor de escritores esperando
	 private int nw;
	 private boolean hay_datos;
	 private final Condition lectoresEsperando; //Cola explicita de consumidores
	 private final Condition escritoresEsperando; //
	 
	// Para manejar las preguntas y respuestas
	    private String preguntaActual;
	    private String[] opciones;
	 
	public MonitorComplejoEscritorLector() {
		this.lock = new ReentrantLock(true);
		this.nr = this.nw = 0;
		this.dw = 0;
		this.hay_datos =false;
		lectoresEsperando = lock.newCondition();
		escritoresEsperando = lock.newCondition();
		this.preguntaActual = "Pregunta de ejemplo";
        this.opciones = new String[]{"Opción 1", "Opción 2", "Opción 3", "Opción 4"};
	}
	
	
	//Lector solicita lectura
	public void request_read() throws InterruptedException{
		lock.lock();
		
		while(this.nw > 0 || !hay_datos) lectoresEsperando.await();
		this.nr++;
		lock.unlock();
	}

	public void release_read() {
		lock.lock();
		this.nr--;
		
		if(nr == 0) {
			escritoresEsperando.signal();
		}
		lock.unlock();
	}


	public void request_write() throws InterruptedException{
		lock.lock();
		
		while(nr > 0 || this.nw > 0) { escritoresEsperando.await();}
		
		this.nw++;
		lock.unlock();
	}

	
	public void release_write() {
		lock.lock();
		
		hay_datos = true;
		this.nw--;
		escritoresEsperando.signal();
		lectoresEsperando.signalAll();
		
		lock.unlock();
		
	}
	
	// --- MÉTODOS PARA OBTENER LAS PREGUNTAS Y OPCIONES ---

    public String obtenerPregunta() {
        return preguntaActual;
    }

    public String[] obtenerOpciones() {
        return opciones;
    }

    public void actualizarPregunta(String nuevaPregunta, String[] nuevasOpciones) {
        preguntaActual = nuevaPregunta;
        opciones = nuevasOpciones;
    }
}	
