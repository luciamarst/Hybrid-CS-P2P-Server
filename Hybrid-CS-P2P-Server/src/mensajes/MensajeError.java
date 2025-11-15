package mensajes;

public class MensajeError extends Mensaje {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String motivo;
    
    public MensajeError(String motivo) {
        tipo = TipoMensaje.ERROR;
        this.motivo = motivo;
    }
    
    public String getMotivo() {
        return motivo;
    }
}