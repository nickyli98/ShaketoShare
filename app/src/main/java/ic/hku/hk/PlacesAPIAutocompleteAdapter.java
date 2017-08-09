package ic.hku.hk;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;


import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlacesAPIAutocompleteAdapter
        extends ArrayAdapter<AutocompletePrediction> implements Filterable {

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private ArrayList<AutocompletePrediction> mResultList;
    private GoogleApiClient client;
    private LatLngBounds bounds;
    private AutocompleteFilter filter;

    public PlacesAPIAutocompleteAdapter(Context context, GoogleApiClient googleApiClient,
                                        LatLngBounds bounds, AutocompleteFilter filter) {
        //TODO FIX
        super(context,android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
        client = googleApiClient;
        this.bounds = bounds;
        this.filter = filter;
    }

    public void setBounds(LatLngBounds bounds) {
        this.bounds = bounds;
    }

    public void setFilter(AutocompleteFilter filter) {
        this.filter = filter;
    }

    public void setClient(GoogleApiClient client){
        this.client = client;
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public AutocompletePrediction getItem(int pos) {
        return mResultList.get(pos);
    }

    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = super.getView(pos, convertView, parent);
        AutocompletePrediction item = getItem(pos);
        TextView view1 = row.findViewById(android.R.id.text1);
        TextView view2 = row.findViewById(android.R.id.text2);
        view1.setText(item.getPrimaryText(STYLE_BOLD));
        view2.setText(item.getSecondaryText(STYLE_BOLD));
        return row;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(client == null || !client.isConnected()){
                    Toast.makeText(getContext(), R.string.FailedToConnectToast,
                            Toast.LENGTH_SHORT).show();
                    return null;
                }
                ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                if(constraint != null){
                    filterData = getAutocomplete(constraint);
                }

                results.values = filterData;
                if(filterData != null){
                    results.count = filterData.size();
                } else {
                    results.count = 0;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                if(filterResults != null && filterResults.count > 0){
                    mResultList = (ArrayList<AutocompletePrediction>) filterResults.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if(resultValue instanceof AutocompletePrediction){
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    //Pre: Client is connected and not null
    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
        PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi
                .getAutocompletePredictions(client, constraint.toString(),
                        bounds, filter);
        AutocompletePredictionBuffer autocompletePredictions =
                results.await(60, TimeUnit.SECONDS);
        final Status status = autocompletePredictions.getStatus();
        if(!status.isSuccess()){
            Toast.makeText(getContext(), R.string.FailedToConnectToast, Toast.LENGTH_SHORT).show();
            autocompletePredictions.release();
            return null;
        }
        return DataBufferUtils.freezeAndClose(autocompletePredictions);
    }
}
