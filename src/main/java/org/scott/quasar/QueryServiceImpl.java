/**
 * 
 */
package org.scott.quasar;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class QueryServiceImpl.
 *
 * @author scott
 */
public class QueryServiceImpl implements QueryService {
	
	
	/** The queue. */
	private final ConcurrentNavigableMap<Long, Collection<Request>> queue = new ConcurrentSkipListMap<Long, Collection<Request>>();
	
	/** The r. */
	private final Random r = new Random();
	
	private final AtomicInteger counter = new AtomicInteger();
	private int counterDuringMonitor;
	
	/** The period. */
	private final int period;
	
	
	/**
	 * @param period
	 */
	public QueryServiceImpl(int period) {
		super();
		this.period = period;
	}

	public int getCounterDuringMonitor(){
		return counterDuringMonitor;
	}
	
	public void startMonitor(){
		counter.set(0);
	}
	
	public void stopMonitor(){
		counterDuringMonitor = counter.get();
	}
	
	/* (non-Javadoc)
	 * @see org.scott.quasar.QueryService#query(int, org.scott.quasar.QueryService.Callback)
	 */
	@Override
	public void query(int id, Callback callback) {

		int i=r.nextInt(period);
		Long expected=System.currentTimeMillis()+i;
		Request request = new Request(id,callback);

		Collection<Request> list = queue.get(expected);
		if(list == null){
			list = new ConcurrentLinkedQueue<Request>();
			Collection<Request>  existing = queue.putIfAbsent(expected, list);
			if(existing != null){
				list = existing;
			}
		}
		list.add(request);
	}
	
	
	public void createProcessingThread(long now){
		ConcurrentNavigableMap<Long, Collection<Request>> olders = queue.headMap(now);
		Iterator<Entry<Long, Collection<Request>>> it = olders.entrySet().iterator();
		while(it.hasNext()){
			Entry<Long, Collection<Request>> e = it.next();
			Collection<Request> requests = e.getValue();
			for(Request request: requests){
				int result = request.getId()+100;
				counter.incrementAndGet();
				request.getCallback().result(result);
			}
			it.remove();
		}
				

	}
		
	
	
	/**
	 * The Class Request.
	 */
	static class Request{
		
		/** The id. */
		final private int id;
		
		/** The callback. */
		final private Callback callback;
		
		/**
		 * Instantiates a new request.
		 *
		 * @param id the id
		 * @param callback the callback
		 */
		public Request(int id, Callback callback) {
			super();
			this.id = id;
			this.callback = callback;
		}
		
		/**
		 * Gets the id.
		 *
		 * @return the id
		 */
		public int getId() {
			return id;
		}
		
		/**
		 * Gets the callback.
		 *
		 * @return the callback
		 */
		public Callback getCallback() {
			return callback;
		}
	}

}
