package parte2;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import locks.LockRompeEmpate;
import mensajes.*;

public class OyenteServidor extends Thread {
	//Canal
    
    //Flujo
    private ObjectInputStream input;
    private ObjectOutputStream output;
    
    //Cliente
    private Cliente cliente;
    
    //
    private volatile boolean activo = true; // Bandera de control
    
    //Concurrencia
    private final LockRompeEmpate operacionEnCurso;
    
    //Constructor
    public OyenteServidor(Socket socket, ObjectInputStream input,ObjectOutputStream output , Cliente cliente, LockRompeEmpate operacionEnCurso) {
       
        this.cliente = cliente;
        this.output = output;
		this.input = input;
		this.operacionEnCurso = operacionEnCurso;
    }

    /**
     * Funcion encargada de lanzar la ejecucion de OyenteServidor como hilo
     */
    @Override
    public void run() {
    	  try {
    		  //Esperamos mensajes por el flujo de entrada hasta que este activo
              while (activo) {
                  try {
                      Object obj = input.readObject();
                      if (obj!= null) {
                          procesarMensaje((Mensaje) obj);
                      }
                  } catch (SocketException e) {
                      if (activo) {
                          Consola.imprimir(cliente.getUsuario().getNombreUsuario(), 
                              "Conexión cerrada: " + e.getMessage());
                      }
                      break;
                  }
              }
          } catch (Exception e) {
              if (activo) {
                  Consola.imprimir(cliente.getUsuario().getNombreUsuario(),
                      "Error en conexión: " + e.getMessage());
              }
          } finally {
              cerrarConexion();
          }
    }
    
    /**
     * Funcion encargada de tratar cada tipo de mensaje que llega y transmitirlo por los flujos
   
     * @param mensaje
     */
    private void procesarMensaje(Mensaje mensaje) {

        switch (mensaje.getTipo()) {
            case CONFIRMACION:
                procesarConfirmacion((MensajeConfirmacion) mensaje);
                break;
            case SOLICITUD_ARCHIVO:
                procesarSolicitudArchivo((MensajeSolicitudArchivo) mensaje);
                break;
            case RESPUESTA_ARCHIVO:
                procesarRespuestaArchivo((MensajeRespuestaArchivo) mensaje);
                break;
            case INICIO_P2P:
                procesarInicioP2P((MensajeP2P) mensaje);
                break;
            case ERROR:
            	procesarError((MensajeError) mensaje);
            	break;
            case DESCONEXION:
            	cerrarConexion();
                break;
               
            default:
                System.out.println("OyenteServidor: Mensaje no manejado: " + mensaje.getTipo());
        }
    }
    
    /**
     * Procesamos el mensaje de MensajeConfirmacion
     * @param mensaje
     */
    private void procesarConfirmacion(MensajeConfirmacion mensaje) {
        Consola.imprimir(cliente.getUsuario().getNombreUsuario(), "Registro confirmado por el servidor");
        try {
            output.writeObject(mensaje);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Procesamos la solicitud de archivo por parte de un cliente
     * @param mensaje
     * 
     */
    private void procesarSolicitudArchivo(MensajeSolicitudArchivo mensaje) {
        String nombreArchivo = mensaje.getArchivo();
        
        Consola.imprimir(cliente.getUsuario().getNombreUsuario(),"Solicitando archivo " + nombreArchivo);
    	Set<Usuario> usuario = Servidor.getTablaArchivos().get(nombreArchivo);
    	if(!usuario.isEmpty()) {
            MensajeRespuestaArchivo respuesta = new MensajeRespuestaArchivo(usuario.iterator().next(), nombreArchivo, false);
            try {
				output.writeObject(respuesta);
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	else {
    		Consola.imprimir(cliente.getUsuario().getNombreUsuario(),"Nadie tiene el archivo: " + nombreArchivo);
    	}
    }

    
    /**
     * Procesamos la respuesta archivo tras una solicitud de archivo
     * @param mensaje
     * 
     * @implNote
     * 1. Lanzamos un hilo para el cliente receptor del archivo {@link #mandarArchivoP2P()}
     */
    private void procesarRespuestaArchivo(MensajeRespuestaArchivo mensaje) {
    	System.out.println("");
    	Consola.imprimir(cliente.getUsuario().getNombreUsuario(),
    			"Preparando transferencia P2P para: " + mensaje.getArchivo());
    		
    	// Flujo nuevo: Conectar y recibir directamente
    	Consola.imprimir(cliente.getUsuario().getNombreUsuario(),
    			"Recibir archivo Directo");
       
    	new Thread(() -> {
    		 cliente.mandarArchivoP2P(mensaje.getUsuario().getNombreUsuario(),
                     mensaje.getUsuario().getIp(),
                     mensaje.getUsuario().getPuertoP2P(),
                     Set.of(mensaje.getArchivo())
                 );
        }).start();
    }

    /**
     * Procesamos el inicio de la conexion P2P	como servidor por parte del cliente proveedor de un archivo, lanzando un hilo que se encargue de iniciar
     * dicha conexion como servidor.
     * @param mensaje
     */
    private void procesarInicioP2P(MensajeP2P mensaje) {
    	System.out.println("");
        Consola.imprimir(cliente.getUsuario().getNombreUsuario(),
            "Usuario solicitando archivo...Preparando para enviar archivo: " + mensaje.getArchivo());
        new Thread(() -> {
            cliente.iniciarTransferenciaComoServidor(mensaje.getArchivo(), mensaje.isEnvioMasivo());
        }).start();
    }

    /**
     * Procesar mensajes de eror
     * @param mensaje
     */
    private void procesarError(MensajeError mensaje) {
    	Consola.imprimir("SERVIDOR", mensaje.getMotivo());
    	operacionEnCurso.realeseLock(1);
    }
    
    /**
     * Procedimiento encargado del cierra de los flujos de OyenteServidor
     */
    private void cerrarConexion() {
    	try {
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }finally {
	        Thread.currentThread().interrupt(); // Asegurar terminación del hilo
	    }
    }
    
    /**
     * Procedimiento llamados desde cliente cada vez que este quiere salir del servidor, para asegurarnos de detener los flujos de OyenteServidor y que cliente
     * cierre todos sus flujos abiertos, para que no se quede como un hilo "zombie"
     */
    public void detener() {
        this.activo = false;
        cerrarConexion();
    }
    

}