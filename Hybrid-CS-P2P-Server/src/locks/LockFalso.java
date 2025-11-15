package locks;

public class LockFalso {
	protected volatile boolean M;
	
	public LockFalso(boolean M) {
		this.M = M;
	}
	
	public void takeLock() {}
	
	public void realeseLock() {}
}
