package com.my.core.data;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.my.core.protocol.IObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// FIXME: This is temporary because not all the methods would notify the
// FIXME: observers about the change.
public class ObservableArrayList<E> extends CopyOnWriteArrayList<E>
    implements IObservableList<E> {

    private final Handler mHandler;
    private final List<ListChangeListener<E>> mCallbacks = new ArrayList<>();

    private int mMaxCapacity;

    public ObservableArrayList(Looper looper) {
        super();

        // Ensure the handler.
        mHandler = new Handler(looper);
    }

    public ObservableArrayList(@NonNull E[] array,
                               Looper looper) {
        super(array);

        // Ensure the handler.
        mHandler = new Handler(looper);
    }

    public ObservableArrayList(@NonNull Collection<? extends E> c,
                               Looper looper) {
        super(c);

        // Ensure the handler.
        mHandler = new Handler(looper);
    }

    @Override
    public E set(int index, E element) {
        E ret = super.set(index, element);

        dispatchCallbacks(null, null, ret);

        return ret;
    }

    @Override
    public boolean add(E e) {
        if (size() == mMaxCapacity) return false;

        final boolean ret = super.add(e);

        if (ret) {
            dispatchCallbacks(e, null, null);
        }

        return ret;
    }

    @Override
    public void add(int index, E element) {
        if (size() == mMaxCapacity) return;

        super.add(index, element);
        dispatchCallbacks(element, null, null);
    }

    @Override
    public E remove(int index) {
        E removed = super.remove(index);
        dispatchCallbacks(null, removed, null);
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        dispatchCallbacks(null, null, null);
    }

    @Override
    public void addListener(ListChangeListener<E> listener) {
        synchronized (mCallbacks) {
            mCallbacks.add(listener);
        }
    }

    @Override
    public void removeListener(ListChangeListener<E> listener) {
        synchronized (mCallbacks) {
            mCallbacks.remove(listener);
        }
    }

    public void setMaxCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Given capacity is negative.");
        }

        mMaxCapacity = capacity;
    }

    public int getMaxCapacity() {
        return mMaxCapacity;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void dispatchCallbacks(final E added,
                                   final E removed,
                                   final E updated) {
        synchronized (mCallbacks) {
            if (mCallbacks.isEmpty()) return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mCallbacks) {
                    for (ListChangeListener<E> callback : mCallbacks) {
                        if (added != null) {
                            callback.onItemAdded(ObservableArrayList.this, added);
                        }
                        if (removed != null) {
                            callback.onItemRemoved(ObservableArrayList.this, removed);
                        }
                        if (updated != null) {
                            callback.onItemChanged(ObservableArrayList.this, updated);
                        }
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface Provider<E> {
        ObservableArrayList<E> getObservableList();
    }
}
