// Copyright (c) 2017-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.my.core.data;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO: Remove it because it's so far useless.
public class ObservableCollections {

    public static <T> IObservableCollection<T> makeCollection(Collection<T> c) {
        return new ObservableCollection<>(c);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private ObservableCollections() {
        // DO NOTHING.
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface IObservableCollection<E> {

        @SuppressWarnings("unused")
        void addOnSetChangedListener(OnUpdateListener<E> listener);

        @SuppressWarnings("unused")
        void removeOnSetChangedListener(OnUpdateListener<E> listener);

        @SuppressWarnings("unused")
        void removeAllOnSetChangedListener();
    }

    public interface OnUpdateListener<E> extends Collection<E> {

        @SuppressWarnings("unused")
        void onUpdateCollection(Collection<E> collection);
    }

    private static class ObservableCollection<E>
        implements Collection<E>,
                   IObservableCollection<E> {

        // Backing Collection
        final Collection<E> mCollection;
        // Object on which to synchronize
        final Object mMutex;
        final Handler mHandler;
        List<OnUpdateListener<E>> mCallbacks;

        ObservableCollection(Collection<E> c) {
            if (c == null) {
                throw new NullPointerException();
            }

            mCollection = c;
            mMutex = new Object();
            mHandler = new Handler(Looper.myLooper());
        }

        ObservableCollection(Collection<E> c,
                             Object mutex) {
            if (c == null || mutex == null) {
                throw new NullPointerException();
            }
            mCollection = c;
            mMutex = mutex;
            mHandler = new Handler(Looper.myLooper());
        }

        @Override
        public int size() {
            synchronized (mMutex) {
                return mCollection.size();
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mMutex) {
                return mCollection.isEmpty();
            }
        }

        @Override
        public boolean contains(Object o) {
            synchronized (mMutex) {
                return mCollection.contains(o);
            }
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            synchronized (mMutex) {
                return mCollection.containsAll(c);
            }
        }

        @NonNull
        public Object[] toArray() {
            synchronized (mMutex) {
                return mCollection.toArray();
            }
        }

        @NonNull
        public <T> T[] toArray(@NonNull T[] a) {
            synchronized (mMutex) {
                return mCollection.toArray(a);
            }
        }

        // TODO: Can we make a snapshot?
        @NonNull
        @Override
        public Iterator<E> iterator() {
            // Must be manually synched by user!
            return mCollection.iterator();
        }

        @Override
        public boolean add(E e) {
            synchronized (mMutex) {
                boolean res = mCollection.add(e);

                if (res) dispatchOnUpdateCallbacks();

                return res;
            }
        }

        @Override
        public boolean remove(Object o) {
            synchronized (mMutex) {
                boolean res = mCollection.remove(o);

                if (res) dispatchOnUpdateCallbacks();

                return res;
            }
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends E> c) {
            synchronized (mMutex) {
                boolean res = mCollection.addAll(c);

                if (res) dispatchOnUpdateCallbacks();

                return res;
            }
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            synchronized (mMutex) {
                boolean res = mCollection.removeAll(c);

                if (res) dispatchOnUpdateCallbacks();

                return res;
            }
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            synchronized (mMutex) {
                boolean res = mCollection.retainAll(c);

                if (res) dispatchOnUpdateCallbacks();

                return res;
            }
        }

        @Override
        public void clear() {
            synchronized (mMutex) {
                mCollection.clear();
                dispatchOnUpdateCallbacks();
            }
        }

        @Override
        public String toString() {
            synchronized (mMutex) {
                return mCollection.toString();
            }
        }

        @Override
        @SuppressWarnings("unused")
        public void addOnSetChangedListener(OnUpdateListener<E> listener) {
            synchronized (mMutex) {
                // Ensure the list.
                if (mCallbacks == null) {
                    mCallbacks = new CopyOnWriteArrayList<>();
                }

                mCallbacks.add(listener);
            }
        }

        @Override
        @SuppressWarnings("unused")
        public void removeOnSetChangedListener(OnUpdateListener<E> listener) {
            synchronized (mMutex) {
                if (mCallbacks == null) return;

                mCallbacks.remove(listener);
            }
        }

        @Override
        @SuppressWarnings("unused")
        public void removeAllOnSetChangedListener() {
            synchronized (mMutex) {
                if (mCallbacks == null) return;

                mCallbacks.clear();
            }
        }

        private void dispatchOnUpdateCallbacks() {
            if (mHandler == null) return;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (mMutex) {
                        if (mCallbacks == null) return;

                        for (OnUpdateListener<E> callback : mCallbacks) {
                            if (callback == null) continue;

                            callback.onUpdateCollection(mCollection);
                        }
                    }
                }
            });
        }
    }
}
