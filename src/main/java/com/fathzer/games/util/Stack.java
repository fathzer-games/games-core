package com.fathzer.games.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/** A class that manages a stack of objects.
 * <br>This class stores and retrieves data stored in a list, manages the current list index, and automatically creates data instance when if needed.
 * @param <T> The class of data containers.
 */
public class Stack<T> {
	private final Supplier<T> builder;
	private final List<T> backups;
	private int index;
	private T current;
	
	/** Constructor.
	 * <br>Creates a stack with an element
	 * @param builder A supplier that can create a data container.
	 */
	public Stack(Supplier<T> builder) {
		this.builder = builder;
		this.backups = new ArrayList<>();
		this.index = 0;
		this.current = null;
	}
	
	/** Gets the current element.
	 * @return an element
	 */
	public T get() {
		if (current==null) {
			if (index>=backups.size()) {
				current = builder.get();
				backups.add(current);
			} else {
				current = backups.get(index);
			}
		}
		return current;
	}
	
	/** Moves to the next element.
	 * <br>Creates the element if needed. 
	 */
	public void next() {
		index++;
		current = null;
	}

	/** Moves to previous element.
	 * @throws NoSuchElementException if current element is the first one
	 */
	public void previous() {
		if (index==0) {
			throw new NoSuchElementException();
		}
		index--;
		current = backups.get(index);
	}
}
