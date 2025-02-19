package com.fathzer.games.util.exec;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class CustomThreadFactory implements ThreadFactory {
	
	public static class BasicThreadNameSupplier implements Supplier<String>{
		private final AtomicLong count;
		private final String prefix;
		
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
