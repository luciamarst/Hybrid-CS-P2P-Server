package monitores;

public class Elecciones {

	private static int N;
	private static int HILOS;
	
	public Elecciones(int n, int hilos) {
		N = n;
		HILOS = hilos;
	}
	
	public static Thread[] eleccion(TipoSincronizacion tipo, Monitor m) {
		Thread[] hilos;
		
		if(tipo.equals(TipoSincronizacion.INCREMENTO_DECREMENTO)) {
			hilos = new Thread[HILOS];
			Elecciones.iniciarHilosIncrementosDecrementos(hilos, m);
		}
		else if(tipo.equals(TipoSincronizacion.PRODUCTOR_CONSUMIDOR)) {
			hilos = new Thread[HILOS];
			Elecciones.iniciarHilosProductoresConsumidores(hilos,m);
		}
		else {
			hilos = new Thread[HILOS*N];
			Elecciones.iniciarHilosLectorEscritor(hilos,m);
		}
		
		return hilos;
	}
	
	public static void iniciarHilosIncrementosDecrementos(Thread[] hilos, Monitor m) {
		for(int i = 0; i < HILOS;i++) {
			if(i%2 == 0) {
		    	hilos[i] = new Thread(()-> {
		    		for(int j = 0; j < N; j++) {
		    			m.incremento();
		    		}
		    	});
	    	}
	    	else {
	    		hilos[i] = new Thread(()-> {
		    		for(int j = 0; j < N; j++) {
		    			m.decremento();
		    		}
		    	});
	    	}
		}
	}
	public static void iniciarHilosProductoresConsumidores(Thread[] hilos, Monitor m) {
		for(int i = 0; i < HILOS;i++) {
			if(i%2 == 0) {
		    	hilos[i] = new Thread(()-> {
		    		for(int j = 0; j < N; j++) {
		    			try {
							m.producir(j);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		    		}
		    	});
	    	}
	    	else {
	    		hilos[i] = new Thread(()-> {
		    		for(int j = 0; j < N; j++) {
		    			try {
							@SuppressWarnings("unused")
							int elemen_consumido = m.consumir();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    			
		    		}
		    	});
	    	}
		}
	}
	public static void iniciarHilosLectorEscritor(Thread[] hilos, Monitor m) {
		for(int i = 0; i < HILOS*N;i++) {
			if(i%5 != 0) {
		    	hilos[i] = new Thread(()-> {
		    			try {
		    				m.request_read();
							m.release_read();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		    		
		    	});
	    	}
	    	else {
	    		hilos[i] = new Thread(()-> {
		    			try {
							m.request_write();
							m.release_write();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    	});
	    	}
		}
	}
}
