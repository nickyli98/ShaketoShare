package ic.hku.hk;

import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static ic.hku.hk.AndroidUtils.dpToPx;

public class addressDialog {

    static <T extends AppCompatActivity> void pickUpAddressDialog(final T context
            , final GoogleApiClient apiClient, final EditText pickUpAddress
            , final PlacesAPIAutocompleteAdapter adapter, final boolean showCurrentPlaceCheck
            , final GoogleMap mMap, final SlidingUpPanelLayout layout, final Geocoder geocoder
            , final ImageView centreMap, final Button selectFromMapDone, final TextView addressPreview){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = context.getLayoutInflater().inflate(R.layout.dialog_address_selection, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        final TextView currentAddress = (TextView) dialog.findViewById(R.id.selectCurrentAddress);
        final TextView selectFromMap = (TextView) dialog.findViewById(R.id.selectFromMap);
        final AutoCompleteTextView enterManually
                = (AutoCompleteTextView) dialog.findViewById(R.id.enterManually);
        if(currentAddress != null && selectFromMap != null && enterManually != null){
            currentAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(showCurrentPlaceCheck){
                        showCurrentPlace(pickUpAddress, apiClient, context);
                        dialog.cancel();
                    }
                }
            });
            selectFromMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mMap != null){
                        selectFromMap(pickUpAddress, dialog, mMap, layout, geocoder
                                , centreMap, selectFromMapDone, addressPreview);
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
            enterManually.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_DONE) {
                        pickUpAddress.setText(enterManually.getText());
                        dialog.cancel();
                    }
                    return true;
                }
            });
        }
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
        final Button refresh = mView.findViewById(R.id.refresh);
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
                    Toast.makeText(context, R.string.NoAddressesFoundToast, Toast.LENGTH_SHORT).show();
                }
                // Show a dialog offering the user the list of likely places, and add a
                // marker at the selected place.
            }
        });
    }

    /*
     Code for selecting the location from the map. Makes the marker for the centre visible and
     then selects the location based on where the camera is centred. Then uses the Android Geocoder
     to reverse geocode the address from these coordinates.
     */
    private static void selectFromMap(final TextView addressView
            , final AlertDialog dialog, final GoogleMap mMap, final SlidingUpPanelLayout layout
            , final Geocoder geocoder, final ImageView centreMap, final Button selectFromMapDone
            , final TextView addressPreview) {

        dialog.cancel();

        centreMap.setVisibility(View.VISIBLE);
        selectFromMapDone.setVisibility(View.VISIBLE);
        addressPreview.setVisibility(View.VISIBLE);

        final LatLng centre = mMap.getCameraPosition().target;
        addressPreview.setText(getAddress(centre.latitude, centre.longitude, geocoder));

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                addressPreview.setText("");
            }
        });
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                final LatLng centre = mMap.getCameraPosition().target;
                addressPreview.setText(getAddress(centre.latitude, centre.longitude, geocoder));
            }
        });
        layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        selectFromMapDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LatLng centre = mMap.getCameraPosition().target;
                selectFromMapDone.setVisibility(View.INVISIBLE);
                centreMap.setVisibility(View.INVISIBLE);
                layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                addressView.setText(getAddress(centre.latitude, centre.longitude, geocoder));
            }
        });
    }

    private static String getAddress(double latitude, double longitude, Geocoder geocoder) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            System.out.println("NEW ADDRESS: " + address);
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
