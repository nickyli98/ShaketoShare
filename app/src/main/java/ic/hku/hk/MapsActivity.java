package ic.hku.hk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
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
import static ic.hku.hk.AddressDialog.pickUpAddressDialog;
import static ic.hku.hk.LanguageDialog.languageDialog;

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
    private LinearLayout buttonBar;
    private LinearLayout insidePane;
    private Button shareButton;
    private SegmentedGroup supplyDemandSwitch;
    private RadioButton supplyOn;
    private RadioButton demandOn;
    private Switch organicSwitch;
    private EditText weightEditText;
    private EditText dateFromEditText;
    private EditText dateToEditText;
    private EditText pickUpAddress;

    //Settings menu
    private TextView pendingOrders;
    private TextView orderHistory;
    private TextView contactUs;
    private TextView logOut;
    private LinearLayout languageSetting;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private LinearLayout weightLayout;

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

        pendingOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

        orderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        languageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageDialog(MapsActivity.this, languageSetting);
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
                openAddressDialog();
            }
        });

        weightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weightEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(weightEditText, InputMethodManager.SHOW_IMPLICIT);
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
                        collapseUI();
                    } else if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                        expandUI();
                    }
                } else if (newState.equals(SlidingUpPanelLayout.PanelState.DRAGGING)) {
                    if (previousState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                        expandUI();
                    } else if (previousState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                        collapseUI();
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
                if (buttonBar.getVisibility() == View.VISIBLE
                        && insidePane.getVisibility() == View.VISIBLE
                        && layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    vibrate();
                }
            }
        });

        supplyOn.setChecked(true);
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
        buttonBar = (LinearLayout) findViewById(R.id.buttonBar);
        insidePane = (LinearLayout) findViewById(R.id.insidePane);

        //Swipe up menu buttons
        shareButton = (Button) findViewById(R.id.share);
        supplyDemandSwitch = (SegmentedGroup) findViewById(R.id.supplyDemandSwitch);
        supplyOn = (RadioButton) findViewById(R.id.supplyOn);
        demandOn = (RadioButton) findViewById(R.id.demandOn);

        //Form elements
        organicSwitch = (Switch) findViewById(R.id.organic);
        weightEditText = (EditText) findViewById(R.id.weight);
        dateFromEditText = (EditText) findViewById(R.id.dateFrom);
        dateToEditText = (EditText) findViewById(R.id.dateTo);
        pickUpAddress = (EditText) findViewById(R.id.pickUpAddress);
        weightLayout = (LinearLayout) findViewById(R.id.weightBox);

        //Settings elements
        pendingOrders = (TextView) findViewById(R.id.settings_pendingOrders);
        orderHistory = (TextView) findViewById(R.id.settings_orderHistory);
        contactUs = (TextView) findViewById(R.id.settings_contactUs);
        logOut = (TextView) findViewById(R.id.settings_logOut);
        languageSetting = (LinearLayout) findViewById(R.id.settings_language);

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
                supplyDemandSwitch.setVisibility(View.VISIBLE);
                break;
            case EXPANDED:
                boolean shareCheckPassed = shareCheck(weightEditText, dateFromEditText,dateToEditText, pickUpAddress);
                if (shareCheckPassed) {
                    shareRequest();
                    collapseUI();
                }
        }
    }

    private void vibrate() {
        layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        supplyDemandSwitch.setVisibility(View.VISIBLE);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
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
                //if in select from map case then reopen UI
                if (centreMap.getVisibility() == View.VISIBLE) {
                    openAddressDialog();
                    returnToAddressDialogUI();
                } else {
                    finish();
                }
                break;
            case EXPANDED:
                layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                break;
        }
    }

    private void returnToAddressDialogUI() {
        centreMap.setVisibility(View.INVISIBLE);
        selectFromMapDone.setVisibility(View.INVISIBLE);
        addressPreview.setVisibility(View.INVISIBLE);
        buttonBar.setVisibility(View.VISIBLE);
        insidePane.setVisibility(View.VISIBLE);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        layout.setTouchEnabled(true);
    }

    private void openAddressDialog() {
        pickUpAddressDialog(MapsActivity.this, mGoogleApiClient
                , pickUpAddress, adapter, showCurrentPlaceCheck()
                , mMap, layout, geocoder, centreMap, selectFromMapDone
                , addressPreview, buttonBar, insidePane);
    }

    private void shareRequest() {
        //TODO: clear the fields

        //TODO SHARE

        Toast.makeText(MapsActivity.this, R.string.SharedToast, Toast.LENGTH_LONG).show();
    }

    private boolean shareCheck(final EditText weight, final EditText dateFrom, final EditText dateTo, final EditText address) {
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
        pickUpAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                pickUpAddress.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return shareCheckHelper(weight, dateFrom, dateTo, pickUpAddress);
    }

    private boolean weightEmpty(final EditText weightEditText) {
        if(TextUtils.isEmpty(weightEditText.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.weight_needed), Toast.LENGTH_SHORT).show();
            weightEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        }
        return false;
    }

    private boolean addressEmpty(EditText addressEditText) {
        if(TextUtils.isEmpty(addressEditText.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.address_needed), Toast.LENGTH_SHORT).show();
            addressEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        }
        return false;
    }

    private boolean dateOrderRight(EditText dateFromEditText, EditText dateToEditText) {
        try {
            Date dateFrom = sdf.parse(dateFromEditText.getText().toString());
            Date dateTo = sdf.parse(dateToEditText.getText().toString());
            //Time is set for date comparison to work
            dateFrom.setSeconds(58);
            dateFrom.setMinutes(59);
            dateFrom.setHours(23);
            dateTo.setSeconds(59);
            dateTo.setMinutes(59);
            dateTo.setHours(23);
            if (dateTo.before(dateFrom)) {
                Toast.makeText(this, getResources().getString(R.string.date_order_wrong), Toast.LENGTH_SHORT).show();
                dateToEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
                return false;
            }
            if (!dateFrom.after(Calendar.getInstance().getTime())) {
                Toast.makeText(this, getResources().getString(R.string.date_from_wrong), Toast.LENGTH_SHORT).show();
                dateFromEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
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

    private boolean shareCheckHelper(EditText weightEditText, EditText dateFromEditText, EditText dateToEditText, EditText addressEditText){
        return !addressEmpty(addressEditText) && !weightEmpty(weightEditText) && weightPositive(weightEditText) && dateOrderRight(dateFromEditText, dateToEditText);
    }

    private void collapseUI() {
        supplyDemandSwitch.setVisibility(View.INVISIBLE);
        hideKeyboard();
        weightEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
        dateFromEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
        dateFromEditText.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
        pickUpAddress.setBackgroundTintList(getResources().getColorStateList(R.color.inputText, MapsActivity.this.getTheme()));
    }

    private void expandUI() {
        supplyDemandSwitch.setVisibility(View.VISIBLE);
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
