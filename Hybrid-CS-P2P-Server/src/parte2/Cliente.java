package parte2;

import java.io.*;
import java.net.*;
import java.util.*;

import locks.LockBakery;
import locks.LockRompeEmpate;
import mensajes.*;
import semaforos.AlmacenTransferencias;
import semaforos.Productor;

public class Cliente implements Runnable {
	//CONSTANTES
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 5050;
	private static final int MAX_TRANSFERENCIAS_P2P = 5;

    //CONCURRENCIA
    private final LockRompeEmpate operacionEnCurso;
    private AlmacenTransferencias almacen;
    private LockBakery lockBakery;
    private int clientes_conectados;
    private int hilosP2P_activos;
    
    //DATOS Y USUARIOS
    private Usuario usuario;
    private String nombreUsuario;
    private Map<String, Set<Object>> cliente;
    private static Set<Object> archivosLocales;
    
    //SOCKETS Y FLUJOS
    private Socket socketCliente;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private OyenteServidor oyenteServidor;
    private static Scanner scanner;
    
    //P2P
    private ServerSocket serverP2P;
    private static int puertoP2P;
    private boolean p2pActivo;

    //Constructor
    public Cliente(String string, InetAddress inetAddress, int i, HashSet<Object> hashSet) {
    	operacionEnCurso = new LockRompeEmpate(1);
    	archivosLocales = new HashSet<>();
    	nombreUsuario = string;
    	scanner = new Scanner(System.in);
        lockBakery = new LockBakery(MAX_TRANSFERENCIAS_P2P);
        clientes_conectados = 0;
        hilosP2P_activos=0;
        p2pActivo = false;
    }
    
    
    /**
     * Inicialización del socket y flujos apra el cliente, lanzando su hilo OyenteServidor
     */
    public void iniciar() {
    	 try {
             socketCliente = new Socket(SERVIDOR, PUERTO_SERVIDOR);
             output = new ObjectOutputStream(socketCliente.getOutputStream());
             input = new ObjectInputStream(socketCliente.getInputStream());
             

         	 usuario = new Usuario(nombreUsuario,socketCliente.getLocalAddress(),puertoP2P,archivosLocales);
         	 registro();
             
             oyenteServidor = new OyenteServidor(socketCliente, input, output, this,operacionEnCurso);
             new Thread(oyenteServidor).start();
             menu();  // Este debe ser el último paso
             
         } catch (IOException e) {
             Consola.imprimir(nombreUsuario, "Error al conectar: " + e.getMessage());
         }
    }

    /**
     * Procedimiento encargado de llamar a todas las funciones involucradas en el registro de un usuario.
     * 
     * @implNote
     * 1. Carga de archivo pertenecientes al cliente desde clientes.txt {@link #carga()}
     * 2. Registrar cliente en servidor {@link #registrarEnServidor()}
     * 3. Esperar la repsuesta de confirmación de registro por parte del servidor antes de lanzar el hilo OyenteServidor {@link #esperarRegistro()}
     */
    private void registro() {
    	carga();
        registrarEnServidor();
        esperarRegistro();
    }
    
    /**
     * Carga de datos del cliente desde el fichero clientes.txt
     */
    private void carga() {
        cliente = TextFileUtils.getClienteFromTextFile(nombreUsuario);
        
        if (cliente != null) {
            System.out.println("Cliente encontrado: " + nombreUsuario);
            archivosLocales = cliente.get("imagenes");
            System.out.println("Tus archivos:");
            archivosLocales.forEach(System.out::println);
        } else {
            System.out.println("Cliente no encontrado. Se creará uno nuevo.");
            archivosLocales = new HashSet<>();
        }
        
    }
    
    /**
     * Envío del mensaje de registro al servidor para que el cliente y sus archivos sean cargados en las tablas
     */
    private void registrarEnServidor() {
        try {
            MensajeConexion registro = new MensajeConexion(usuario,archivosLocales);
            output.writeObject(registro);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error al registrar en servidor: " + e.getMessage());
        }
    }
    
