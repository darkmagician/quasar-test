/**
 * 
 */
package org.scott.quasar;


/**
 * The Interface AbstractThread.
 *
 * @author scott
 */
public interface AbstractThread {

	
	/**
	 * Start.
	 */
	public void start();
	
	/**
	 * Interrupt.
	 */
	public void interrupt();
	
    /**
     * Interrupted.
     *
     * @return true, if successful
     */
    public boolean interrupted();
    
    /**
     * Park.
     *
     * @param timeout the timeout
     * @throws Exception 
     */
    public void park(long timeout) throws Exception;
  
    
    /**
     * Park.
     *
     * @param timeout the timeout
     * @throws Exception 
     */
    public void park(Object blocker, long timeout) throws Exception;
    
    /**
     * Park.
     *
     * @param timeout the timeout
     * @throws Exception 
     */
    public void unpark(Object blocker) throws Exception;   
    
    /**
     * @author scott
     *
     */
    public abstract class ThreadAware implements Runnable{
    	protected AbstractThread current;
    	/**
    	 * @param current
    	 */
    	public void setCurrentThread(AbstractThread current){
    		this.current = current;
    	}
    }
}
