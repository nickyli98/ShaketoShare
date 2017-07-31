package ic.hku.hk;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_ADDRESS;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String ISO3166_ALPHA2_COUNTRYCODE = "HK";
    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private PlacesAPIAutocompleteAdapter adapter;
    private boolean firstTime = true;
    private LatLngBounds HONGKONG = new LatLngBounds(
            new LatLng(22.17, 113.82), new LatLng(22.54, 114.38));
    private final String dateFormat = "yyyy/MM/dd"; //In which you need put here
    private final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.TRADITIONAL_CHINESE);
    private SlidingUpPanelLayout layout;
    private TabHost host;
    private Button shareButton;
    private Button minimiseButton;
    private Switch supplyOrganic;
    private EditText supplyWeight;
    private EditText supplyDateFrom;
    private EditText supplyDateTo;
    private EditText pickUpAddress;
    private Switch demandOrganic;
    private EditText demandWeight;
    private EditText demandDateFrom;
    private EditText demandDateTo;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        supplyOrganic = (Switch) findViewById(R.id.supplyOrganic);
        supplyWeight = (EditText) findViewById(R.id.supplyWeight);
        supplyDateFrom = (EditText) findViewById(R.id.supplyDateFrom);
        supplyDateTo = (EditText) findViewById(R.id.supplyDateTo);
        pickUpAddress = (EditText) findViewById(R.id.pickUpAddress);
        demandOrganic = (Switch) findViewById(R.id.demandOrganic);
        demandWeight = (EditText) findViewById(R.id.demandWeight);
        demandDateFrom = (EditText) findViewById(R.id.demandDateFrom);
        demandDateTo = (EditText) findViewById(R.id.demandDateTo);
        shareButton = (Button) findViewById(R.id.share);
        minimiseButton = (Button) findViewById(R.id.minimise);
        host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Supply tab
        TabHost.TabSpec spec = host.newTabSpec("Supply Tab");
        spec.setContent(R.id.supplyTab);
        spec.setIndicator("Supply");
        host.addTab(spec);

        //Demand tab
        spec = host.newTabSpec("Demand Tab");
        spec.setContent(R.id.demandTab);
        spec.setIndicator("Demand");
        host.addTab(spec);

        layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });

        supplyDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(supplyDateFrom);
            }
        });

        supplyDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(supplyDateTo);
            }
        });

        demandDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(demandDateFrom);
            }
        });

        demandDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(demandDateTo);
            }
        });

        minimiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minimiseButton.setVisibility(View.INVISIBLE);
                layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        pickUpAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_address_selection, null);
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
                            showCurrentPlace(pickUpAddress);
                            dialog.cancel();
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
            }
        });

        layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (previousState.equals(SlidingUpPanelLayout.PanelState.DRAGGING)) {
                    if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                        minimiseButton.setVisibility(View.INVISIBLE);
                        hideKeyboard();
                    } else if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                        minimiseButton.setVisibility(View.VISIBLE);
                    }
                } else if (newState.equals(SlidingUpPanelLayout.PanelState.DRAGGING)) {
                    if (previousState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                        minimiseButton.setVisibility(View.VISIBLE);
                    } else if (previousState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                        minimiseButton.setVisibility(View.INVISIBLE);
                        hideKeyboard();
                    }
                }
            }
        });

        layout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            adapter.setClient(null);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    private int dpToPx(float dp){
        Resources r = MapsActivity.this.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }

    private void share() {
        switch (layout.getPanelState()) {
            case COLLAPSED:
                layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                minimiseButton.setVisibility(View.VISIBLE);
                break;
            case EXPANDED:
                boolean shareCheckPassed = false;
                switch (host.getCurrentTab()) {
                    case 0: //supply
                        shareCheckPassed = shareCheck(supplyWeight, supplyDateFrom, supplyDateTo);
                        break;
                    case 1: //demand
                        shareCheckPassed = shareCheck(demandWeight, demandDateFrom, demandDateTo);
                        break;
                }
                if (shareCheckPassed) {
                    shareRequest();
                    layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    minimiseButton.setVisibility(View.INVISIBLE);
                }
            case ANCHORED:
                break;
            case HIDDEN:
                break;
            case DRAGGING:
                break;
        }
    }

    private void handleShakeEvent(int count) {
        share();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        switch (layout.getPanelState()) {
            case COLLAPSED:
                finish();
                break;
            case EXPANDED:
                layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                break;
        }
    }

    private void shareRequest() {
        //clean the fields
        clearForm((ViewGroup) findViewById(R.id.demandTab));
        clearForm((ViewGroup) findViewById(R.id.supplyTab));

        //TODO SHARE

        Toast.makeText(MapsActivity.this, "Shared!", Toast.LENGTH_LONG).show();
    }

    private void dateSet(final EditText dateSet) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

            private void updateLabel() {
                dateSet.setText(sdf.format(calendar.getTime()));
            }
        };
        new DatePickerDialog(dateSet.getContext(), date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean shareCheck(EditText weight, EditText dateFrom, EditText dateTo) {
        boolean emptyFields = false;
        if(TextUtils.isEmpty(weight.getText().toString())) {
            Toast.makeText(MapsActivity.this, "Weight needed", Toast.LENGTH_LONG).show();
            emptyFields = true;
        }
        if(TextUtils.isEmpty(dateFrom.getText().toString())) {
            Toast.makeText(MapsActivity.this, "Date From needed", Toast.LENGTH_LONG).show();
            emptyFields = true;
        }
        if(TextUtils.isEmpty(dateTo.getText().toString())) {
            Toast.makeText(MapsActivity.this, "Date To needed", Toast.LENGTH_LONG).show();
            emptyFields = true;
        }
        if (emptyFields) {
            return false;
        }

        double weightValue = Double.parseDouble(weight.getText().toString());
        Date dateFromValue = null;
        try {
            dateFromValue = sdf.parse(dateFrom.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date dateToValue = null;
        try {
            dateToValue = sdf.parse(dateTo.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return shareCheckHelper(weightValue, dateFromValue, dateToValue);
    }

    private boolean shareCheckHelper(double weight, Date dateFrom,
                               Date dateTo){
        //TODO fix, shouldnt work unless date set sets to 23:59:59
        return weight > 0 && dateTo.after(dateFrom) && dateFrom.after(Calendar.getInstance().getTime());
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mSensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void showCurrentPlace(final TextView addressView) {
        if (mMap == null) {
            return;
        }
        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission")
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            final int mMaxEntries = 5;
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
            final View mView = getLayoutInflater().inflate(R.layout.dialog_select_closest_address, null);
            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            final Button refresh = (Button) mView.findViewById(R.id.refresh);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    showCurrentPlace(addressView);
                }
            });
            final LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int px24 = dpToPx(24);
            marginParams.setMargins(px24, 0, px24, px24);
            dialog.show();
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                    int i = 0;
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        // Build a list of likely places to show the user. Max 5.
                        final TextView view = (TextView) dialog.findViewById(getResources()
                                .getIdentifier("address" + (i + 1), "id", getPackageName()));
                        if(view == null){
                            continue;
                        }
                        view.setText(placeLikelihood.getPlace().getAddress());
                        view.setLayoutParams(marginParams);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addressView.setText(view.getText());
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
                        Toast.makeText(MapsActivity.this, "No nearby addresses found" +
                                "\nPlease check your connection", Toast.LENGTH_SHORT).show();
                    }
                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                }
            });
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        // Position the map's camera in Hong Kong.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.3964, 114.1095), 10.0f));
        initializeGooglePlayServices();
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.5f));
                return false;
            }
        });
        //Locks the camera to Hong Kong
        mMap.setLatLngBoundsForCameraTarget(HONGKONG);
        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(25.0f);
    }

    private void initializeGooglePlayServices() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mLocationPermissionGranted = true;
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (mGoogleApiClient, mLocationRequest, this);
        }
        if (adapter != null){
            adapter.setClient(mGoogleApiClient);
        } else {
            AutocompleteFilter.Builder filterBuilder = new AutocompleteFilter.Builder();
            filterBuilder.setCountry(ISO3166_ALPHA2_COUNTRYCODE);
            adapter = new PlacesAPIAutocompleteAdapter(MapsActivity.this, mGoogleApiClient, HONGKONG, filterBuilder.build());
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastKnownLocation = location;
        if(firstTime){
            //TODO hacky fix
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                    (new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), 17.5f));
            firstTime = false;
        }
    }

    private void clearForm(ViewGroup group)
    {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText)view).setText("");
            }

            if(view instanceof ViewGroup && (((ViewGroup)view).getChildCount() > 0))
                clearForm((ViewGroup)view);
        }
    }


}
