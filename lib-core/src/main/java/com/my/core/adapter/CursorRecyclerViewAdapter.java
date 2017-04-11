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

package com.my.core.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Looper;
import android.provider.BaseColumns;
import android.support.v7.appcompat.BuildConfig;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> {

    public static final int PAYLOAD_ITEM_CHECKED = 0;
    public static final int PAYLOAD_ITEM_UNCHECKED = 1;

    final private WeakReference<Context> mContext;
    final private WeakReference<LayoutInflater> mInflater;

    private RecyclerView mRecyclerView;
    private Cursor mCursor;

    // State.
    private boolean mDataValid;
    private int mRowIdColumn;

    public CursorRecyclerViewAdapter(Context context) {
        mContext = new WeakReference<>(context);
        mInflater = new WeakReference<>(LayoutInflater.from(context));
        mDataValid = false;
        mRowIdColumn = -1;

        // Enable stable-ID so that it is able to find ViewHolder by item ID.
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null && !mCursor.isClosed()) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid &&
            mCursor != null && !mCursor.isClosed() &&
            mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        } else {
            return -1;
        }
    }

    @Override
    public void onBindViewHolder(VH viewHolder,
                                 int position) {
        this.onBindViewHolder(viewHolder, position, null);
    }

    @Override
    public void onBindViewHolder(VH viewHolder,
                                 int position,
                                 List<Object> payloads) {
        try {
            if (!mDataValid) {
                throw new IllegalStateException(
                    "this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException(
                    "couldn't move cursor to position " + position);
            }
            if (BuildConfig.DEBUG) {
                Log.d("@", "onBindViewHolder(" + position +
                           "), context=" + getContext() +
                           ", running on " + Looper.myLooper());
            }

            onBindViewHolder(viewHolder, mCursor, payloads);
        } catch (Throwable exception) {
            if (BuildConfig.DEBUG) {
                Log.d("@", "onBindViewHolder(" + position +
                           "), error=" + exception.getMessage());
            }
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param viewHolder The ViewHolder which should be updated to represent
     *                   the contents of the item at the given position in
     *                   the data set.
     * @param cursor     The cursor of the item within the adapter's data set.
     * @param payloads   A list of merged payloads (could be null). Can be
     *                   empty list if requires full update.
     */
    public abstract void onBindViewHolder(final VH viewHolder,
                                          final Cursor cursor,
                                          final List<Object> payloads);

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        mRecyclerView = null;
    }

    @SuppressWarnings("unused")
    final public Context getContext() {
        return mContext.get();
    }

    @SuppressWarnings("unused")
    final public LayoutInflater getInflater() {
        return mInflater.get();
    }

    @SuppressWarnings("unused")
    final public Cursor getCursor() {
        return mCursor;
    }

    @SuppressWarnings("unused")
    final public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Return the ViewHolder for the item with the given id. The RecyclerView
     * must use an Adapter with
     * {@link RecyclerView.Adapter#setHasStableIds(boolean) stableIds}
     * to return a non-null value.
     * <p>
     * This method checks only the children of RecyclerView. If the item with
     * the given <code>id</code> is not laid out, it <em>will not</em> create
     * a new one.
     * <p>
     * When the ItemAnimator is running a change animation, there might be 2
     * ViewHolders with the same id. In this case, the updated ViewHolder will
     * be returned.
     *
     * @param id The id for the requested item
     *
     * @return The ViewHolder with the given <code>id</code> or null if there
     * is no such item
     */
    @SuppressWarnings("unused")
    final public RecyclerView.ViewHolder findViewHolderForItemId(final long id) {
        if (mRecyclerView == null) {
            throw new IllegalStateException(
                "No RecyclerView is observing this adapter.");
        }

        return mRecyclerView.findViewHolderForItemId(id);
    }

    /**
     * Return the ViewHolder for the item in the given position of the data set.
     * Unlike {@link RecyclerView#findViewHolderForLayoutPosition(int)} this
     * method takes into account any pending adapter changes that may not be
     * reflected to the layout yet. On the other hand, if
     * {@link RecyclerView.Adapter#notifyDataSetChanged()}
     * has been called but the new layout has not been calculated yet, this
     * method will return <code>null</code> since the new positions of views are
     * unknown until the layout is calculated.
     * <p>
     * This method checks only the children of RecyclerView. If the item at the given
     * <code>position</code> is not laid out, it <em>will not</em> create a new one.
     * <p>
     * When the ItemAnimator is running a change animation, there might be 2 ViewHolders
     * representing the same Item. In this case, the updated ViewHolder will be returned.
     *
     * @param position The position of the item in the data set of the adapter
     * @return The ViewHolder at <code>position</code> or null if there is no such item
     */
    @SuppressWarnings("unused")
    final public RecyclerView.ViewHolder findViewHolderForAdapterPosition(final int position) {
        if (mRecyclerView == null) {
            throw new IllegalStateException(
                "No RecyclerView is observing this adapter.");
        }

        return mRecyclerView.findViewHolderForAdapterPosition(position);
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing
     * cursor it will be closed.
     */
    @SuppressWarnings("unused")
    public void setData(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null && !old.isClosed()) {
            old.close();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * Swap in a new Cursor, returning the old Cursor. Unlike
     * {@link #setData(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
            oldCursor.close();
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow(BaseColumns._ID);
            mDataValid = true;
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        notifyDataSetChanged();
        return oldCursor;
    }

    // FIXME: Seems not work.
    final private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    };
}
