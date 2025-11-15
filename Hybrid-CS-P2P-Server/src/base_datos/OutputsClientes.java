package base_datos;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

import monitores.MonitorComplejoEscritorLector;
import parte2.Usuario;

public class OutputsClientes extends HashMap<Usuario, ObjectOutputStream> {
	MonitorComplejoEscritorLector monitor;
	
	public OutputsClientes() {
		monitor = new MonitorComplejoEscritorLector();
		try {
			monitor.request_write();
			monitor.release_write();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public ObjectOutputStream put(Usuario key, ObjectOutputStream value) {
		try {
			monitor.request_write();
		    return super.put(key, value);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			monitor.release_write();
		}
		return null;
	}
	
	@Override
	public ObjectOutputStream get(Object key) {
		try {
			monitor.request_read();
		    return super.get(key);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			monitor.release_read();
		}
		return null;
	}


	 @Override
	 public ObjectOutputStream computeIfAbsent(Usuario key, Function<? super Usuario, ? extends ObjectOutputStream> mappingFunction) {
	        try {
				monitor.request_write();
				ObjectOutputStream valor = super.get(key);
		        if (valor == null) {
		            valor = mappingFunction.apply(key);
		            if (valor != null) {
		                super.put(key, valor);
		            } 
		        } 
		        return valor;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        finally {
	        	monitor.release_write();
	        }
	        return null;
	    }
	
	@Override
	public boolean containsKey(Object key) {
		try {
			monitor.request_read();
		    return super.containsKey(key);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			monitor.release_read();
		}
		return false;
	}
	
	@Override
	public ObjectOutputStream remove(Object key) {
		try {
			monitor.request_write();
		    return super.remove(key);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			monitor.release_write();
		}
		return null;
	}
	
	 @Override
    public ObjectOutputStream getOrDefault(Object key, ObjectOutputStream defaultValue) {
		 try {
			monitor.request_write();
			ObjectOutputStream valor = super.get(key);
	        if (valor == null && !super.containsKey(key)) {
	            return defaultValue;
	        }
	        return valor;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.release_write();
		}
		 return null;
    }
	 
	@Override
    public Set<Usuario> keySet() {
		try {
			monitor.request_write();
	        return super.keySet();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			monitor.release_read();
		}
		return null;
    }
}
