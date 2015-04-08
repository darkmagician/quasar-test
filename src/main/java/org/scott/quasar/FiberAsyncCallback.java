/**
 * 
 */
package org.scott.quasar;

import org.scott.quasar.QueryService.Callback;

import co.paralleluniverse.fibers.FiberAsync;

/**
 * @author scott
 *
 */
public abstract class FiberAsyncCallback extends FiberAsync<String, RuntimeException> implements Callback {

	/* (non-Javadoc)
	 * @see org.scott.quasar.QueryService.Callback#result(java.lang.String)
	 */
	@Override
	public void result(String result) {
		  asyncCompleted(result);
	}


}
