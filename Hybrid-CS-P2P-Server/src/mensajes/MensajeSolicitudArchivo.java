package mensajes;

import parte2.Usuario;

public class MensajeSolicitudArchivo extends Mensaje {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nombreArchivo;
	private Usuario usuario;
    // Constructor, getters y setters
	
	public MensajeSolicitudArchivo(String nombreArchivo, Usuario usuario) {
		tipo = TipoMensaje.SOLICITUD_ARCHIVO;
		this.nombreArchivo =nombreArchivo;
		this.usuario = usuario;
	}
	
	public String getArchivo() {
		return this.nombreArchivo;
	}
	
	public Usuario getUsuario() {
		return this.usuario;
	}
}
