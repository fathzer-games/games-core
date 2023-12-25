package com.fathzer.games.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class DualListIterator<T> implements Iterator<T> {
    private final ListIterator<T> iterator1;
    private final ListIterator<T> iterator2;

    DualListIterator(List<T> list1, List<T> list2) {
        this.iterator1 = list1.listIterator();
        this.iterator2 = list2.listIterator();
    }

    @Override
    public boolean hasNext() {
        return iterator1.hasNext() || iterator2.hasNext();
    }

    @Override
    public T next() {
        if (iterator1.hasNext()) {
            return iterator1.next();
        } else {
            return iterator2.next();
        }
    }
}
