package mensajes;

import java.io.Serializable;


public abstract class Mensaje implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected TipoMensaje tipo;
	
	public TipoMensaje getTipo() {
		return tipo;
	}
	
}
