package mensajes;

import parte2.Usuario;

public class MensajeP2P  extends Mensaje{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
	private String archivo;
	private boolean envio_masivo;
	
	public MensajeP2P(Usuario usuario, String archivo, boolean envio_masivo) {
		tipo = TipoMensaje.INICIO_P2P;
		this.usuario = usuario;
		this.archivo = archivo;
		this.envio_masivo = envio_masivo;
	}
	
	public MensajeP2P getMensajeP2P() {
		return this;
	}
	
	public Usuario getUsuario() {
		return this.usuario;
	}
	
	public String getArchivo() {
		return this.archivo;
	}
	
	public boolean isEnvioMasivo() {
		return envio_masivo;
	}
	
}
