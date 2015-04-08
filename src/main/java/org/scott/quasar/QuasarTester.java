/**
 * 
 */
package org.scott.quasar;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;


/**
 * @author scott
 *
 */
@SuppressWarnings("serial")
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class QuasarTester {
	@Param({"100", "400"})
	private int threadSize = 100;
	private final int taskNum = 5000000;
	
	private final QueryServiceImpl service = new QueryServiceImpl(100); 
	private ExecutorService es ;
	private ExecutorService es2  ;
	private FiberScheduler scheduler; //new FiberForkJoinScheduler("Fiber",4);

	

	

	@Setup
	public void setup() throws InterruptedException{

		service.start();
		es = new ThreadPoolExecutor(threadSize,threadSize,1000, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		es2 = new ThreadPoolExecutor(4,4,1000, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100000), new CallerRunsPolicy());
		scheduler = new FiberExecutorScheduler("Fiber2",es2); 
	}
	@TearDown
	public void teardonw(){
		 es.shutdown();
		 es2.shutdown();
		 service.stop();
	}
	
	@Benchmark
	public void syncQuery(){
		final Lock lock = new ReentrantLock();
		final Condition signal = lock.newCondition();
		final AtomicInteger completed= new AtomicInteger();
		final Random r = new Random();
		for(int i=0;i<taskNum;i++){
			final int k = r.nextInt(100000);
			es.execute(new Runnable(){
				
				@Override
				public void run() {
					SyncCallback callback = new SyncCallback();
					service.query(k, callback);
					try {
						callback.waitForResponse();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String result = callback.getResponse();
					if(result == null){
						System.out.println("Empty result");
					}
					int current = completed.incrementAndGet();
					if(taskNum == current){
						try {
							lock.lock();
							signal.signal();
						}finally{
							lock.unlock();
						}
					}
				}
				
			});
		}
		try {
			lock.lock();
			if(completed.get()==taskNum){
				return;
			}
			signal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
	
	
	@Benchmark
	public void fiberQuery() throws SuspendExecution, InterruptedException, ExecutionException{
		final Lock lock2 = new ReentrantLock();
		final Condition signal2 = lock2.newCondition();
		final AtomicInteger completed= new AtomicInteger();
		final AtomicInteger started= new AtomicInteger();
		  final Random r = new Random();
		  
		  
		for(int i=0;i<taskNum;i++){
			final int k = r.nextInt(100000);
			new Fiber<Void>(scheduler){

				/* (non-Javadoc)
				 * @see co.paralleluniverse.fibers.Fiber#run()
				 */
				@Override
				protected Void run() throws SuspendExecution,
						InterruptedException {
					started.incrementAndGet();
					  String result = new FiberAsyncCallback() {
						    protected void requestAsync() {
						    	service.query(k, this);
						    }
						  }.run();
					if(result == null){
						System.out.println("fiber Empty result");
					}	 
					int current = completed.incrementAndGet();
					if(taskNum == current){
						try {
							lock2.lock();
							signal2.signal();
						}finally{
							lock2.unlock();
						}
					}
					return null;
				}
				
			}.start();
		}
		try {
			lock2.lock();

			if(completed.get()==taskNum){
				return;
			}
			signal2.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			lock2.unlock();
		}
	}
	
}
