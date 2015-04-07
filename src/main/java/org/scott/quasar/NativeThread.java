/**
 * 
 */
package org.scott.quasar;

import java.util.concurrent.locks.LockSupport;

/**
 * @author scott
 *
 */
public class NativeThread implements AbstractThread {
	
	
	private final Thread t;
	
	public NativeThread(Runnable run){
		t=new Thread(run);
	}

	/* (non-Javadoc)
	 * @see org.scott.quasar.AbstractThread#start()
	 */
	@Override
	public void start() {
		t.start();
	}

	/* (non-Javadoc)
	 * @see org.scott.quasar.AbstractThread#interrupt()
	 */
	@Override
	public void interrupt() {
		t.interrupt();
	}

	/* (non-Javadoc)
	 * @see org.scott.quasar.AbstractThread#interrupted()
	 */
	@Override
	public boolean interrupted() {
		return t.isInterrupted();
	}

	@Override
	public void park(long timeout) {
		LockSupport.parkNanos(timeout);
	}

	@Override
	public void park(Object blocker, long timeout) throws Exception {
		LockSupport.parkNanos(blocker, timeout);
		
	}

	@Override
	public void unpark(Object blocker) throws Exception {
		LockSupport.unpark((Thread) blocker);
	}

}
