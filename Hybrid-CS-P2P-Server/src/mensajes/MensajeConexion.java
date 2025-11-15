package mensajes;

import java.util.Set;
import parte2.Usuario;

public class MensajeConexion extends Mensaje{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
	private Set<Object> archivos;
	public MensajeConexion(Usuario usuario, Set<Object> archivosLocales) {
		tipo = TipoMensaje.CONEXION;
		this.usuario = usuario;
		this.archivos = archivosLocales;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	
	public Set<Object> getArchivos(){
		return archivos;
	}
}

