package parte2;

import java.io.*;
import java.net.Socket;
import java.util.*;

import mensajes.*;

public class OyenteCliente extends Thread {
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean conectado = true;
    
    public OyenteCliente(Socket socket,ObjectOutputStream out, ObjectInputStream in) {
        this.socket = socket;
        this.output = out;
		this.input = in;
    }

    /**
     * Procedimiento encargada de lanzar OyenteCliente como hilo
     */
    @Override
    public void run() {
        try {
            while (true) {
            	Object obj;
            	if ((obj = input.readObject()) != null) {  // Verifica si hay datos disponibles
                    procesarMensaje((Mensaje) obj);
                } else {
                    Thread.sleep(100);  // Pequeña pausa para evitar consumo excesivo de CPU
                    break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Conexión cerrada por el servidor");
        } catch (Exception e) {
            if (conectado) {
                System.err.println("Error en OyenteCliente: " + e.getMessage());
            }
        } finally {
            cerrarConexion();
        }
    }

    /**
     * Procedimiento encargado de procesar los mensajes que llegan por el flujo de entrada
     * @param mensaje
     */
    private void procesarMensaje(Mensaje mensaje) {
        switch (mensaje.getTipo()) {
            case CONEXION:
                procesarConexion((MensajeConexion) mensaje);
                break;
                
            case SOLICITUD_ARCHIVO:
                procesarSolicitudArchivo((MensajeSolicitudArchivo) mensaje);
                break;
                
            case CONSULTA:
                procesarConsulta((MensajeConsulta) mensaje);
                break;
            
            case ACTUALIZACION_ARCHIVOS:
            	procesarActualizacionArchivos((MensajeActualizacionArchivos) mensaje);
            	break;
                
            case ENVIO_GLOBAL:
            	procesarEnvioGlobal((MensajeEnvioGlobal) mensaje);
            	break;
            	
            case DESCONEXION:
            	conectado = false;
                procesarDesconexion((MensajeDesconexion) mensaje);
                break;
                
            default:
                System.out.println("OyenteCliente: Mensaje no manejado: " + mensaje.getTipo());
        }
    }
    
    /**
     * Procedimiento encargado de tratar el mensaje de conexion (registro) por parte de un cliente
     * @param mensaje
     * 
     * @implNote
     * 1. Comprobamos que ese usuario no esta ya conectado al servidor
     * 2. Actualizamos las tablas y agregamos el cliente a la lsita de clientes conectados si corresponde
     * 3. Enviamos el mensaje de confirmación de conexion
     */
    private void procesarConexion(MensajeConexion mensaje) {
        try {
        	System.out.println("Conexion oyente cliente");
            Usuario usuario = mensaje.getUsuario();
            boolean enServidor = true;
            if(!Servidor.clienteEnServidor(usuario)) {
	            Servidor.registrarUsuario(usuario, mensaje.getArchivos(), output);
	            Servidor.agregarClienteConectado(usuario, socket);
	            enServidor = false;
            }
            output.writeObject(new MensajeConfirmacion(usuario, mensaje.getArchivos(), enServidor));
            output.flush();
        } catch (IOException e) {
            System.err.println("Error confirmando conexión: " + e.getMessage());
        }
        
    }

    /**
     * Procedimiento encargado de procesar una solicitud de archivo para la operación 2. Descagar archivo
     * En esta operacion, el proveedor es el cliente que tiene el archivo que yo como cliente quiero, mientras que yo soy el que lo recibe.
     * Por esto mismo, mandamos por el flujo del proveedor un mensajeP2P para que inicie la conexion como servidor y yo como cliente mando
     * un mensaje MensajeRespuestaArchivo para ejecutar la función encargada de tratar la recepcion de un archivo
     * @param mensaje
     */
    private void procesarSolicitudArchivo(MensajeSolicitudArchivo mensaje) {
    	Set<Usuario> proveedores = Servidor.buscarProveedoresArchivo(mensaje.getArchivo());
        
        if (!proveedores.isEmpty()) {
            Usuario proveedor = proveedores.iterator().next();
            try {
                // Notificar al cliente que tiene el archivo para que active su servidor P2P
            	ObjectOutputStream outProveedor = Servidor.getOutputStreamUsuario(proveedor);
                
            	//Enviamos mensaje a través del flujo del proovedor para que inicie el servidor p2p
                outProveedor.writeObject(new MensajeP2P(mensaje.getUsuario(), mensaje.getArchivo(), false));
                outProveedor.flush();
                
                // Responder al solicitante con los datos del proveedor
                output.writeObject(new MensajeRespuestaArchivo(proveedor, mensaje.getArchivo(), false));
                output.flush();
                
            } catch (IOException e) {
                System.err.println("Error respondiendo solicitud: " + e.getMessage());
            }
        } else {
        	try {
				output.writeObject(new MensajeError("Archivo no disponible: " + mensaje.getArchivo()));
			} catch (IOException e) {
				e.printStackTrace();
			}
            
        }
    }

    /**
     * Procedimiento encargado de responder a la operaicion de consultar de las tablas de datos.
     * 
     * @param mensaje
     */
    private void procesarConsulta(MensajeConsulta mensaje) {
        Map<Usuario, Set<Object>> tabla = Servidor.getTablaUsuarios();
		
		if (tabla == null || tabla.isEmpty()) {
		    System.out.println("No hay datos disponibles");
		    return;
		}

		 Consola.imprimir("SISTEMA", "\n--- RESULTADO DE CONSULTA ---");
		for (Map.Entry<Usuario, Set<Object>> entry : tabla.entrySet()) {
		    Usuario usuario = entry.getKey();
		    Set<Object> archivos = entry.getValue();
		    
		    System.out.println("\nUsuario: " + usuario.getNombreUsuario());
		    System.out.println("Archivos compartidos (" + archivos.size() + "):");
		    
		    if (archivos.isEmpty()) {
		    	 Consola.imprimir("SISTEMA", "  (No tiene archivos compartidos)");
		    } else {
		        archivos.forEach(archivo -> System.out.println("  - " + archivo));
		    }
		}
		System.out.println("----------------------------\n");
    }

    /**
     * Procedimiento encargado de actualizar las bases de datos del servidor.
     * Empleada tras recibir archivo de otros clientes
     * @param mensaje
     */
    private void procesarActualizacionArchivos(MensajeActualizacionArchivos mensaje) {
    	
        System.out.println("Procesando actualización de archivos");
		
		Usuario usuario = mensaje.getUsuario();
		Set<Object> archivos = mensaje.getArchivos();
		
		for(Object archivo: archivos) {
		    Servidor.actualizarTablaArchivos(usuario, (String) archivo, true);
		}
		
    }
    
    /**
     * Procedimiento similar a {@link #procesarSolicitudArchivo(MensajeSolicitudArchivo)} pero siendo en este caso yo como cliente el proveedor.
     * Este procedimiento responde a la operacion 3. Envio global, de forma que enviamos un archivo dado a los demás clientes conectados al servidor.
     * 
     * La función que nos inicia como servidor es llamada en la propia funcion de esta operacion, para no complicar el flujo.
     * Al llegar aqui, enviamos mensajes de MensajeRespuestaArchivo por cada cliente, para que ejecuten la funcion mandarArchivoP2P.
     * @param mensaje
     */
    private void procesarEnvioGlobal(MensajeEnvioGlobal mensaje) {
    	Set<Usuario> receptores = Servidor.socketsTodosUsuariosMenosYo(mensaje.getUsuarioProveedor().getNombreUsuario());
    	// Preparar el almacén de transferencias
        
        for (Usuario receptor : receptores) {
            try {

                ObjectOutputStream outReceptor = Servidor.getOutputStreamUsuario(receptor);
                outReceptor.writeObject(new MensajeRespuestaArchivo(mensaje.getUsuarioProveedor(),mensaje.getArchivo(),true));
                outReceptor.flush();
            } catch (IOException e) {
                Consola.imprimir("OYENTE CLIENTE", "Error notificando a " + receptor.getNombreUsuario());
            }
        }
    }
   
    /**
     * Procedimiento encargado de la desconexion de OenteCliente, dejando todos los flujos y sokcet cerrado y llamando a la funcion de eliminar el cliente
     * conectado dado.
     * @param mensaje
     */
    private void procesarDesconexion(MensajeDesconexion mensaje) {
    	    try {
    	        // 1. Cerrar flujos primero
    	        if (input != null) input.close();
    	        if (output != null) output.close();
    	        if (socket != null && !socket.isClosed()) socket.close();
    	        
    	        // 2. Notificar al servidor
    	        Servidor.eliminarClienteConectado(mensaje.getUsuario());
    	    } catch (IOException e) {
    	        Consola.imprimir("ERROR", "Error al procesar desconexión: " + e.getMessage());
    	    } finally {
    	        Thread.currentThread().interrupt(); // Asegurar terminación del hilo
    	    }
    	
    }

    /**
     * Cierre de la conexion
     */
    private void cerrarConexion() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }
}