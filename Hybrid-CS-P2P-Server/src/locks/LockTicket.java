package locks;

import java.util.concurrent.atomic.AtomicInteger;

import glob.EvilEntero;

public class LockTicket extends Lock{
	private int numero;
	private AtomicInteger number;
	private int next;
	
	/*No puedo poner private volatile boolean[] marcas porque solo la referencia al arreglo es
	 * volátil, no sus elementos. Es decir, que si hago marcas = new boolean[M];, los cambios 
	 * serán visibles para todos los hilos pero si hago marcas[i] = true, no hay garantía 
	 * de visibilidad inmediate entre hilos, porque sus elementos no son volátiles
	 * 
	*/
	
	public LockTicket() {
		super(0);
        this.numero = 0;
        number = new AtomicInteger(1);
        next = 1;
	}
	
	@Override
	public void takeLock(int i) {
		
        //Consola.imprimir("TICKET","Proceso con id " + i + " toma el turno " + number.get());
        numero  = (number.getAndIncrement()); // Instruccion fetch and add. Cogemos un ticket unico. 
        //FA(number,1) -> El proceso actual se queda con el antiguo valor de number pero number incrementa en 1, de forma que no se pierde ningun valor y evitando las carreras de datos
        
        while(numero != next) {}//Consola.imprimir("TICKET", "Esperando a que otro cliente salga");}; //Esperamos hasta que sea el turno de i
        
	}
	
	//No hace falta que sea atomico porque solo va a entrar un proceso a realese
	//No existe la posibilidad de que dos procesos tengan el mismo ticket y por tanto no puede haber dos procesos realizando la op next = next+1
	@Override
	public void realeseLock(int i) {
		//Una vez que ha salido de la seccion critica, se avanza el turno al siguiente en la cola
		next = next+1;
	}
	
}
