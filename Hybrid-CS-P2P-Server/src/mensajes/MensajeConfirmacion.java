package mensajes;

import java.util.Set;
import parte2.Usuario;

public class MensajeConfirmacion extends Mensaje{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
	private Set<Object>  archivos;
	private boolean encontrado;
	public MensajeConfirmacion(Usuario usuario, Set<Object>  set, boolean encontrado) {
		tipo = TipoMensaje.CONFIRMACION;
		this.usuario = usuario;
		this.archivos = set;
		this.encontrado = encontrado;
	}
	
	public Object getMensajeConfirmacion() {
		return this;
	}
	
	public Usuario getUsuario() {
		return this.usuario;
	}
	
	public Set<Object>   getArchivos(){
		return this.archivos;
	}
	
	public boolean getEncontrado() {
		return this.encontrado;
	}
}
