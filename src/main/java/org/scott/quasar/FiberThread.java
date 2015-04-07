/**
 * 
 */
package org.scott.quasar;

import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;

/**
 * @author scott
 *
 */
public class FiberThread implements AbstractThread {
	
	private final Fiber<Void> t;
	
	public FiberThread(final Runnable run){
		t=new Fiber<Void>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			/* (non-Javadoc)
			 * @see co.paralleluniverse.fibers.Fiber#run()
			 */
			@Override
			protected Void run() throws SuspendExecution, InterruptedException {
				run.run();
				return null;
			}};
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

	/* (non-Javadoc)
	 * @see org.scott.quasar.AbstractThread#park(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void park(long timeout) throws SuspendExecution {
		Fiber.park(timeout, TimeUnit.NANOSECONDS);
	}


	@Override
	public void park(Object blocker, long timeout) throws Exception {
		Fiber.park(blocker,timeout, TimeUnit.NANOSECONDS);
		
	}


	@Override
	public void unpark(Object blocker) throws Exception {
		t.unpark(blocker);
	}


}
