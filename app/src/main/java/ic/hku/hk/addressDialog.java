package ic.hku.hk;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import static ic.hku.hk.AndroidUtils.dpToPx;

public class addressDialog {

    static <T extends AppCompatActivity> void pickUpAddressDialog(final T context
            , final GoogleApiClient apiClient, final EditText pickUpAddress
            , final PlacesAPIAutocompleteAdapter adapter, final boolean showCurrentPlaceCheck){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = context.getLayoutInflater().inflate(R.layout.dialog_address_selection, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        final TextView currentAddress = (TextView) dialog.findViewById(R.id.selectCurrentAddress);
        final AutoCompleteTextView enterManually
                = (AutoCompleteTextView) dialog.findViewById(R.id.enterManually);
        if(currentAddress != null && enterManually != null){
            currentAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(showCurrentPlaceCheck){
                        showCurrentPlace(pickUpAddress, apiClient, context);
                        dialog.cancel();
                    }
                }
            });
            enterManually.setAdapter(adapter);
            enterManually.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    pickUpAddress.setText(adapter.getItem(pos).getFullText(null));
                    dialog.cancel();
                }
            });
        }
        ;
    }

    private static <T extends AppCompatActivity> void showCurrentPlace(final EditText pickUpAddress
            , final GoogleApiClient client, final T context) {
        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.
        @SuppressWarnings("MissingPermission")
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(client, null);
        final int mMaxEntries = 5;
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        final View mView = context.getLayoutInflater().inflate(R.layout.dialog_select_closest_address, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        final Button refresh = (Button) mView.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                showCurrentPlace(pickUpAddress, client, context);
            }
        });
        final LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int px24 = dpToPx(24, context);
        marginParams.setMargins(px24, 0, px24, px24);
        dialog.show();
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                int i = 0;
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    // Build a list of likely places to show the user. Max 5.
                    final TextView view = (TextView) dialog.findViewById(context.getResources()
                            .getIdentifier("address" + (i + 1), "id", context.getPackageName()));
                    if(view == null){
                        continue;
                    }
                    view.setText(placeLikelihood.getPlace().getAddress());
                    view.setLayoutParams(marginParams);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pickUpAddress.setText(view.getText());
                            dialog.cancel();
                        }
                    });
                    i++;
                    if(i >= mMaxEntries){
                        break;
                    }
                }
                // Release the place likelihood buffer, to avoid memory leaks.
                likelyPlaces.release();
                if(i == 0){
                    //No likely places
                    dialog.cancel();
                    Toast.makeText(context, "No nearby addresses found" +
                            "\nPlease check your connection", Toast.LENGTH_SHORT).show();
                }
                // Show a dialog offering the user the list of likely places, and add a
                // marker at the selected place.
            }
        });
    }


}
