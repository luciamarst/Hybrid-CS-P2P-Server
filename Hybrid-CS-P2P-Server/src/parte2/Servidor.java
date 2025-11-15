package parte2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

import base_datos.ClientesConectados;
import base_datos.ListaClientes;
import base_datos.OutputsClientes;
import base_datos.TablaArchivos;
import base_datos.TablaUsuarios;
import locks.MiLock;
import mensajes.*;
import monitores.MonitorComplejoEscritorLector;

public class Servidor implements Runnable {
	//CONSTANTES
    private static final int PUERTO_SERVIDOR = 5050;
    private static final int MAXIMO_CLIENTES = 5;
	
    //Tablas de contenidos del servidor (Bases de datos)
    private static TablaUsuarios tablaUsuarios;
    private static TablaArchivos tablaArchivos;
    private static ClientesConectados clientesConectados;
    
    //Tener los flujos de salida de los clientes a los que enviar en envioGlobal
    private static OutputsClientes outputsClientes;
    //Lista de clientes conectados pero identificados por su string (nombre de usuario)
    private static ListaClientes listaClientes;
    
    //flujos
    private static ServerSocket serverSocket;
    
    //Concurrencia 
    private static MiLock lock;
    private static Semaphore semaforo_clientes;
    
    //Bucle de escucha de nuevos clientes
    private static boolean servidorAbierto;
    
    //Constructor
    public Servidor(MonitorComplejoEscritorLector monitor, MiLock lock) {
    	semaforo_clientes= new Semaphore(MAXIMO_CLIENTES);
    	this.servidorAbierto = false;
    	this.lock = lock;
    	tablaUsuarios = new TablaUsuarios();
    	tablaArchivos = new TablaArchivos();
    	clientesConectados = new ClientesConectados();
    	outputsClientes = new OutputsClientes();
    	listaClientes = new ListaClientes();
    }
    
