package ic.hku.hk;

import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.List;

import static ic.hku.hk.AndroidUtils.dpToPx;

public class AddressDialog {

    static <T extends AppCompatActivity> void pickUpAddressDialog(final T context
            , final GoogleApiClient apiClient, final EditText pickUpAddress
            , final PlacesAPIAutocompleteAdapter adapter, final boolean showCurrentPlaceCheck
            , final GoogleMap mMap, final SlidingUpPanelLayout layout, final Geocoder geocoder
            , final ImageView centreMap, final Button selectFromMapDone, final TextView addressPreview
            , final LinearLayout buttonBar, final LinearLayout insidePane, final TextView shareLat
            , final TextView shareLon){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = context.getLayoutInflater().inflate(R.layout.dialog_address_selection, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        final AutoCompleteTextView enterManually
                = (AutoCompleteTextView) dialog.findViewById(R.id.enterManually);
        final RelativeLayout editLL = (RelativeLayout) dialog.findViewById(R.id.editLL);
        final RelativeLayout mapLL = (RelativeLayout) dialog.findViewById(R.id.mapLL);
        final RelativeLayout locationLL = (RelativeLayout) dialog.findViewById(R.id.locationLL);
        if(enterManually != null
                && editLL != null && mapLL != null && locationLL != null){
            enterManually.setAdapter(adapter);
            //for autocomplete suggestions
            enterManually.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    String address = (String) adapter.getItem(pos).getFullText(null);
                    try {
                        Address location = geocoder.getFromLocationName(address, 1).get(0);
                        shareLat.setText(String.valueOf(location.getLatitude()));
                        shareLon.setText(String.valueOf(location.getLongitude()));
                        pickUpAddress.setText(address);
                    } catch (Exception e) {
                        Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show();
                    }
                    dialog.cancel();
                }
            });
            enterManually.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_DONE) {
                        String address = enterManually.getText().toString();
                        pickUpAddress.setText(address);
                        try {
                            Address location = geocoder.getFromLocationName(address, 1).get(0);
                            shareLat.setText(String.valueOf(location.getLatitude()));
                            shareLon.setText(String.valueOf(location.getLongitude()));
                            dialog.cancel();
                        } catch (IOException e) {
                            Toast.makeText(context, "Error in parsing your address", Toast.LENGTH_SHORT).show();
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });
            View.OnTouchListener onTouchListenerEditLL = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            editLL.setBackgroundColor(context.getResources().getColor(R.color.textSelectedGrey, context.getTheme()));
                            break;
                        case MotionEvent.ACTION_UP:
                            editLL.setBackgroundColor(context.getResources().getColor(R.color.inputText, context.getTheme()));
                            enterManually.setCursorVisible(true);
                            enterManually.setBackgroundTintList(context.getResources().getColorStateList(R.color.colorAccent, context.getTheme()));
                    }
                    return false;
                }
            };
            editLL.setOnTouchListener(onTouchListenerEditLL);
            enterManually.setOnTouchListener(onTouchListenerEditLL);
            mapLL.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mapLL.setBackgroundColor(context.getResources().getColor(R.color.textSelectedGrey, context.getTheme()));
                            break;
                        case MotionEvent.ACTION_UP:
                            mapLL.setBackgroundColor(context.getResources().getColor(R.color.inputText, context.getTheme()));
                            if(mMap != null){
                                selectFromMap(pickUpAddress, dialog, mMap, layout, geocoder
                                        , centreMap, selectFromMapDone, addressPreview, buttonBar, insidePane, shareLat, shareLon);
                            }
                    }
                    return true;
                }
            });
            locationLL.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            locationLL.setBackgroundColor(context.getResources().getColor(R.color.textSelectedGrey, context.getTheme()));
                            break;
                        case MotionEvent.ACTION_UP:
                            locationLL.setBackgroundColor(context.getResources().getColor(R.color.inputText, context.getTheme()));
                            if(showCurrentPlaceCheck){
                                showCurrentPlace(pickUpAddress, apiClient, context, shareLat, shareLon);
                                dialog.cancel();
                            }
                    }
                    return true;
                }
            });
        }
    }

    private static <T extends AppCompatActivity> void showCurrentPlace(final EditText pickUpAddress
            , final GoogleApiClient client, final T context, final TextView shareLat, final TextView shareLon) {
        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.
        @SuppressWarnings("MissingPermission")
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(client, null);
        final int mMaxEntries = 5;
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        final View mView = context.getLayoutInflater().inflate(R.layout.dialog_select_closest_address, null);
        mBuilder.setView(mView);
        final AlertDialog likelyPlacesDialog = mBuilder.create();
        final Button refresh = mView.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likelyPlacesDialog.cancel();
                showCurrentPlace(pickUpAddress, client, context, shareLat, shareLon);
            }
        });
        final LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int px24 = dpToPx(24, context);
        marginParams.setMargins(px24, 0, px24, px24);
        likelyPlacesDialog.show();
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                int i = 0;
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    // Build a list of likely places to show the user. Max 5.
                    final TextView view = (TextView) likelyPlacesDialog.findViewById(context.getResources()
                            .getIdentifier("address" + (i + 1), "id", context.getPackageName()));
                    if(view == null){
                        continue;
                    }
                    view.setVisibility(View.VISIBLE);
                    view.setText(placeLikelihood.getPlace().getAddress());
                    final LatLng latLng = placeLikelihood.getPlace().getLatLng();
                    view.setLayoutParams(marginParams);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pickUpAddress.setText(view.getText());
                            shareLat.setText(String.valueOf(latLng.latitude));
                            shareLon.setText(String.valueOf(latLng.longitude));
                            likelyPlacesDialog.cancel();
                        }
                    });
                    i++;
                    if(i >= mMaxEntries){
                        break;
                    }
                }
                mView.findViewById(R.id.loadingAddresses).setVisibility(View.GONE);
                // Release the place likelihood buffer, to avoid memory leaks.
                likelyPlaces.release();
                if(i == 0){
                    //No likely places
                    likelyPlacesDialog.cancel();
                    Toast.makeText(context, R.string.NoAddressesFoundToast, Toast.LENGTH_SHORT).show();
                }
                // Show a dialog offering the user the list of likely places, and add a
                // marker at the selected place.
            }
        });

        //if back is pressed then close this new dialog
        likelyPlacesDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    likelyPlacesDialog.cancel();
                }
                return false;
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
            , final TextView addressPreview, final LinearLayout buttonBar, final LinearLayout insidePane
            , final TextView shareLat, final TextView shareLon) {

        dialog.cancel();

        centreMap.setVisibility(View.VISIBLE);
        selectFromMapDone.setVisibility(View.VISIBLE);
        addressPreview.setVisibility(View.VISIBLE);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        buttonBar.setVisibility(View.INVISIBLE);
        insidePane.setVisibility(View.INVISIBLE);
        layout.setTouchEnabled(false);

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
                shareLat.setText(String.valueOf(centre.latitude));
                shareLon.setText(String.valueOf(centre.longitude));
                selectFromMapDone.setVisibility(View.INVISIBLE);
                centreMap.setVisibility(View.INVISIBLE);
                addressPreview.setVisibility(View.INVISIBLE);
                buttonBar.setVisibility(View.VISIBLE);
                insidePane.setVisibility(View.VISIBLE);
                layout.setTouchEnabled(true);
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
