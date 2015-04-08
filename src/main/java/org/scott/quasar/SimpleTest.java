/**
 * 
 */
package org.scott.quasar;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;

/**
 * @author scott
 *
 */
@SuppressWarnings("serial")
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class SimpleTest {
	private int threadSize = 100;
	private final int taskNum = 1;
	private ExecutorService es ;
	
	private final AtomicInteger completed= new AtomicInteger();
	
	private final Lock lock = new ReentrantLock();
	private final Condition signal = lock.newCondition();
	
	@Setup
	public void setup(){
		
		 es = new ThreadPoolExecutor(threadSize,threadSize,1000, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	@TearDown
	public void teardonw(){
		 es.shutdown();
	}
	
	//@Benchmark
	public void threadTest(){
		 completed.set(0);
		for(int i=0;i<taskNum;i++){
			es.execute(createTask());
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
	public void fiberTest(){
		 completed.set(0);
		for(int i=0;i<taskNum;i++){
			new Fiber<Void>(createSuspendable(i)).start();
		}
		try {
			lock.lock();
			if(completed.get()==taskNum){
				System.out.println("completed!!!");
				return;
			}
			signal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
		
	}
	private SuspendableRunnable createSuspendable(final int id) {
		return new SuspendableRunnable(){
			final Random r = new Random();
			@Override
			public void run()  throws SuspendExecution, InterruptedException{
				System.out.println("start "+id);
				for(int i=10;i>0;i--){
					Fiber.park(r.nextInt(20), TimeUnit.NANOSECONDS);
					org.openjdk.jmh.infra.Blackhole.consumeCPU(12345l);
				}
				int current = completed.incrementAndGet();
				System.out.println("completed "+id+" "+current);
				if(taskNum == current){
					try {
						System.out.println("completedaa");
						lock.lock();
						signal.signal();
						System.out.println("completedbb");
					}finally{
						lock.unlock();
					}
				}
			}
			
		};
	}
	
	
	
	public Runnable createTask(){
		return new Runnable(){
			final Random r = new Random();
			@Override
			public void run() {
				for(int i=10;i>0;i--){
					LockSupport.parkNanos(r.nextInt(200));
					org.openjdk.jmh.infra.Blackhole.consumeCPU(12345l);
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
			
		};
	}
}
