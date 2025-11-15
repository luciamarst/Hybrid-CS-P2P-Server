package base_datos;

import java.util.ArrayList;

import monitores.MonitorComplejoEscritorLector;

public class ListaClientes extends ArrayList<String>{
	MonitorComplejoEscritorLector monitor;
	
	public ListaClientes() {
		monitor = new MonitorComplejoEscritorLector();
		try {
			monitor.request_write();
			monitor.release_write();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public boolean contains(Object o) {
		try {
			monitor.request_read();
	        return super.contains(o);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.release_read();
		}
		return false;
    }

    @Override
    public boolean add(String s) {
    	try {
			monitor.request_write();
	        return super.add(s);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.release_write();
		}
		return false;
    }

    @Override
    public boolean remove(Object o) {
    	try {
			monitor.request_write();
	        return super.remove(o);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.release_write();
		}
		return false;
    }
}
