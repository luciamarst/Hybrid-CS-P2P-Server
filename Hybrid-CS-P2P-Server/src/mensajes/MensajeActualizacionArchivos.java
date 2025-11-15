package mensajes;

import java.util.Set;

import parte2.Usuario;

public class MensajeActualizacionArchivos extends Mensaje {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
    private Set<Object> archivo;
    
    public MensajeActualizacionArchivos(Usuario usuario, Set<Object> archivo) {
        this.tipo = TipoMensaje.ACTUALIZACION_ARCHIVOS;
        this.usuario = usuario;
        this.archivo = archivo;
    }

    // Getters
    public Usuario getUsuario() {
        return usuario;
    }
    
    public Set<Object> getArchivos() {
    	return archivo;
    }


}