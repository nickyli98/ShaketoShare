package ic.hku.hk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
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
import java.util.Locale;

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
    private ImageView centreMap;
    private Button selectFromMapDone;
    private TextView addressPreview;
    private Geocoder geocoder;
    private SlidingUpPanelLayout layout;
    private Button shareButton;
    private SegmentedGroup supplyDemandSwitch;
    private Button minimiseButton;
    private Switch organicSwitch;
    private EditText weightEditText;
    private EditText dateFromEditText;
    private EditText dateToEditText;
    private EditText pickUpAddress;

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

        dateFromEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(dateFromEditText);
            }
        });

        dateToEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(dateToEditText);
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
                            , R.string.FailedToConnectToast
                            , Toast.LENGTH_SHORT).show();
                    return;
                }
                pickUpAddressDialog(MapsActivity.this, mGoogleApiClient
                        , pickUpAddress, adapter, showCurrentPlaceCheck()
                        , mMap, layout, geocoder, centreMap, selectFromMapDone
                        , addressPreview);
            }
        });

        layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {}

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

        //Select from map
        centreMap = (ImageView) findViewById(R.id.centreMap);
        selectFromMapDone = (Button) findViewById(R.id.selectFromMapDone);
        addressPreview = (TextView) findViewById(R.id.addressPreview);
        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

        //Swipe up menu buttons
        shareButton = (Button) findViewById(R.id.share);
        minimiseButton = (Button) findViewById(R.id.minimise);
        supplyDemandSwitch = (SegmentedGroup) findViewById(R.id.supplyDemandSwitch);

        //Form elements
        organicSwitch = (Switch) findViewById(R.id.organic);
        weightEditText = (EditText) findViewById(R.id.weight);
        dateFromEditText = (EditText) findViewById(R.id.dateFrom);
        dateToEditText = (EditText) findViewById(R.id.dateTo);
        pickUpAddress = (EditText) findViewById(R.id.pickUpAddress);

        //Sets date default to current date
        dateFromEditText.setText(sdf.format(Calendar.getInstance().getTime()));
        dateToEditText.setText(sdf.format(Calendar.getInstance().getTime()));
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
                boolean shareCheckPassed = shareCheck(weightEditText, dateFromEditText,dateToEditText);
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
        //TODO: clear the fields

        //TODO SHARE

        Toast.makeText(MapsActivity.this, R.string.SharedToast, Toast.LENGTH_LONG).show();
    }

    private boolean shareCheck(final EditText weight, final EditText dateFrom, final EditText dateTo) {
        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                weightEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //auto-generated method stub
            }
        });
        dateFromEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dateFromEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //auto-generated method stub
            }
        });
        dateToEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dateToEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //auto-generated method stub
            }
        });
        return shareCheckHelper(weight, dateFrom, dateTo);
    }

    private boolean weightEmpty(final EditText weightEditText) {
        if(TextUtils.isEmpty(weightEditText.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.weight_needed), Toast.LENGTH_SHORT).show();
            weightEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        }
        return false;
    }

    private boolean dateOrderRight(EditText dateFromEditText, EditText dateToEditText) {
        try {
            Date dateFrom = sdf.parse(dateFromEditText.getText().toString());
            Date dateTo = sdf.parse(dateToEditText.getText().toString());
            if (dateTo.before(dateFrom)) {
                Toast.makeText(this, getResources().getString(R.string.date_order_wrong), Toast.LENGTH_SHORT).show();
                dateToEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputError));
                return false;
            }
            if (dateFrom.before(Calendar.getInstance().getTime())) {
                Toast.makeText(this, getResources().getString(R.string.date_from_wrong), Toast.LENGTH_SHORT).show();
                dateFromEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputError));
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean weightPositive(EditText weightEditText) {
        return Double.parseDouble(String.valueOf(weightEditText.getText())) > 0;
    }

    private boolean shareCheckHelper(EditText weightEditText, EditText dateFromEditText, EditText dateToEditText){
        return !weightEmpty(weightEditText) && weightPositive(weightEditText) && dateOrderRight(dateFromEditText, dateToEditText);
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
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
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