    /**
     * Inicialización del servidor
     * Creamos el serverSocket del servidor y esperamos la conexion de nuevos clientes.
     * Conforme los clientes se conectan vamos creando  escuchas continuas de cada uno de ellos.
     * 
     * Para manejar el cierre del servidor cuando ya no hay clientes conectado, manejo la excepcion SocketException,
     * de forma que cuando se produce, si el servidor no esta abierto, lo vamos deteniendo y finalmente llamamos a la
     * funcion cerrarConexion().
     */
    private void iniciar() {
    	try {
    		lock.takeLock(); // Bloquear para modificar estado
            serverSocket = new ServerSocket(PUERTO_SERVIDOR);
            servidorAbierto = true;
            lock.realeseLock();
            
            
            Consola.imprimir("SERVIDOR", "Iniciado en puerto " + PUERTO_SERVIDOR);

            while (true) {
                lock.takeLock();
                boolean activo = servidorAbierto;
                lock.realeseLock();

                if (!activo) break;

                try {
                	try {
        				semaforo_clientes.acquire();
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
                    Socket socketCliente = serverSocket.accept();
                    
                    Consola.imprimir("SERVIDOR", "Nuevo cliente conectado: " + socketCliente.getInetAddress());
                    
                    new Thread(new OyenteCliente(socketCliente,
                        new ObjectOutputStream(socketCliente.getOutputStream()),
                        new ObjectInputStream(socketCliente.getInputStream())
                       )).start();
                    
                } catch (SocketException e) {
                    lock.takeLock();
                    if (!servidorAbierto) {
                        Consola.imprimir("SERVIDOR", "Servidor deteniéndose...");
                        lock.realeseLock();
                        break;
                    }
                    lock.realeseLock();
                    Consola.imprimir("ERROR", "Error en accept(): " + e.getMessage());
                }
            }
        } catch (IOException e) {
            Consola.imprimir("ERROR", "Error al iniciar servidor: " + e.getMessage());
        } finally {
            cerrarConexion();
            Consola.imprimir("SERVIDOR", "Servidor cerrado correctamente");
        }
    }
    
    /**
     * Actualizamos la base de datos del servidor con la informacion del nuevo cliente registrado
     * 
     * @param usuario, Usuario a registrar en la base de datos
     * @param archivos, Conjunto de archivo que pertenecen al cliente que vamos a registrar (los del txt)
     * @param out, flujo de salida del cliente a registrar
     */
    public static void registrarUsuario(Usuario usuario, Set<Object> archivos, ObjectOutputStream out) {
        
        tablaUsuarios.put(usuario, archivos);
		listaClientes.add(usuario.getNombreUsuario());
		outputsClientes.put(usuario, out);
		for (Object archivo : archivos) {
		    tablaArchivos.computeIfAbsent((String) archivo, k -> new HashSet<>()).add(usuario);
		}
    }
    
    /**
     * Funcion que comprueba si un cliente que quiere registrarse con un nombre de usuario X puede hacerlo o no.
     * Si ese usuario X ya esta conectado al servidor no podrá conectarse.
     * 
     * @param usuario
     * @return listaClientes.contains(usuario.getNombreUsuario())
     */
	public static boolean clienteEnServidor(Usuario usuario) {
    	return listaClientes.contains(usuario.getNombreUsuario());
    }

	/**
	 * Busca los clientes que son propietarios de un archivo dado
	 * @param nombreArchivo
	 * @return Set<Usuario>, usuarios propietarios
	 */
    public static Set<Usuario> buscarProveedoresArchivo(String nombreArchivo) {
        return tablaArchivos.getOrDefault(nombreArchivo, Collections.emptySet());
    }
    
    /**
     * Funcion encargada de devolver el conjunto de clientes conectados al servidor excepto el cliente proveedor
     * del archivo.
     * @param nombreUsuario
     * @return Set<Usuario>
     */
    public static Set<Usuario> socketsTodosUsuariosMenosYo(String nombreUsuario){
    	Set<Usuario> usuarios = new HashSet<Usuario>();
    	for (Usuario usuario : clientesConectados.keySet()) {
			if(!nombreUsuario.equals(usuario.getNombreUsuario())) {
				usuarios.add(usuario);
			}
		}
		return usuarios;
    }

    /**
     * Getter de la tabla de usuario 
     * @return  Map<Usuario, Set<Object>>
     */
    public static Map<Usuario, Set<Object>> getTablaUsuarios() {
    	return Collections.unmodifiableMap(tablaUsuarios);
    }
    
    /**
     * Getter de la tabla de archivos
     * @return Map<String, Set<Usuario>> 
     */
    public static Map<String, Set<Usuario>> getTablaArchivos() {
    	Map<String, Set<Usuario>> tabla = null;
        return Collections.unmodifiableMap(tablaArchivos);
    }
    
    /**
     * Getter de los clientes conectados
     * @return Map<Usuario, Socket>
     */
    public static Map<Usuario, Socket> getClientesConectados() {
    	Map<Usuario, Socket> tabla = null;
    	return Collections.unmodifiableMap(clientesConectados);
		
    }
    
    /**
     * Getter del flujo de salida de un usuario dado
     * @param usuario
     * @return ObjectOutputStream
     */
    public static ObjectOutputStream getOutputStreamUsuario(Usuario usuario) {
    	ObjectOutputStream output = null;
    	output = outputsClientes.get(usuario);
		return output;
    }
    
    
    /**
     * Procedimiento encargado de actualizar las bases de datos del servidor para un cliente y archivo dados
     * @param usuario
     * @param archivo
     * @param agregar
     */
    public static void actualizarTablaArchivos(Usuario usuario, String archivo, boolean agregar) {
    	
        try {
            if (agregar) {
                tablaUsuarios.computeIfAbsent(usuario, k -> new HashSet<>()).add(archivo); //Hecho
                tablaArchivos.computeIfAbsent(archivo, k -> new HashSet<>()).add(usuario);
            } else {
                if (tablaUsuarios.containsKey(usuario)) { //Hecho
                    tablaUsuarios.get(usuario).remove(archivo);
                }
                if (tablaArchivos.containsKey(archivo)) {
                    tablaArchivos.get(archivo).remove(usuario);
                }
            }

        } catch (Exception e) {
			e.printStackTrace();
        }
    }

    /**
     * Funcion encargada de agregar a la lista de clientes conectados un nuevo cliente, almacenando tanto
     * su usuario como su socket
     * @param usuario
     * @param socket
     */
    public static void agregarClienteConectado(Usuario usuario, Socket socket) {
    	if(!clientesConectados.containsKey(usuario)) clientesConectados.put(usuario, socket);
    }

    /**
     * Procedimiento encargado de eliminar un cliente conectado
     * 
     * @implNote
     * 1. Obtenemos el socket de cliente que se va a desconectar
     * 2. Enviamos un mensaje de desconexion
     * 3. Eliminamos de la lista de clientes conectados al cliente dado
     * 4. Eliminamos la información de este cliente de las demás tablas del servidor {@link #eliminarClienteTablas(Usuario)}
     * 5. Si ya no quedan clientes conectados al servidor, los cerramos
     */
    public static void eliminarClienteConectado(Usuario usuario) {
    	try {

            Consola.imprimir("SERVIDOR", usuario.getNombreUsuario() + " desconectado del servidor");
            Socket socket = clientesConectados.get(usuario);
            // Intentar notificar al cliente antes de eliminarlo
            try {
                if (socket != null && !socket.isClosed()) {
                    OutputStream output = socket.getOutputStream();
                    ObjectOutputStream objOut = new ObjectOutputStream(output);
                    objOut.writeObject(new MensajeDesconexion(usuario));
                    objOut.flush();
                }
            } catch (IOException e) {
                // El cliente ya se desconectó, no es un error crítico
                Consola.imprimir("AVISO", "No se pudo notificar a " + usuario.getNombreUsuario());
            }
            
            // Eliminar de las estructuras de datos
            clientesConectados.remove(usuario);
            eliminarClienteTablas(usuario);
            
            semaforo_clientes.release();
            
            // Verificar si hay clientes restantes
            if(clientesConectados.isEmpty()) {
            	Consola.imprimir("SERVIDOR", "No hay clientes conectados, cerrando servidor");
            	cerrarConexion();
            }
            
        } catch (Exception e) {
            Consola.imprimir("ERROR", "Excepción inesperada al eliminar cliente: " + e.getMessage());
        }
    }
    
    /**
     * Procedimiento encargado de eliminar de las tablas la información de un usuario dado.
     * @param usuario
     */
    private static void eliminarClienteTablas(Usuario usuario) {
    	Set<Object> objetosUsuario = tablaUsuarios.get(usuario);
    	
    	for(Object obj: objetosUsuario) {
    		String archivo = (String) obj;
    		Set<Usuario> archivoUsuario = tablaArchivos.get(archivo);
    		archivoUsuario.remove(usuario);
    	}
    	
    	tablaUsuarios.remove(usuario); //Hecho
    	listaClientes.remove(usuario.getNombreUsuario());
    	
    }
    
    /**
     * Cierre de los recursos del servidor. 
     * Cerramos el socket.
     */
    private static void cerrarConexion() {
    	lock.takeLock();
        try {
            servidorAbierto = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Forzará accept() a lanzar SocketException
            }
           
        } catch (IOException e) {
            Consola.imprimir("ERROR", "Error al cerrar serverSocket: " + e.getMessage());
        } finally {
            lock.realeseLock();
        }
    }
      
    /**
     * Funcion encargada de lanzar el hilo
     */
    @Override
    public void run() {
		iniciar();
    }
    
    
}