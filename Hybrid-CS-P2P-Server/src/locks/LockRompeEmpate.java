package locks;


import glob.EvilEntero;

public class LockRompeEmpate extends Lock{
	private EvilEntero[] last;
	private EvilEntero[] in;
	
	/*No puedo poner private volatile boolean[] marcas porque solo la referencia al arreglo es
	 * volátil, no sus elementos. Es decir, que si hago marcas = new boolean[M];, los cambios 
	 * serán visibles para todos los hilos pero si hago marcas[i] = true, no hay garantía 
	 * de visibilidad inmediate entre hilos, porque sus elementos no son volátiles
	 * 
	*/
	
	public LockRompeEmpate(int M) {
		super(M);
		this.in = new EvilEntero[super.getM()+1];  // Crear un arreglo para 2M hilos
		this.last = new EvilEntero[super.getM()+1];
		for (int i = 0; i < super.getM()+1; i++) {
            in[i] = new EvilEntero(0);  // Inicializar todos los elementos
            last[i] = new EvilEntero(0);
		}
        
	}
	
	@Override
	public void takeLock(int i) {
		for(int j = 1; j < super.getM()+1; j++) {
			last[j]._num = i;
			in[i]._num = j;
			
			for(int k = 1; k < super.getM()+1; k++) {
				if(i != k) {
				    while (in[k]._num >= in[i]._num && last[j]._num == i) { //1*
	
				    }
				}
			}
		}
        
        
        //1*
        /*Comprobamos que la j no es el hilo actual, que el proceso j ha solicitado entrar en la seccion critica 
         * y si es asi pero el turno es del hilo actual, el hilo j ha de esperar
         */
	}
	
	@Override
	public void realeseLock(int i) {
		in[i]._num=0;
	}
	
}
