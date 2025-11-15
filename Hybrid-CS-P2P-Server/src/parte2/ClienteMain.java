package parte2;

import java.net.InetAddress;
import java.util.HashSet;


public class ClienteMain {
	//Main encargado del lanzamiento de la clase Cliente
    public static void main(String[] args) {
        try {
        	Cliente cliente = new Cliente("",InetAddress.getLocalHost(), 5001, new HashSet<Object>());
            new Thread(cliente).start();
        } catch (Exception e) {
            System.err.println("Error al iniciar el cliente: " + e.getMessage());
        }
    }
}