    /**
     * Esperamos a recibir el mensaje de que nos hemos conectado con el servidor antes de mostrar menu
     */
    private void esperarRegistro() {
        try {
            // Esperamos un mensaje del servidor confirmando el registro
            Object respuesta = input.readObject();

            MensajeConfirmacion mensaje = (MensajeConfirmacion) respuesta;
            if (mensaje.getTipo() == TipoMensaje.CONFIRMACION) {
            	if(mensaje.getEncontrado()) {
            		Consola.imprimir("SERVIDOR", "Ese cliente ya se encuentra conectado, usa otro nombre");
            		System.out.print("Nuevo nombre de usuario: ");
                	this.nombreUsuario = scanner.nextLine();
                	usuario = new Usuario(nombreUsuario,socketCliente.getLocalAddress(),puertoP2P,archivosLocales);
                	registro();
            	}
                System.out.println("Registro exitoso en el servidor.");
            } else {
                System.out.println("Respuesta inesperada del servidor: " + mensaje);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al esperar confirmación de registro: " + e.getMessage());
        }
    }

    /**
     * Menu del servidor, para que cada cliente tome la opción correspondiente
     */
    private void menu() {
    	while (true) {
            
    		 operacionEnCurso.takeLock(0);
	    	 Consola.imprimir(nombreUsuario, "--- MENÚ PRINCIPAL ---");
	         Consola.imprimir(nombreUsuario, "1. Consultar archivos disponibles");
	         Consola.imprimir(nombreUsuario, "2. Descargar archivo");
	         Consola.imprimir(nombreUsuario, "3. Compartir archivo con los demás clientes");
	         Consola.imprimir(nombreUsuario, "4. Salir");
	         Consola.imprimirSinSalto(nombreUsuario, "Opción: ");
	         operacionEnCurso.realeseLock(0);
            

            String opcion = scanner.nextLine();
            
            switch (opcion) {
                case "1":
                    consultarArchivos();
                    break;
                case "2":
                	operacionEnCurso.takeLock(1);
                    descargarArchivo();
                    break;
                case "3":
                	operacionEnCurso.takeLock(1);
                	enviarArchivoTodos();
                	break;
                case "4":
                    salir();
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
            

            
        }
    	
    }
    
    /**
     * Enviar el mensaje de consulta al oyente para que el servidor consulte sus tablas.
     */
    private void consultarArchivos() {
        try {
        	MensajeConsulta mensaje = new MensajeConsulta(usuario);
            output.writeObject(mensaje);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error al consultar archivos: " + e.getMessage());
        }
    }
    
    /**
     * Soliciar el archivo a descargar por parte del cliente y transmitir el mensaje del archivo solicitado
     */
    private void descargarArchivo() {
        Consola.imprimirSinSalto(nombreUsuario,"Nombre del archivo a descargar: ");
        String nombreArchivo = scanner.nextLine();
        
        try {
            output.writeObject(new MensajeSolicitudArchivo(nombreArchivo, usuario));
            output.flush();
        } catch (IOException e) {
            System.err.println("Error al solicitar descarga: " + e.getMessage());
        }
    }
    
    /**
     * El cliente envia un archivo de su pertenencia (obviamente) a todos los clientes que hay en la red.
     */
    private void enviarArchivoTodos() {
    	 
    	 try {
    		 Consola.imprimirSinSalto(nombreUsuario,"Nombre del archivo a enviar: ");
    	        String nombreArchivo = scanner.nextLine();
    	        
    	        if(archivosLocales.contains(nombreArchivo)) {
    	            
    	            // Iniciar servidor P2P en modo masivo
    	            iniciarTransferenciaComoServidor(nombreArchivo, true);
    	            
    	         // Notificar al servidor
    	            output.writeObject(new MensajeEnvioGlobal(nombreArchivo, usuario));
    	            output.flush();
    	            
    	            
    	        } else {
    	            Consola.imprimir(nombreUsuario, "El archivo especificado no te pertenece");
    	            operacionEnCurso.realeseLock(1);
    	        }
             
    	 }catch (IOException e) {
    		 e.printStackTrace();
    	 }

    }
    
    /**
     * Preparamos el puerto P2P al cliente que se conecta
     */
    public void prepararServidorP2P() {
        try {
            // Solo prepara el puerto pero no inicia el servidor
            ServerSocket serverP2P = new ServerSocket(0);
            puertoP2P = serverP2P.getLocalPort(); 
            serverP2P.close(); // Cerramos el socket, lo recrearemos cuando sea necesario
            Consola.imprimir(nombreUsuario, "Puerto P2P asignado: " + puertoP2P);
        } catch (IOException e) {
            Consola.imprimir(nombreUsuario, "Error preparando P2P: " + e.getMessage());
        }
    } 
    
    /**
     * Inicia el servidor P2P solo cuando se necesita enviar archivos.
     * @param archivo Archivo que se va a enviar a través del canal.
     * @param esEnvioMasivo indica si se trata de una descarga (unica conexion P2P entre proveedor y receptor) o bien de un envio global a los clientes
     * 
     * @implNote
     * 1. Si se trata de una descarga normal, simplemente esperamos una conexion por aprte del cliente receptor del archivo y tratamos la descarga.
     *    Si se trata de un envio global, hacemos lo mismo pero un número n de veces, siendo n el número total de clientes - 1.
     *    
     * 2. Una vez diferenciado el tipo de operacion, se espera hasta la conexion de algun cliente en ambas partes y se lanza un hilo de OyenteP2P, el cual
     *    se encarga de gestionar los mensajes de solicitud y confirmacion.
     */
    public void iniciarTransferenciaComoServidor(String archivo, boolean esEnvioMasivo) {
    	try {
    		new Thread(() -> {
    	        try {
    	            serverP2P = new ServerSocket(puertoP2P);
    	            Consola.imprimir(nombreUsuario, "Servidor P2P activo en puerto " + puertoP2P);
    	            p2pActivo = false;
    	            if (esEnvioMasivo) {

    	                almacen = new AlmacenTransferencias();
    	                clientes_conectados = 0;
    	                hilosP2P_activos =0;
    	                while (true && !serverP2P.isClosed()) {
    	                	
    	                	if(p2pActivo) {
    	                		Consola.imprimir(nombreUsuario, "Cierre");
    	                		break;
    	                	}
    	                	
    	                    Socket socketCliente = serverP2P.accept();
    	                    hilosP2P_activos++;
    	                    lockBakery.takeLock(clientes_conectados);
    	                    

    	                	Productor productor = new Productor(almacen, (String) archivo);
    	                	productor.start();
    	                	
    	                	 
	                         new OyenteP2P(socketCliente, this, true, clientes_conectados).start();
	                          
	    	                  clientes_conectados++;
	                         Consola.imprimir(nombreUsuario, 
	                             "Cliente " + clientes_conectados + " conectado para recibir " + archivo);
    	                    
    	                }
    	                

    	            } else {
    	                // Modo normal (un solo cliente)
    	                Socket socketCliente = serverP2P.accept();
    	                new OyenteP2P(socketCliente, this,esEnvioMasivo,0).start();
    	                Consola.imprimir(nombreUsuario, "Cliente conectado para recibir " + archivo);
    	            }
    	            
    	        } catch (IOException e) {
    	        	if (p2pActivo) {
                        Consola.imprimir(nombreUsuario, "ConexionP2P deteniéndose");
                    }
    	        } finally {
    	        	if (p2pActivo) {
	    	        	cerrarConexionP2P();
	    	            Consola.imprimir("CLIENTE", "P2P cerrado correctamente");
	        			this.operacionEnCurso.realeseLock(1);
    	        	}
    	        }
    	    }).start();
    	}
    	finally {
    			
    	}
    }
    
    /**
     * Establece una conexión P2P como cliente solicitante con otro cliente (proveedor) para descargar archivos.
     * 
     * @param inetAddress      Dirección IP del cliente proveedor que tiene los archivos
     * @param puerto           Puerto P2P del cliente proveedor donde escucha conexiones
     * @param archivosSolicitados Conjunto de nombres de archivos solicitados
     * 
     * @implNote Flujo de operación:
     * 1. Se conecta al socket P2P del proveedor
     * 2. Envía una solicitud para el archivo (con el usuario solicitante)
     * 3. Espera confirmación o error del proveedor
     * 4. Procesa los archivos confirmados mediante {@link #recibirContenido(String)}
     * 
     */
    public void mandarArchivoP2P(String proveedor,InetAddress inetAddress, int puerto, Set<Object> archivosSolicitados) {
        try {
        	
            Consola.imprimir(nombreUsuario, "Conectando a " + inetAddress + ":" + puerto);
            Thread.sleep(500);
            Socket socket = new Socket(inetAddress, puerto);
            ObjectOutputStream outP2P = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inP2P = new ObjectInputStream(socket.getInputStream());
            
            
            // Enviar solicitud por cada archivo
            for (Object archivo : archivosSolicitados) {
            	
                outP2P.writeObject(new MensajeSolicitudArchivo((String) archivo, usuario));
                outP2P.flush();
                
                // Esperar confirmación
                
                Object respuesta = inP2P.readObject();
                if (respuesta instanceof MensajeConfirmacion) {
                    MensajeConfirmacion confirmacion = (MensajeConfirmacion) respuesta;
                    for (Object archivoConfirmado : confirmacion.getArchivos()) {
                        recibirContenido((String) archivoConfirmado);
                    }
                    Consola.imprimir(nombreUsuario, "Archivo recibido correctamente");
                } else if (respuesta instanceof MensajeError) {
                    System.err.println("Error: " + ((MensajeError) respuesta).getMotivo());
                }
            }
            
            
           
           if(socket != null) {
        	   socket.close();
        	   Consola.imprimir(nombreUsuario, "Socket cerrado: " + nombreUsuario);
           }
           if(outP2P != null) {
        	   outP2P.close();
        	   Consola.imprimir(nombreUsuario, "Output cerrado: " + nombreUsuario);
           }
           if(inP2P != null) {
        	   inP2P.close();
        	   Consola.imprimir(nombreUsuario, "Input cerrado: " + nombreUsuario);
           }

           this.operacionEnCurso.realeseLock(1);
        } catch (Exception e) {
            System.err.println("Error en transferencia P2P: " + e.getMessage());
        }
    }
    
    
    /**
     * Función encargada de notificar al servidor el nuevo archivo adquirido por el cliente y que este
     * actualice la base de datos del servidor.
     * @param nombreArchivo
     */
    private void notificarNuevoArchivoAlServidor(String nombreArchivo) {
        try {
            Set<Object> archivo = new HashSet<>();
            archivo.add(nombreArchivo);
            MensajeActualizacionArchivos mensaje = new MensajeActualizacionArchivos(this.usuario, archivo);
            
            Consola.imprimir(nombreUsuario, "Notificando al servidor sobre nuevo archivo: " + nombreArchivo);
            
            output.writeObject(mensaje);
            output.flush();
            
            
        } catch (IOException e) {
            System.err.println("Error notificando archivo al servidor: " + e.getMessage());
        } finally {
        }
    }
    
    
    /**
     * Función utilizada para añadir el archivo solicitado por dicho cliente a través de la conexión P2P establceida
     * con el mismo y notificar al servidor la adición de dicho archivo. 
     * 
     * @param nombreArchivo
     * 
     * @implNote Flujo de operación:
     * 1.Añadimos a a los archivos locales del usuario que se ha conectado al servidor el archivo enviado
     * 2.Notificamos al servidor que hemos recibido ese archivo para que actualice las tablas {@link #notificarNuevoArchivoAlServidor(nombreArchivo)}
     *  
     */
    public void recibirContenido(String nombreArchivo) {
    	archivosLocales.add(nombreArchivo);
        Consola.imprimir(nombreUsuario, "Nuevo archivo recibido: " + nombreArchivo);
        notificarNuevoArchivoAlServidor(nombreArchivo);
    }
    
    /**
     * Salir para cada cliente, cerrando la conexion del mismo con el servidor y sus flujos
     */
    private void salir() {
    	try {
    		//Mensjae de desconecion del cliente
            output.writeObject(new MensajeDesconexion(usuario));
            output.flush();
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje de desconexión: " + e.getMessage());
        } finally {
            try {
            	//Detener OyenteServidor
                if (oyenteServidor != null) { // Asegúrate de tener una referencia al OyenteServidor
                    oyenteServidor.detener();
                }
                // Cierra todos los recursos
                if (input != null) input.close();
                if (output != null) output.close();
                if (socketCliente != null) socketCliente.close();
                if (scanner != null) scanner.close();
                
                
            } catch (IOException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
            System.out.println("Sesión finalizada.");
        }
    }
    
    /**
     * Procedimiento de cierre de la conexion P2P establecida desde un cliente proveedor.
     * Se encarga de cerrar el socket con el que fue creado el serverSocket de la conexion
     */
    public void cerrarConexionP2P() {
        try {
            this.p2pActivo = true;
            if (serverP2P != null && !serverP2P.isClosed()) {
            	serverP2P.close(); // Forzará accept() a lanzar SocketException
            	 Consola.imprimir("CLIENTE", "Cerrando conexion");
            }
        } catch (IOException e) {
            Consola.imprimir("ERROR", "Error al cerrar serverSocket: " + e.getMessage());
        }
    }
    
    /**
     * Getter de archivos locales
     */
    public Set<Object> getArchivosLocales() {
        try {
            return new HashSet<>(archivosLocales);
        } finally {
        }
    }
    
    /**
     * Getter de usuario
     */
    public Usuario getUsuario() {
        return this.usuario;
    }
    
    /**
     * Getter del alamcen de transferencias
     * @return AlmacenTransferencias
     */
    public AlmacenTransferencias getAlmacen() {
    	return this.almacen;
    }
    
    /**
     * Getter del lock RompeEmpate
     * Lock rompe-empate utilizado para esperar a terminar las operaciones como descarga o envio global y mostrar el menu.
     * De lo contrario, el menu se muestra antes de que las operaciones terminen.
     * @return LockRompeEmpate
     */
    public LockRompeEmpate takeLockRompeEmpate() {
    	return this.operacionEnCurso;
    }
    
    /**
     * Procedimiento para liberar el lock en el id especificado 
     * Lock bakery utilizado para el envio global.
     */
    public void realeseLockBakery(int clientes) {
        lockBakery.realeseLock(clientes);
    }
    
    /**
     * Procedimiento que decrementa la variable hilosP2P_activos.
     * Este atributo lleva la cuenta de hilos OyenteP2P creados, lo mismo que decir que lelvar el número de conexiones P2P creadas.
     * Usados para sincronizar los mensajes del menu después de los mensajes de las oepraciones.
     */
    public void decrementarHilosP2P() {
    	hilosP2P_activos--;
    }
    
    /**
     * Getter del atributo hilosP2P_activos.
     */
    public int getHilosP2P() {
    	return this.hilosP2P_activos;
    }
    
    /**
     * Funcion run para el lanzamiento de Cliente como Thread.
     */
    @Override
    public void run() {
    	try {
    		System.out.print("Nombre de usuario: " + nombreUsuario);
        	this.nombreUsuario = scanner.nextLine();
        	prepararServidorP2P();  // Preparamos el puerto del canal P2P
            Thread.sleep(500);     // Pequeña pausa para asegurar inicio
            iniciar();            // Luego la conexión al servidor principal
        } catch (InterruptedException e) {
            Consola.imprimir(nombreUsuario, "Error en inicialización: " + e.getMessage());
        }
    }
    
}