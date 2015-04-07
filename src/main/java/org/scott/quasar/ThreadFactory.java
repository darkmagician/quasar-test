/**
 * 
 */
package org.scott.quasar;

import org.scott.quasar.AbstractThread.ThreadAware;

/**
 * @author scott
 *
 */
public class ThreadFactory {
	private static boolean nativeThread = true;
	
	public static AbstractThread createThread(Runnable run){
		AbstractThread t = nativeThread? new NativeThread(run):new FiberThread(run);
		if(run instanceof ThreadAware){
			((ThreadAware) run).setCurrentThread(t);
		}
		return t;
	}
}
