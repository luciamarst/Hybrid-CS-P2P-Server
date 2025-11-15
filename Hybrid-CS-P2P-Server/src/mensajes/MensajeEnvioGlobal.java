package mensajes;

import parte2.Usuario;

public class MensajeEnvioGlobal extends Mensaje{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String archivo;
	private Usuario user_proveedor;
	
	public MensajeEnvioGlobal(String archivo, Usuario user) {
		tipo = TipoMensaje.ENVIO_GLOBAL;
		this.archivo = archivo;
		this.user_proveedor = user;
	}
	
	public String getArchivo() {
		return archivo;
	}
	
	public Usuario getUsuarioProveedor() {
		return user_proveedor;
	}
	
	
}

