package locks;

public class MiLock extends LockFalso{

	public MiLock(boolean M) {
		super(M);
	}
	
	@Override
	public void takeLock() {
		while(M) {}
		M = true;
	}
	
	@Override
	public void realeseLock() {
		if(M) M = false;
	}
	

}
