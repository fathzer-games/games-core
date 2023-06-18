package com.fathzer.games.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fathzer.games.MoveGenerator;

public abstract class GameContextExecutor<M> {
	private boolean interrupted;
	private ThreadPoolExecutor exec;

	static {
		System.out.println("This is the new version"); //TODO
	}

	protected GameContextExecutor(ThreadPoolExecutor executor) {
		this.exec = executor;
	}

	protected GameContextExecutor() { //TODO remove?
		this.exec = new ThreadPoolExecutor(0, PhysicalCores.count(), 10L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), (Runnable r) -> new GameThread<>(r));
	}
	
	public <T> List<Future<T>> exec(Collection<Callable<T>> tasks) throws InterruptedException {
		this.interrupted = false;
		return exec.invokeAll(tasks);
	}
	
	@SuppressWarnings("unchecked")
	public MoveGenerator<M> getMoveGenerator() {
		return ((GameThread<M>)Thread.currentThread()).context;
	}
	
	public int getParallelism() {
		return exec.getMaximumPoolSize();
	}

	public void setParallelism(int parallelism) {
		this.exec.setMaximumPoolSize(parallelism);
	}
	
    public abstract MoveGenerator<M> buildMoveGenerator();
    
	public boolean isInterrupted() {
		return interrupted;
	}

	public void interrupt() {
		this.interrupted = true;
	}
	
	public ThreadPoolExecutor getExecutor() {
		return this.exec;
	}
}
