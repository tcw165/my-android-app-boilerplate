package com.my.boilerplate.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.my.boilerplate.R;
import com.my.boilerplate.data.GeoPlace;
import com.my.boilerplate.protocol.IOnClickObjectListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Note that this adapter requires a valid {@link GoogleApiClient}.
 * The API client must be maintained in the encapsulating Activity, including all lifecycle and
 * connection states. The API client must be connected with the {@link Places#GEO_DATA_API} API.
 */
public class PlacesAutoCompleteAdapter
    extends RecyclerView.Adapter<PlacesAutoCompleteAdapter.PredictionHolder>
    implements Filterable {

    private List<GeoPlace> mResultList;
    private final GoogleApiClient mGoogleApiClient;
    private LatLngBounds mBounds;
    private final AutocompleteFilter mPlaceFilter;

    private final Context mContext;
    private final int layout;
    private final IOnClickObjectListener mOnClickListener;

    public PlacesAutoCompleteAdapter(Context context,
                                     GoogleApiClient googleApiClient,
                                     int layoutRes,
                                     LatLngBounds bounds,
                                     AutocompleteFilter filter,
                                     IOnClickObjectListener onClickListener) {
        mContext = context;
        layout = layoutRes;
        mGoogleApiClient = googleApiClient;
        mBounds = bounds;
        mPlaceFilter = filter;
        mOnClickListener = onClickListener;
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    mResultList = getAutocomplete(constraint);
                    if (mResultList != null) {
                        // The API successfully returned results.
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    //notifyDataSetInvalidated();
                }
            }
        };
    }

    private List<GeoPlace> getAutocomplete(CharSequence constraint) {
        if (mGoogleApiClient.isConnected()) {
            Log.i("", "Starting autocomplete query for: " + constraint);

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results =
                Places.GeoDataApi
                    .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                                mBounds, mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(mContext, "Error contacting API: " + status.toString(),
                               Toast.LENGTH_SHORT).show();
                Log.e("", "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }

            Log.i("", "Query completed. Received " + autocompletePredictions.getCount()
                      + " predictions.");

            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID and description).

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            List<GeoPlace> resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new GeoPlace object.
                resultList.add(new GeoPlace(prediction.getPlaceId(),
                                            prediction.getFullText(null).toString()));
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();

            return resultList;
        }
        Log.e("", "Google API client is not connected for autocomplete query.");
        return null;
    }

    @Override
    public PredictionHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View convertView = layoutInflater.inflate(layout, viewGroup, false);

        return new PredictionHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final PredictionHolder holder,
                                 final int position) {
        final View itemView = holder.itemView;
        final GeoPlace suggestion = getItem(position);

        holder.addressView.setText(mResultList.get(position).fullAddress);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onClickObject(itemView, suggestion);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mResultList != null)
            return mResultList.size();
        else
            return 0;
    }

    /**
     * Sets the bounds for all subsequent queries.
     */
    public void setBounds(LatLngBounds bounds) {
        mBounds = bounds;
    }

    public void clear() {
        mResultList.clear();
        notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private GeoPlace getItem(int position) {
        return mResultList.get(position);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    static class PredictionHolder extends RecyclerView.ViewHolder {

        TextView addressView;

        PredictionHolder(View itemView) {

            super(itemView);
            addressView = (TextView) itemView.findViewById(R.id.address);
        }
    }
}
