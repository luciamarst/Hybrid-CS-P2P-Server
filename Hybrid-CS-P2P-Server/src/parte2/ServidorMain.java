package parte2;

import locks.MiLock;
import monitores.MonitorComplejoEscritorLector;

public class ServidorMain {
	
	//Main encargado del lanzamiento de la clase Servidor
    public static void main(String[] args) {
        try {
        	MonitorComplejoEscritorLector monitor = new MonitorComplejoEscritorLector();
            Thread servidorThread = new Thread(new Servidor(monitor,new MiLock(false)));
            servidorThread.start();
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}
