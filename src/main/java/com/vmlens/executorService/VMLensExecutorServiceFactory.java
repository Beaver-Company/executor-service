package com.vmlens.executorService;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import com.vmlens.executorService.internal.ListNode;
import com.vmlens.executorService.internal.manyToOne.ConcurrentLinkedList;
import com.vmlens.executorService.internal.manyToOne.LinkedNode;
import com.vmlens.executorService.internal.manyToOne.QueueManyWriters;
import com.vmlens.executorService.internal.manyToOne.QueueNode;
import com.vmlens.executorService.internal.manyToOne.QueueSingleReader;
import com.vmlens.executorService.internal.oneToMany.QueueSingleWriter;
import com.vmlens.executorService.internal.oneToMany.ToBeConsumed;
import com.vmlens.executorService.internal.service.DispatcherThread;
import com.vmlens.executorService.internal.service.EventBusImpl;
import com.vmlens.executorService.internal.service.ExecutorServiceImpl;
import com.vmlens.executorService.internal.service.WorkerThreadForEventConsumer;
import com.vmlens.executorService.internal.service.WorkerThreadForRunnable;

/**
 * 
 * 
 * @author thomas
 *
 */

public class VMLensExecutorServiceFactory {
	
	
	public static ExecutorService create(int threadCount)
	{
		return create( threadCount, true);
	}

	
	
	
	static ExecutorService create(int threadCount, boolean startThreads)
	{
		 ConcurrentLinkedList<Runnable> writingThreads = new ConcurrentLinkedList<Runnable>();
	
		 QueueSingleReader<Runnable> queueSingleReader = new QueueSingleReader<Runnable>(writingThreads);	
	
		 
		 WorkerThreadForRunnable[] workerThreadArray = new WorkerThreadForRunnable[threadCount];
		 
		 for(int i = 0 ; i < threadCount ; i++)
		 {
			 workerThreadArray[i] = new WorkerThreadForRunnable();
		 }
		 
		 final ListNode<ToBeConsumed<LinkedNode<Runnable>>> start = new ListNode<ToBeConsumed<LinkedNode<Runnable>>>(workerThreadArray[0].getToBeConsumed(), workerThreadArray[0].getId());
		 ListNode<ToBeConsumed<LinkedNode<Runnable>>> current = start;
		 
		 for(int i = 1 ; i < threadCount ; i++)
		 {
			 current.next =  new ListNode<ToBeConsumed<LinkedNode<Runnable>>>(workerThreadArray[i].getToBeConsumed(), workerThreadArray[i].getId());
			 current =  current.next;
		 }
		 
		 
		 
		 
		 QueueSingleWriter<LinkedNode<Runnable>> queueSingleWriter = new QueueSingleWriter<LinkedNode<Runnable>>(start);
		 DispatcherThread dispatcherThread = new DispatcherThread(queueSingleWriter,queueSingleReader);
		 
		
		 QueueManyWriters<Runnable> queueManyWriters = new QueueManyWriters<Runnable>(writingThreads,dispatcherThread);
		 
		 if( startThreads )
		 {
			 dispatcherThread.start();
			 
			 for(int i = 0 ; i < threadCount ; i++)
			 {
				 workerThreadArray[i].start();
			 }
		 }
		 
		
		 
		 
		 
		 
		 
		 
		 
		 
		
	 	return new ExecutorServiceImpl(queueManyWriters,dispatcherThread);
	}
	
	
	
	
	
	
	public static <T> EventBus<T> createEventBus(Iterator<Consumer<Iterator<T>>>  consumerIterator )
	{
		 ConcurrentLinkedList<T> writingThreads = new ConcurrentLinkedList<T>();
		
		 QueueSingleReader<T> queueSingleReader = new QueueSingleReader<T>(writingThreads);	
	
		 ListNode<ToBeConsumed<LinkedNode<T>>> start   = null;
		 ListNode<ToBeConsumed<LinkedNode<T>>> current = null;
		 
		 while(  consumerIterator.hasNext()  )
		 {
			 Consumer<Iterator<T>> consumer = consumerIterator.next();
			 WorkerThreadForEventConsumer<T>  workerThreadForEventConsumer = new WorkerThreadForEventConsumer<T>(consumer); 
			 
			 if( start == null )
			 {
				 start =  new ListNode<ToBeConsumed<LinkedNode<T>>>(workerThreadForEventConsumer.getToBeConsumed()  , workerThreadForEventConsumer.getId());
				 current = start;
			 }
			 else
			 {
				 current.next = new ListNode<ToBeConsumed<LinkedNode<T>>>(workerThreadForEventConsumer.getToBeConsumed()  , workerThreadForEventConsumer.getId());
				 current =  current.next;
			 }
			 
			 workerThreadForEventConsumer.start();
			 
			 
			 
		 }
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
		
		 
		 
		 
		 QueueSingleWriter<LinkedNode<T>> queueSingleWriter = new QueueSingleWriter<LinkedNode<T>>(start);
		 DispatcherThread dispatcherThread = new DispatcherThread(queueSingleWriter,queueSingleReader);
		 
		
		 
		 dispatcherThread.start();
		 
		 
		 QueueManyWriters<T> queueManyWriters = new QueueManyWriters<T>(writingThreads,dispatcherThread);
		 
		 
		 
	
		 
		 
		 
		
	 	return new EventBusImpl<T>(queueManyWriters,dispatcherThread);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
