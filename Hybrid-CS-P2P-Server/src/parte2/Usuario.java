package parte2;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Set;

public class Usuario implements Serializable{
	/**
	 * Clase encargada de representa a cada cliente que se conecta al servidor como usuario, creando un objeto que contiene
	 * su nombre de usuario, direccion ip, puerto del servidor, puerto p2p y sus archivos.
	 */
	private static final long serialVersionUID = 1L;
	private String nombreUsuario; //Nombre de usuario
    private InetAddress ip; //Ip del usuario, la misma para todos porque ejecuto desde mismo PC
    private int puertoServidor; //Puerto al que están conectados
    private int puertoP2P;
    private Set<Object> archivosCompartidos; // Archivos que tiene el usuario y está dispuesto a compartir

    
    public Usuario(String nombre, InetAddress ip,int puertoP2P, Set<Object> objetos) {
    	this.nombreUsuario = nombre;
        this.ip = ip;
        this.archivosCompartidos = objetos;
        this.puertoP2P = puertoP2P;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public InetAddress getIp() {
        return ip;
    }
    
    public int getPuerto() {
    	return this.puertoServidor;
    }
    
    public int getPuertoP2P() {
        return puertoP2P;
    }
    
    public void setPuerto(int puerto) {
    	this.puertoServidor = puerto;
    }
    
    public Set<Object> getArchivosCompartidos() {
        return archivosCompartidos;
    }
    
    public void setObjetos(Set<Object> objetos) {
        this.archivosCompartidos = objetos;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario usuario)) return false;
        return puertoServidor == usuario.puertoServidor &&
                Objects.equals(nombreUsuario, usuario.nombreUsuario) &&
                Objects.equals(ip, usuario.ip);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nombreUsuario, ip, puertoServidor);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "nombreUsuario='" + nombreUsuario + '\'' +
                ", ip='" + ip + '\'' +
                ", archivosCompartidos=" + archivosCompartidos +
                '}';
    }
}
