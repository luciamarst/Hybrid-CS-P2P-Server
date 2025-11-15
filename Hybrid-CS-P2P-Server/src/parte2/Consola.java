package parte2;

import locks.LockTicket;

public class Consola {
	/**
	 * Clase encargada de tratar la consola sin que se solapen mensajes, ya que de lo contrario, al ser un recurso compartido entre varios hilos
	 * podrían solaparse los mensajes.
	 * 
	 * Por ello, como máximo, se permite la impresión de a lo sumo 1 mensaje.
	 */
	private static final int MAX_MENSAJES = 1;
	private static LockTicket lockConsola = new LockTicket(); // Lock para controlar el flujo de mensajes
	private static int contador_mensajes = -1;
	
    // Colores
    private static final String RESET = "\u001B[0m";
    private static final String[] COLORS = {
        "\u001B[36m", // Cian
        "\u001B[33m", // Amarillo
        "\u001B[32m", // Verde
        "\u001B[35m"  // Morado
    };

    public static void imprimir(String nombreCliente, String mensaje) {
    	contador_mensajes++;
        int colorIndex = Math.abs(nombreCliente.hashCode()) % COLORS.length;
        String color = COLORS[colorIndex];
        
        if(contador_mensajes > -1) {
	        lockConsola.takeLock(contador_mensajes); // Cogemos el lock
	        try {
	            System.out.println(color + "[" + nombreCliente + "]" + RESET + " " + mensaje);
	        } finally {
	            lockConsola.realeseLock(contador_mensajes); // Soltamos
	            contador_mensajes--;
	        }
        }
    }
    
    public static void imprimirSinSalto(String nombreCliente, String mensaje) {
    	contador_mensajes++;
        int colorIndex = Math.abs(nombreCliente.hashCode()) % COLORS.length;
        String color = COLORS[colorIndex];
        
        if(contador_mensajes > -1) {
	        lockConsola.takeLock(contador_mensajes); // Cogemos el lock
	        try {
	            System.out.print(color + "[" + nombreCliente + "]" + RESET + " " + mensaje);
	        } finally {
	            lockConsola.realeseLock(contador_mensajes); // Soltamos
	            contador_mensajes--;
	        }
        }
    }
}