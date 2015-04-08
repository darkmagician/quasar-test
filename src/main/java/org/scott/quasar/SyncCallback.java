package org.scott.quasar;

import java.util.concurrent.Semaphore;

import org.scott.quasar.QueryService.Callback;

public class SyncCallback implements Callback{
	  private final Semaphore m_lock;
	  private String result;

	  public SyncCallback()
	  {
	    this.result = null;
	    this.m_lock = new Semaphore(1);
	    this.m_lock.acquireUninterruptibly();
	  }
		@Override
		public void result(String result) {
			this.result= result;
			 this.m_lock.release();
			
		}
	
	
	  public boolean checkForResponse()
	  {
	    return this.m_lock.tryAcquire();
	  }

	  public String getResponse()
	  {
	    return this.result;
	  }

	  public void waitForResponse()
	    throws InterruptedException
	  {
	    this.m_lock.acquire();
	    this.m_lock.release();
	  }

}
