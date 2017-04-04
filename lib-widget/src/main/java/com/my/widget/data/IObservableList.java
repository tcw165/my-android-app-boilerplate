package com.my.widget.data;

import java.util.List;

/**
 * A list that allows listeners to track changes when they occur.
 */
public interface IObservableList<E> {
    /**
     * Add a listener to this observable list.
     * @param listener the listener for listening to the list changes
     */
    void addListener(ListChangeListener<E> listener);

    /**
     * Tries to removed a listener from this observable list. If the listener is not
     * attached to this list, nothing happens.
     * @param listener a listener to remove
     */
    void removeListener(ListChangeListener<E> listener);

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    interface ListChangeListener<T> {
        void onItemAdded(List<T> list, T item);
        void onItemRemoved(List<T> list, T item);
        void onItemChanged(List<T> list, T item);
    }
}
