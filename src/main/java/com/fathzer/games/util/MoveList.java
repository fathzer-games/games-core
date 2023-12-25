package com.fathzer.games.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.ToIntFunction;

/** A specialized list optimized to sort moves.
 * Some ai algorithm, like alpha-beta pruning can be greatly optimized when moves are sorted with the (a priori) best moves first.
 * Usually, the <i>a priori</i> considers a lot of possible moves as equivalent;
 * Typically, in chess, all non promotion, non capture moves are considered equivalent.<br>
 * As <a href="https://en.wikipedia.org/wiki/Sorting_algorithm#Classification">sort has a O(<i>n</i>&#160;log&#160;<i>n</i>) computational complexity</a>, an optimization is to sort only the promotion or capture moves.
 * This class does this optimization by excluding from the sort all moves with an evaluation of Integer.MIN_VALUE.  
 * @see <a href="https://www.chessprogramming.org/Move_Ordering">Move Ordering on Chess Programming Wiki</a>
 * @param <E> The type of moves
 */
@SuppressWarnings("java:S2160")
public class MoveList<E> extends AbstractList<E> {
	private static class NoSort<T> implements ToIntFunction<T> {
	    @SuppressWarnings("rawtypes")
	    private static final ToIntFunction NO_SORT = new NoSort<>();

		@Override
		public int applyAsInt(T value) {
			return Integer.MIN_VALUE;
		}
	}
	
    @SuppressWarnings("unchecked")
    public static final <T> ToIntFunction<T> noSort() {
        return NoSort.NO_SORT;
    }
	
    private final List<E> toBeSorted;
    private final List<E> list;
    private ToIntFunction<E> evaluator;
    private final Comparator<E> comparator = (e1, e2) -> evaluator.applyAsInt(e2)-evaluator.applyAsInt(e1); 
    
    /** Constructor.
     * <br>The evaluator used for the sort ... sorts no moves (all moves are evaluated to Integer.MIN_VALUE).
     * @see #setEvaluator(ToIntFunction)
     */
    public MoveList() {
        this.toBeSorted = new ArrayList<>();
        this.list = new ArrayList<>();
        this.evaluator = noSort();
    }
    
    /** Constructor from a list and an evaluator.
     * <br>Warning, there are side effects between the argument list and the created instance. The argument can be changed by this call. 
     * @param moves A list of moves
     * @param evaluator a move evaluator
     */
    public MoveList(List<E> moves, ToIntFunction<E> evaluator) {
        this.toBeSorted = new ArrayList<>();
        this.list = moves;
    	setEvaluator(evaluator);
    }
    
    /** Sets the evaluator.
     * @param evaluator The new evaluator. Null to have no sort
     */
    public void setEvaluator(ToIntFunction<E> evaluator) {
    	this.evaluator = evaluator == null ? noSort() : evaluator;
    	if (!isEmpty()) {
    		// Split again the elements 
			list.addAll(toBeSorted);
			toBeSorted.clear();
			final ListIterator<E> iter = list.listIterator();
			while (iter.hasNext()) {
				E m = iter.next();
				if (evaluator.applyAsInt(m)!=Integer.MIN_VALUE) {
					toBeSorted.add(m);
					iter.remove();
				}
			}
    	}
    }

	@Override
	public boolean add(E e) {
		return evaluator.applyAsInt(e)==Integer.MIN_VALUE ? list.add(e) : toBeSorted.add(e);
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

	/** Sorts the list elements in descending order accordingly with the evaluator defined in {@link #setEvaluator(ToIntFunction)}
	 */
	public void sort() {
		toBeSorted.sort(comparator);
	}
	
	@Override
	public Iterator<E> iterator() {
		return toBeSorted.isEmpty() ? list.iterator() : new DualListIterator<>(toBeSorted, list);
	}
}
