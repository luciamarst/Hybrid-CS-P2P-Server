package mensajes;

import parte2.Usuario;

public class MensajeConsulta extends Mensaje{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
	
	public MensajeConsulta(Usuario user) {
		tipo = TipoMensaje.CONSULTA;
		this.usuario = user;
	}
	public Object getMensajeConsulta() {
		return this;
	}
	
	public Usuario getUsuario() {
		return this.usuario;
	}
}
