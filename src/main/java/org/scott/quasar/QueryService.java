/**
 * 
 */
package org.scott.quasar;

/**
 * The Interface QueryService.
 *
 * @author scott
 */
public interface QueryService {

	
	/**
	 * Query.
	 *
	 * @param id the id
	 * @param callback the callback
	 */
	public void query(int id,Callback callback); 
	
	/**
	 * The Interface Callback.
	 */
	interface Callback{
		
		/**
		 * Result.
		 *
		 * @param result the result
		 */
		public void result(String result);
	}
}
