package com.my.widget.data;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableHashSet<T> extends HashSet<T> {

    private Handler mHandler;
    private List<OnSetChangedListener<T>> mCallbacks;

    @SuppressWarnings("unused")
    public ObservableHashSet() {
        super();
        ensureHandler();
    }

    @SuppressWarnings("unused")
    public ObservableHashSet(Collection<? extends T> c) {
        super(c);
        ensureHandler();
    }

    @SuppressWarnings("unused")
    public ObservableHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        ensureHandler();
    }

    @SuppressWarnings("unused")
    public ObservableHashSet(int initialCapacity) {
        super(initialCapacity);
        ensureHandler();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        synchronized (this) {
            // Iterating a list may throw ConcurrentModificationException by
            // using for (Clazz var : list).
            // Copy the idea of CopyOnWriteArrayList to return the iterator of
            // a snapshot.
            HashSet<T> snapshot = new ObservableHashSet<>(this);
            return snapshot.iterator();
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return super.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return super.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (this) {
            return super.contains(o);
        }
    }

    @Override
    public boolean add(T t) {
        synchronized (this) {
            final boolean changed = super.add(t);

            if (changed) {
                dispatchOnSetChangedCallbacks();
            }

            return changed;
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            final boolean changed = super.remove(o);

            if (changed) {
                dispatchOnSetChangedCallbacks();
            }

            return changed;
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            super.clear();
            dispatchOnSetChangedCallbacks();
        }
    }

    @SuppressWarnings("unused")
    public void addOnSetChangedListener(OnSetChangedListener<T> listener) {
        synchronized (this) {
            // Ensure the list.
            if (mCallbacks == null) {
                mCallbacks = new CopyOnWriteArrayList<>();
            }

            mCallbacks.add(listener);
        }
    }

    @SuppressWarnings("unused")
    public void removeOnSetChangedListener(OnSetChangedListener<T> listener) {
        synchronized (this) {
            if (mCallbacks == null) return;

            mCallbacks.remove(listener);
        }
    }

    @SuppressWarnings("unused")
    public void removeAllOnSetChangedListener() {
        synchronized (this) {
            if (mCallbacks == null) return;

            mCallbacks.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void ensureHandler() {
        mHandler = new Handler(Looper.myLooper());
    }

    private void dispatchOnSetChangedCallbacks() {
        if (mHandler == null) return;

        final ObservableHashSet<T> thiz = this;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallbacks == null) return;

                for (OnSetChangedListener<T> callback : mCallbacks) {
                    callback.onSetChanged(thiz);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface OnSetChangedListener<T> {
        void onSetChanged(ObservableHashSet<T> thiz);
    }
}
