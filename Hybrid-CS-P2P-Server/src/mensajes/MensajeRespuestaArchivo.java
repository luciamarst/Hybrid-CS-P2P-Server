package mensajes;

import parte2.Usuario;

public class MensajeRespuestaArchivo extends Mensaje{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
	private String archivo;
	private boolean envio_masivo;
	
	public MensajeRespuestaArchivo(Usuario usuario, String archivo, boolean envio) {
		tipo = TipoMensaje.RESPUESTA_ARCHIVO;
		this.usuario = usuario;
		this.archivo = archivo;
		this.envio_masivo = envio;
	}
	
	public Usuario getUsuario() {
		return this.usuario;
	}
	
	public Object getArchivo() {
		return this.archivo;
	}
	
	public boolean isEnvioMasivo() {
		return envio_masivo;
	}
	
}
