package com.fathzer.games.util.exec;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A publisher of items.
 * <br>It can be used to publish items to a list of subscribers.
 * <br>Unlike {@link java.util.concurrent.SubmissionPublisher}, it is synchronous.
 * It means that no items are published until all subscribers have processed the previous items.
 * <br>The subscribers can be invoked in parallel threads (see {@link #ItemPublisher(ExecutorService)}).
 * @param <T> The type of the items
 */
public class ItemPublisher<T> implements AutoCloseable, Runnable {
	
	/**
	 * An item listener.
	 * <br>It should be passed to {@link #subscribe(ItemListener)} to subscribe to the publisher and receive the items.
	 * <br>Its {@link Consumer#accept(Object)} method will be called for each item published.
	 * @param <T> The type of the items
	 */
	public interface ItemListener<T> extends Consumer<T> {
		/**
		 * Called when the subscription is made.
		 * <br>It does nothing by default.
		 * @param itemPublisher The publisher that is being subscribed to
		 */
		default void onSubscribe(ItemPublisher<T> itemPublisher) {}
		/**
		 * Called when the item publisher is closed.
		 * @param itemPublisher The publisher that is being subscribed to
		 */
		default void onComplete(ItemPublisher<T> itemPublisher) {}
	}
	
	private final Queue<T> items;
	private final List<ItemListener<T>> subscribers;
	private final ExecutorService executor;
	private final AtomicBoolean paused; 
	private final Semaphore pauseLock;
	private volatile boolean isClosed = false;
	private volatile boolean wasInterrupted;
	
	/**
	 * Creates a new item publisher.
	 * <br>The listeners will be invoked in the same thread as the publisher.
	 */
	public ItemPublisher() {
		this(null);
	}
	
	/**
	 * Creates a new item publisher.
	 * <br>The listeners will be invoked in the threads of the provided executor service.
	 * @param itemProcessor The executor service to use to process the items
	 */
	public ItemPublisher(ExecutorService itemProcessor) {
		this.items = new LinkedList<>();
		this.subscribers = new LinkedList<>();
		this.executor = itemProcessor;
		this.paused = new AtomicBoolean();
		this.pauseLock = new Semaphore(1);
	}

	/**
	 * Subscribes a new listener to the publisher.
	 * <br>The {@link ItemListener#onSubscribe(ItemPublisher)} method is called before this method returns.
	 * @param subscriber The listener to subscribe
	 * @throws IllegalStateException If the publisher is closed
	 */
	public void subscribe(ItemListener<T> subscriber) {
		if (isClosed) {
			throw new IllegalStateException();
		}
		synchronized (subscribers) {
			subscribers.add(subscriber);
		}
		subscriber.onSubscribe(this);
	}
	
	/**
	 * Unsubscribes a listener from the publisher.
	 * @param subscriber The listener to unsubscribe
	 */
	public void unsubscribe(ItemListener<T> subscriber) {
		synchronized (subscribers) {
			subscribers.remove(subscriber);
		}
	}
	
	/**
	 * Returns the number of subscribers.
	 * @return a positive integer
	 */
	public int getSubscribersCount() {
		synchronized (subscribers) {
			return subscribers.size();
		}
	}

	@Override
	public void run() {
		while (!isClosed || !items.isEmpty()) {
			try {
				T item = null;
				synchronized(items) {
					if (items.isEmpty()) {
						items.wait();
					}
					item = items.poll();
				}
				if (item!=null) {
					pauseLock.acquire();
					try {
						process(item);
					} finally {
						pauseLock.release();
					}
				}
			} catch (InterruptedException e) {
				doInterrupted();
			}
		}
		for (ItemListener<T> sub : subscribers) {
			sub.onComplete(this);
		}
	}

	private void doInterrupted() {
		wasInterrupted = true;
		// Exit gracefully
		this.isClosed = true;
		this.items.clear();
		close();
		Thread.currentThread().interrupt();
	}

	private void process(T item) throws InterruptedException {
		if (executor==null) {
			synchronized (subscribers) {
				for (Consumer<T> sub : subscribers) {
					sub.accept(item);
				}
			}
		} else {
			List<? extends Callable<Void>> callables;
			synchronized (subscribers) {
				callables = subscribers.stream().map(s -> new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						s.accept(item);
						return null;
					}}).toList();
			}
			executor.invokeAll(callables);
		}
	}
	
	/**
	 * Submits a new item to the publisher.
	 * @param item The item to submit
	 * @return true if the item was submitted, false if the publisher is closed
	 */
	public boolean submit(T item) {
		return submit(Collections.singleton(item));
	}
	
	/**
	 * Submits a collection of items to the publisher.
	 * @param items The items to submit
	 * @return true if the items were submitted, false if the publisher is closed
	 */
	public boolean submit(Collection<T> items) {
		if (isClosed) {
			return false;
		}
		synchronized (this.items) {
			this.items.addAll(items);
			this.items.notifyAll();
		}
		return true;
	}
	
	/**
	 * Pauses or resumes the publisher.
	 * <br>When paused, the publisher will not invoke any listeners until it is resumed.
	 * @param pause true to pause, false to resume
	 * @return true if the pause state was changed, false if it was already in the requested state
	 */
	public boolean pause(boolean pause) {
		if (!this.paused.compareAndSet(!pause, pause)) {
			return false;
		}
		if (pause) {
			try {
				pauseLock.acquire();
			} catch (InterruptedException e) {
				doInterrupted();
			}
		} else {
			pauseLock.release();
		}
		return true;
	}

	@Override
	public void close() {
		this.isClosed = true;
		synchronized (items) {
			items.notifyAll();
		}
	}
	
	/**
	 * Checks if the publisher was interrupted.
	 * @return true if the publisher was interrupted
	 */
	public boolean wasInterrupted() {
		return wasInterrupted;
	}
}