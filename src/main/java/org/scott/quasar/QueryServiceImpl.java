/**
 * 
 */
package org.scott.quasar;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
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
	private final ConcurrentNavigableMap<Long, Request> queue = new ConcurrentSkipListMap<Long, Request>();
	
	/** The r. */
	private final Random r = new Random();
	
	private final AtomicInteger counter = new AtomicInteger();
	private int counterDuringMonitor;
	
	/** The period. */
	private final int period;
	private final static int processingNum = 1;
	private final static long interval = 1;
	final List<Thread> processingList = new LinkedList<Thread>();
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

		int i=r.nextInt(period)+12;
		Long expected=System.nanoTime()+i;
		Request request = new Request(id,callback);

		Request existing = queue.get(expected);
		if(existing == null){
			if(null == queue.putIfAbsent(expected, request)){
				return;
			}
		}
		String result = String.valueOf(request.getId()+100);
		counter.incrementAndGet();
		request.getCallback().result(result);
	}
	
	
	public void createProcessingThread(){
		long now = System.nanoTime();
		ConcurrentNavigableMap<Long, Request> olders = queue.headMap(now);
		Iterator<Entry<Long, Request>> it = olders.entrySet().iterator();
		while(it.hasNext()){
			Entry<Long, Request> e = it.next();
			Request request = e.getValue();
			String result = String.valueOf(request.getId()+100);
			counter.incrementAndGet();
			request.getCallback().result(result);
			it.remove();
		}
				

	}
		
	
	public void stop(){
		Iterator<Thread> it = processingList.iterator();
		while(it.hasNext()){
			Thread t = it.next();
			t.interrupt();
			it.remove();
		}
		//System.out.println("Stop Query Service");
	}
	public void start(){
		//System.out.println("Start Query Service");
		for(int i=0;i<processingNum;i++){
			Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						while(true){
							  long start = System.currentTimeMillis();
							  createProcessingThread();
							  if(Thread.interrupted()){
								  return;
							  }
							  long end = System.currentTimeMillis();
							  long sleep = start+interval-end;
							  if(sleep>0){
								 try {
									Thread.sleep(sleep);
								} catch (Exception e) {
									return;
								}
							  }
						  }
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			processingList.add(t);
			t.start();
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
