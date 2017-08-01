package ic.hku.hk;

import android.Manifest;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.Toast;
import android.os.Vibrator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static ic.hku.hk.AndroidUtils.*;
import static ic.hku.hk.Constants.*;
import static ic.hku.hk.addressDialog.pickUpAddressDialog;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Google API
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private PlacesAPIAutocompleteAdapter adapter;
    private boolean firstTime = true;

    //Android UI Elements
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

        initializeAndroidUI();

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
                mGoogleApiClient.connect();
                if(!mGoogleApiClient.isConnected()){
                    Toast.makeText(MapsActivity.this
                            , "Failed to connect to Google API Servers\nPlease check your connection."
                            , Toast.LENGTH_SHORT).show();
                    return;
                }
                pickUpAddressDialog(MapsActivity.this, mGoogleApiClient
                        , pickUpAddress, adapter, showCurrentPlaceCheck(), mMap);
            }
        });

        layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                return;
            }

            @Override
            public void onPanelStateChanged(View panel
                    , SlidingUpPanelLayout.PanelState previousState
                    , SlidingUpPanelLayout.PanelState newState) {
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

    public boolean showCurrentPlaceCheck() {
        return (mMap != null && mLocationPermissionGranted);
    }

    private void initializeAndroidUI() {

        //Tab
        shareButton = (Button) findViewById(R.id.share);
        minimiseButton = (Button) findViewById(R.id.minimise);
        host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Supply tab
        TabHost.TabSpec spec = host.newTabSpec("Supply Tab");
        spec.setContent(R.id.supplyTab);
        spec.setIndicator("Supply");
        host.addTab(spec);
        supplyOrganic = (Switch) findViewById(R.id.supplyOrganic);
        supplyWeight = (EditText) findViewById(R.id.supplyWeight);
        supplyDateFrom = (EditText) findViewById(R.id.supplyDateFrom);
        supplyDateTo = (EditText) findViewById(R.id.supplyDateTo);
        pickUpAddress = (EditText) findViewById(R.id.pickUpAddress);

        //Demand tab
        spec = host.newTabSpec("Demand Tab");
        spec.setContent(R.id.demandTab);
        spec.setIndicator("Demand");
        host.addTab(spec);
        demandOrganic = (Switch) findViewById(R.id.demandOrganic);
        demandWeight = (EditText) findViewById(R.id.demandWeight);
        demandDateFrom = (EditText) findViewById(R.id.demandDateFrom);
        demandDateTo = (EditText) findViewById(R.id.demandDateTo);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            adapter.setClient(null);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
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
        }
    }

    private void handleShakeEvent(int count) {
        if (layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
            layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            minimiseButton.setVisibility(View.VISIBLE);
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

    //Called when map ready. Prompts to install Google Play Services if not installed
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HONGKONGCENTER, 10.0f));
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
                mGoogleApiClient = buildGoogleApiClient(this);
                mGoogleApiClient.connect();
                mLocationPermissionGranted = true;
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            mGoogleApiClient = buildGoogleApiClient(this);
            mGoogleApiClient.connect();
            mMap.setMyLocationEnabled(true);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
    //Requests location permission - Handled by a callback (onRequestPermissionsResult
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

}
