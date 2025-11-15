package parte2;
import java.io.*;
import java.util.*;

public class TextFileUtils {
	/**
	 * Clase encargada de leer de clientes.txt los archivos de los que parte un cliente dado
	 */
	
	private static final String DIRECTORIO = "clientes.txt";
	private static final String EXTENSION = "imagenes";
    // Método para obtener los datos del cliente desde un archivo de texto
    public static Map<String, Set<Object>> getClienteFromTextFile(String clienteNombre) {
        try {
            // Crear un BufferedReader para leer el archivo de texto
            BufferedReader reader = new BufferedReader(new FileReader(DIRECTORIO));
            String line;
            Map<String, Set<Object>> clienteMap = null;
            Set<Object> imagenes = new HashSet<>();
            
            while ((line = reader.readLine()) != null) {
                // Si encontramos el nombre del cliente, comenzamos a leer las imágenes
                if (line.equals(clienteNombre + ":")) {
                    clienteMap = new HashMap<>();
                    
                    // Leer las siguientes líneas (imágenes del cliente)
                    while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                        imagenes.add(line.trim());
                    }
                    clienteMap.put(EXTENSION, imagenes);
                    break; // Salir después de encontrar al cliente
                }
            }

            // Cerrar el BufferedReader
            reader.close();

            // Si no encontramos el cliente, devolvemos null
            if (clienteMap == null) {
                System.out.println("Cliente no encontrado.");
                return null;
            }

            return clienteMap; // Devolver los datos del cliente
        } catch (IOException e) {
            e.printStackTrace();
            return null; // En caso de error al leer el archivo
        }
    }


    
}

