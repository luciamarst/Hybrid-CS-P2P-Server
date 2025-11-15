package base_datos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import monitores.MonitorComplejoEscritorLector;
import parte2.Usuario;

public class TablaUsuarios extends HashMap<Usuario, Set<Object>>{
	MonitorComplejoEscritorLector monitor;
	
	public TablaUsuarios() {
		monitor = new MonitorComplejoEscritorLector();
		try {
			monitor.request_write();
			monitor.release_write();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public Set<Object> put(Usuario key, Set<Object> value) {
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
	public Set<Object> get(Object key) {
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


	public Set<Object> computeIfAbsent(Usuario key, Function<? super Usuario, ? extends Set<Object>> mappingFunction) {
		try {
			monitor.request_write();
	        return super.computeIfAbsent(key, mappingFunction);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
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
	public Set<Object> remove(Object key) {
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


}
