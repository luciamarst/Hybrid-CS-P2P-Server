package mensajes;

import parte2.Usuario;

public class MensajeDesconexion extends Mensaje{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
	
	public MensajeDesconexion(Usuario user) {
		tipo = TipoMensaje.DESCONEXION;
		this.usuario = user;
	}
	public Object getMensajeDesconexion() {
		return this;
	}
	
	public Usuario getUsuario() {
		return this.usuario;
	}
}
