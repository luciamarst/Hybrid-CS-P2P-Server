package parte2;

import java.io.*;
import java.net.Socket;
import java.util.Set;

import mensajes.*;
import semaforos.AlmacenTransferencias;
import semaforos.Consumidor;

public class OyenteP2P extends Thread {
	
    private final Socket socket;
    private final Cliente cliente;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean masivo;
    private int clientes;
    //Concurrencia
    
    public OyenteP2P(Socket socket, Cliente cliente, boolean masivo, int id) {
        this.socket = socket;
        this.cliente = cliente;
        this.masivo = masivo;
        this.clientes = id;
    }
    
    @Override
    public void run() {
        try {
            Consola.imprimir("OYENTE P2P", "Dentro OYENTEP2P");
            
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            
            //Esperamos hasta recibir un mensaje del cliente receptor
            Object obj = input.readObject();

            if (obj instanceof MensajeSolicitudArchivo) {
                MensajeSolicitudArchivo mensaje = (MensajeSolicitudArchivo) obj;
                Consola.imprimir("OYENTE P2P", "Solicitud recibida para archivo: " + mensaje.getArchivo());
                
                if (cliente.getArchivosLocales().contains(mensaje.getArchivo())) { 
                    if(masivo) {
	                    // Usar el patrón productor-consumidor para la transferencia
                    	AlmacenTransferencias almacen = cliente.getAlmacen();
	                    
	                    // Si es un envío masivo, el productor ya habrá almacenado el archivo
	                    // Solo necesitamos el consumidor para esta conexión
	                    Consumidor consumidor = new Consumidor(almacen);
	                    consumidor.start();
	                    
	                    // Esperar a que el consumidor complete su trabajo
	                    consumidor.join();
                    }
                 // Confirmar que tenemos el archivo
                    output.writeObject(new MensajeConfirmacion(cliente.getUsuario(), Set.of(mensaje.getArchivo()),false));
                    output.flush();
                } else {
                    output.writeObject(new MensajeError("Archivo no encontrado"));
                    output.flush();
                }
                
            }
            
            if(masivo) cliente.decrementarHilosP2P();
        } catch (Exception e) {
            System.err.println("Error en OyenteP2P");
        }
        finally {
        	//Antes de acabar el proceso OyenteP2P, dependiendo de la operacion dada, liberamos un lock u otro, además de llamar a cerrarConexionP2P()
            if(!masivo) {
    			cliente.cerrarConexionP2P();
            	cliente.takeLockRompeEmpate().realeseLock(1);
            }
        	if(masivo) {
        		if(cliente.getHilosP2P() == 0) {
        			cliente.cerrarConexionP2P();
        		}
        		cliente.realeseLockBakery(clientes);
        	}
        	
        }
    }
}