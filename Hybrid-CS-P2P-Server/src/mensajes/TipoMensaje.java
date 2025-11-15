package mensajes;

public enum TipoMensaje {
	//Inicio de la conexion Cliente-Servidor
	CONEXION,
	CONFIRMACION,
	
	// Comunicación cliente-servidor para P2P
    SOLICITUD_ARCHIVO,  // Cliente pregunta por un archivo
    RESPUESTA_ARCHIVO,  // Servidor responde con ubicación
    
    // Comunicación P2P
    INICIO_P2P,         // Servidor inicia transferencia
    
    // ERROR
    ERROR,               // Para errores específicos

    //Opciones del menu del usuario
    CONSULTA, DESCARGA,ENVIO_GLOBAL, DESCONEXION,
    
    //Una vez descargado un archivo, avisar al servidor de que actualice su base de datos
    ACTUALIZACION_ARCHIVOS
}
