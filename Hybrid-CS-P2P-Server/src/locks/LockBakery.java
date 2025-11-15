package locks;

import glob.EvilEntero;
import parte2.Consola;

public class LockBakery extends Lock{

	private EvilEntero[] numero;
	
	/*No puedo poner private volatile boolean[] marcas porque solo la referencia al arreglo es
	 * volátil, no sus elementos. Es decir, que si hago marcas = new boolean[M];, los cambios 
	 * serán visibles para todos los hilos pero si hago marcas[i] = true, no hay garantía 
	 * de visibilidad inmediate entre hilos, porque sus elementos no son volátiles
	 * 
	*/
	
	public LockBakery(int M) {
		super(M);
        this.numero = new EvilEntero[super.getM()+1];
		for (int i = 0; i < super.getM()+1; i++) {
            numero[i] = new EvilEntero(0);
        }
        
	}
	
	@Override
	public void takeLock(int i) {
		
        //Busca el número más alto y toma ese número más 1
		
        int max = -1;
        numero[i]._num = 1;
        for(int m = 1; m < super.getM()+1;m++) {
        	int actual = numero[m]._num;
        	if(max < actual) {
        		max = actual;
        	}
        }
        
        numero[i]._num = (max+1); //La ultima persona que entra coge el ticket correspondiente
        boolean impresion = false;
        for(int j = 1; j < super.getM()+1;j++) { //Recorremos todas las personas (procesos) que están esperando para ser atendidos
        	while(j!= i && numero[j]._num > 0 && (numero[i]._num > numero[j]._num || (numero[j]._num == numero[i]._num && j < i))) {
    				if(impresion) {
    					Consola.imprimir("BAKERY","Esperando...");
    					impresion = true;
    				}
        		
        	};
        	//Si hay alguna persona que tiene un menor ticket o que son iguales pero llego antes además de estar esperando, la persona que ha entrado tiene que esperar
        }
        
	}
	
	@Override
	public void realeseLock(int i) {
		numero[i]._num = 0;
	}
	
}
