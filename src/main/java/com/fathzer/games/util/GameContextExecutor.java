package com.fathzer.games.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.fathzer.games.MoveGenerator;

public abstract class GameContextExecutor<M> {
	private int parallelism;
	private boolean interrupted;
	private ExecutorService exec;
	
	protected GameContextExecutor() {
		this.parallelism = PhysicalCores.count();
	}
	
	public <T> List<Future<T>> exec(Collection<Callable<T>> tasks) throws InterruptedException {
    	exec = Executors.newFixedThreadPool(getParallelism(), (Runnable r) -> new GameThread<>(r, buildMoveGenerator()));
		try {
			this.interrupted = false;
			return exec.invokeAll(tasks);
		} finally {
    		exec.shutdown();
		} 
	}
	
	@SuppressWarnings("unchecked")
	public MoveGenerator<M> getMoveGenerator() {
		return ((GameThread<M>)Thread.currentThread()).context;
	}
	
	public int getParallelism() {
		return parallelism;
	}

	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}
	
    public abstract MoveGenerator<M> buildMoveGenerator();
    
	public boolean isInterrupted() {
		return interrupted;
	}

	public void interrupt() {
		this.interrupted = true;
		if (exec!=null) {
			exec.shutdown();
		}
	}
}
