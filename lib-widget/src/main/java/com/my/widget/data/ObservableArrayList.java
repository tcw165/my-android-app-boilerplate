package com.my.widget.data;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableArrayList<E> extends ArrayList<E>
    implements IObservableList<E>,
               Runnable {

    private final Handler mHandler;
    private final List<ListChangeListener<E>> mCallbacks = new ArrayList<>();

    public ObservableArrayList(int initialCapacity,
                               Looper looper) {
        super(initialCapacity);

        // Ensure the handler.
        mHandler = new Handler(looper);
    }

    public ObservableArrayList(Looper looper) {

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

        dispatchCallbacks();

        return ret;
    }

    @Override
    public boolean add(E e) {
        boolean ret = super.add(e);

        if (ret) {
            dispatchCallbacks();
        }

        return ret;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        dispatchCallbacks();
    }

    @Override
    public E remove(int index) {
        E ret = super.remove(index);
        dispatchCallbacks();
        return ret;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = super.remove(o);

        if (ret) {
            dispatchCallbacks();
        }

        return ret;
    }

    @Override
    public void clear() {
        super.clear();
        dispatchCallbacks();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean ret = super.addAll(c);

        if (ret) {
            dispatchCallbacks();
        }

        return ret;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean ret = super.addAll(index, c);

        if (ret) {
            dispatchCallbacks();
        }

        return ret;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        dispatchCallbacks();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = super.removeAll(c);

        if (ret) {
            dispatchCallbacks();
        }

        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = super.retainAll(c);

        if (ret) {
            dispatchCallbacks();
        }

        return ret;
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

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void dispatchCallbacks() {
        synchronized (mCallbacks) {
            if (mCallbacks.isEmpty()) return;

            mHandler.post(this);
        }
    }

    @Override
    public void run() {
        synchronized (mCallbacks) {
            for (ListChangeListener<E> callback : mCallbacks) {
                callback.onListUpdate(this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface Provider<E> {
        ObservableArrayList<E> getObservableList();
    }
}
