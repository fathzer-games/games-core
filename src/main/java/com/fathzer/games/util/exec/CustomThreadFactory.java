package com.fathzer.games.util.exec;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A custom thread factory.
 * <br>It can be used to create a thread factory that will create threads with a specific name and daemon status.
 */
public class CustomThreadFactory implements ThreadFactory {
	
	/**
	 * A basic thread name supplier.
	 * <br>It will create a thread name that is the concatenation of a prefix and a counter.
	 */
	public static class BasicThreadNameSupplier implements Supplier<String>{
		private final AtomicLong count;
		private final String prefix;
		/**
		 * Creates a new basic thread name supplier.
		 * @param prefix The prefix of the thread name
		 */
		public BasicThreadNameSupplier(String prefix) {
			super();
			this.prefix = prefix;
			this.count = new AtomicLong();
		}

		@Override
		public String get() {
			return  prefix + " " + count.incrementAndGet();
		}
	}
	
    private final Supplier<String> threadNameSupplier;
    private final boolean isDaemon;

    /**
     * Creates a new custom thread factory.
     * @param threadNameSupplier The supplier of thread names
     * @param isDaemon The daemon status of the threads
     */
    public CustomThreadFactory(Supplier<String> threadNameSupplier, boolean isDaemon) {
        this.threadNameSupplier = threadNameSupplier;
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName(threadNameSupplier.get());
        if (isDaemon) {
        	thread.setDaemon(true);
        }
		return thread;
    }
}
