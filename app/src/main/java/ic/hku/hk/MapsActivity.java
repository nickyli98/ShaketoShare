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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean firstTime = true;
    private LatLngBounds HONGKONG = new LatLngBounds(
            new LatLng(22.17, 113.82), new LatLng(22.54, 114.38));
    private final String dateFormat = "yyyy/MM/dd"; //In which you need put here
    private final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.TRADITIONAL_CHINESE);

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

        final Switch organic = (Switch) findViewById(R.id.organic);
        final EditText weight = (EditText) findViewById(R.id.weight);
        final EditText dateFrom = (EditText) findViewById(R.id.dateFrom);
        final EditText dateTo = (EditText) findViewById(R.id.dateTo);
        final EditText pickUpAddress = (EditText) findViewById(R.id.pickUpAddress);
        final Button shareButton = (Button) findViewById(R.id.share);
        final Button minimiseButton = (Button) findViewById(R.id.minimise);
        final SlidingUpPanelLayout layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (layout.getPanelState()) {
                    case COLLAPSED:
                        layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                        minimiseButton.setVisibility(View.VISIBLE);
                        break;
                    case EXPANDED:
                        if (shareCheck(weight, dateFrom, dateTo)) {
                            share();
                            layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            minimiseButton.setVisibility(View.INVISIBLE);
                        }
                }
            }
        });

        dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(dateFrom);
            }
        });

        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSet(dateTo);
            }
        });

        minimiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minimiseButton.setVisibility(View.INVISIBLE);
                layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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

    private void handleShakeEvent(int count) {

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
        super.onBackPressed();


    }

    private void share() {
        //TODO: write what would happen when share
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
            dateToValue = sdf.parse(dateFrom.getText().toString());
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
        if (mGoogleApiClient != null) {
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
