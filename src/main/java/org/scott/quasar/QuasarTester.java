/**
 * 
 */
package org.scott.quasar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.scott.quasar.QueryService.Callback;

/**
 * @author scott
 *
 */
public class QuasarTester {

	
	private final static long interval = 100;
	private final static int processingNum = 1;
	private final static int clientNum=4;
	private final static double tps=15000;
	private final static long duration = 10000;
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String [] args) throws InterruptedException{
		final QueryServiceImpl service = new QueryServiceImpl(10);
		final List<AbstractThread> processingList = new ArrayList<AbstractThread>();
			 
		
		for(int i=0;i<processingNum;i++){
			AbstractThread t = ThreadFactory.createThread(new AbstractThread.ThreadAware(){
				@Override
				public void run() {
					try {
						while(true){
							  long start = System.currentTimeMillis();
							  service.createProcessingThread(start);
							  if(current.interrupted()){
								  return;
							  }
							  long end = System.currentTimeMillis();
							  long sleep = start+interval-end;
							  if(sleep>0){
								  current.park(sleep*1000);
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
		
		final List<AbstractThread> clientList = new ArrayList<AbstractThread>();
		
		
		
		for(int i=0;i<clientNum;i++){
			AbstractThread t = ThreadFactory.createThread(new AbstractThread.ThreadAware(){
				private final Random r = new Random();
				@Override
				public void run() {
					  try {
						final long start = System.currentTimeMillis();
						  final double myTPS = tps/clientNum;
						  long number = 0;
						  long expected = 0;
						  while(true){
							
							  while(expected>=number){
								  int id = r.nextInt(100000);
								  Callback callback=new Callback(){

									@Override
									public void result(int result) {
										// TODO Auto-generated method stub
										
									}};
								  service.query(id, callback);
								  number++;
							  }
							  if(current.interrupted()){
								 
								  return;
							  }
							  long now = System.currentTimeMillis();
							  expected = (long) (myTPS/1000*(now-start));
							  if(expected<number){
								  current.park(100000);
							  }
							  
						  }
					} catch ( Exception e) {
						e.printStackTrace();
					}
				}
			});
			clientList.add(t);
			t.start();
		}
		
				
		
		service.startMonitor();
		Thread.sleep(duration);
		service.stopMonitor();
		for(AbstractThread f:clientList){
			f.interrupt();
		}
		for(AbstractThread f:processingList){
			f.interrupt();
		}
		System.out.println("total "+service.getCounterDuringMonitor());

	}
}
