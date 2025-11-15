package base_datos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import monitores.MonitorComplejoEscritorLector;
import parte2.Usuario;

public class TablaArchivos extends HashMap<String, Set<Usuario>>{
	 MonitorComplejoEscritorLector monitor;
		
		public TablaArchivos() {
			monitor = new MonitorComplejoEscritorLector();
			try {
				monitor.request_write();
				monitor.release_write();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		@Override
		public Set<Usuario> put(String key, Set<Usuario> value) {
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
		public Set<Usuario> get(Object key) {
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
		 public Set<Usuario> computeIfAbsent(String key, Function<? super String, ? extends Set<Usuario>> mappingFunction) {
		        try {
					monitor.request_write();
					Set<Usuario> valor = super.get(key);
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
		public Set<Usuario> remove(Object key) {
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
	    public Set<Usuario> getOrDefault(Object key, Set<Usuario> defaultValue) {
			 try {
				monitor.request_write();
				Set<Usuario> valor = super.get(key);
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
}
