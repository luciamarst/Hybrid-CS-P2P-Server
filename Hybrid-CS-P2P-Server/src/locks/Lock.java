package locks;

public class Lock {
	private int M;
	
	public Lock(int M) {
		this.M = M;
	}
	
	public void takeLock(int i) {}
	public void realeseLock(int i) {}
	public int getM() {return M;}
}
