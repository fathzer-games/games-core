package com.fathzer.games.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** A specialized list optimized to sort moves.
 * <br>Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the (a priori) best moves first.
 * Usually, the <i>a priori</i> comparator considers a lot of possible moves as equivalent;
 * Typically, in chess, all non promotion, non capture moves are considered equivalent.<br>
 * As <a href="https://en.wikipedia.org/wiki/Sorting_algorithm#Classification">sort has a O(<i>n</i>&#160;log&#160;<i>n</i>) computational complexity</a>, an optimization is to sort only the promotion or capture moves.
 * This class does this optimization by excluding from the sort all moves with an evaluation of Integer.MIN_VALUE.  
 * @see <a href="https://www.chessprogramming.org/Move_Ordering">Move Ordering on Chess Programming Wiki</a>
 * @param <E> The type of moves
 */
@SuppressWarnings("java:S2160")
public class MoveList<E> extends AbstractList<E> {
	private static class NoSort<T> implements SelectiveComparator<T> {
	    @SuppressWarnings("rawtypes")
	    private static final SelectiveComparator NO_SORT = new NoSort<>();

		@Override
		public int compare(T o1, T o2) {
			return 0;
		}

		@Override
		public boolean test(T t) {
			return false;
		}
	}
	
    @SuppressWarnings("unchecked")
    public static final <T> SelectiveComparator<T> noSort() {
        return NoSort.NO_SORT;
    }
	
    private final List<E> toBeSorted;
    private final List<E> list;
    private SelectiveComparator<E> comparator;
    
    /** Constructor.
     * <br>The evaluator used for the sort ... sorts no moves (all moves are evaluated to Integer.MIN_VALUE).
     * @see #setComparator(SelectiveComparator)
     */
    public MoveList() {
        this.toBeSorted = new ArrayList<>();
        this.list = new ArrayList<>();
        this.comparator = noSort();
    }
    
    /** Constructor from a list and an evaluator.
     * <br>Warning, there are side effects between the argument list and the created instance. The argument can be changed by this call. 
     * @param moves A list of moves
     * @param comparator a move comparator
     */
    public MoveList(List<E> moves, SelectiveComparator<E> comparator) {
        this.toBeSorted = new ArrayList<>();
        this.list = moves;
    	setComparator(comparator);
    }
    
    /** Sets the comparator.
     * @param comparator The new evaluator. Null to have no sort
     */
    public void setComparator(SelectiveComparator<E> comparator) {
    	this.comparator = comparator == null ? noSort() : comparator;
    	if (!isEmpty()) {
    		// Split again the elements 
			list.addAll(toBeSorted);
			toBeSorted.clear();
			final ListIterator<E> iter = list.listIterator();
			while (iter.hasNext()) {
				E m = iter.next();
				if (this.comparator.test(m)) {
					toBeSorted.add(m);
					iter.remove();
				}
			}
    	}
    }

	@Override
	public boolean add(E e) {
		return comparator.test(e) ? toBeSorted.add(e) : list.add(e);
	}

	@Override
	public E get(int index) {
		return index<toBeSorted.size()?toBeSorted.get(index):list.get(index-toBeSorted.size());
	}

	@Override
	public int size() {
		return toBeSorted.size()+list.size();
	}

	@Override
	public void clear() {
		list.clear();
		toBeSorted.clear();
	}

	/** Sorts the list elements in descending order accordingly with the comparator defined in {@link #setComparator(SelectiveComparator)}
	 */
	public void sort() {
		toBeSorted.sort(comparator);
	}
	
	@Override
	public Iterator<E> iterator() {
		return toBeSorted.isEmpty() ? list.iterator() : new DualListIterator<>(toBeSorted, list);
	}
}
