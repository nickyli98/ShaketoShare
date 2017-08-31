package ic.hku.hk;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.SeekBar;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.SphericalUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.w3c.dom.Text;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ic.hku.hk.AndroidUtils.*;
import static ic.hku.hk.Constants.*;
import static ic.hku.hk.AddressDialog.pickUpAddressDialog;
import static ic.hku.hk.DatabaseVariables.*;

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
    private TextView shareLat;
    private TextView shareLon;
    private Geocoder geocoder;
    private SlidingUpPanelLayout layout;
    private LinearLayout dragView;
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
    private LinearLayout radiusMenuBox;
    private SeekBar seekBarRadius;
    private Circle radiusCircle;
    private TextView radiusLengthText;
    private RadioButton supplyOnRadius;
    private RadioButton demandOnRadius;
    private List<Marker> markerList;
    private LinearLayout markerInfo;
    private TextView radiusMarkerAddress;
    private TextView radiusMarkerDateFrom;
    private TextView radiusMarkerDateTo;
    private TextView radiusMarkerOrganic;
    private TextView radiusMarkerWeight;
    private TextView radiusMarkerPrice;
    private TextView radiusMarkerPriceVal;
    private Button matchBidButton;
    private EditText bidAmount;

    //Settings menu
    private ImageView drawerOpen;
    private DrawerLayout drawerLayout;
    private LinearLayout pendingOrders;
    private LinearLayout orderHistory;
    private LinearLayout radiusOfItems;
    private LinearLayout contactUs;
    private LinearLayout logOut;
    private ImageView backArrow;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private LinearLayout weightLayout;
    ShakeDetector.OnShakeListener onShakeListener;

    //Current user
    private String phoneNumber;
    private String nameOfUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent i = getIntent();
        phoneNumber = i.getStringExtra("phoneNumber");
        new createDBC().execute(phoneNumber);

        initializeAndroidUI();


        layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AndroidUtils.isConnected(MapsActivity.this)) {
                    share();
                } else {
                    Toast.makeText(MapsActivity.this, R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
                }
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

        drawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        pendingOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toPending = new Intent(MapsActivity.this, activity_pending_orders.class);
                startActivity(toPending);
            }
        });

        orderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toHistory = new Intent(MapsActivity.this, activity_history_orders.class);
                startActivity(toHistory);
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveRadiusUI();
            }
        });

        radiusOfItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawers();
                radiusMenuBox.setVisibility(View.VISIBLE);
                backArrow.setVisibility(View.VISIBLE);
                dragView.setVisibility(View.INVISIBLE);
                insidePane.setVisibility(View.INVISIBLE);
                buttonBar.setVisibility(View.INVISIBLE);
                layout.setEnabled(false);
                disableShake();
                LatLng centre = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                final double radius = 500;
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(centre).radius(radius).fillColor(getColor(R.color.colorAccentOpaque));
                circleOptions.strokeColor(getColor(R.color.colorAccent));
                circleOptions.strokeWidth(dpToPx(1, MapsActivity.this));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                radiusCircle = mMap.addCircle(circleOptions);
                seekBarRadius.setProgress(5);
                radiusLengthText.setText("0.5km");
                supplyOnRadius.setChecked(true);
                if (AndroidUtils.isConnected(MapsActivity.this)) {
                    new getRadiusItems().execute(centre);
                } else {
                    Toast.makeText(MapsActivity.this, R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
                }

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        final Transaction transaction = (Transaction) marker.getTag();
                        if (transaction != null) {
                            radiusMarkerAddress.setText(transaction.getAddress());
                            radiusMarkerDateFrom.setText(transaction.getDateFrom());
                            radiusMarkerDateTo.setText(transaction.getDateTo());
                            radiusMarkerOrganic.setText((transaction.getOrganic() ? getString(R.string.organic) : getString(R.string.inorganic)));
                            radiusMarkerPrice.setText((transaction.isSupply()) ? getString(R.string.asking_price) : getString(R.string.current_offer));
                            radiusMarkerPriceVal.setText(transaction.getBid());
                            radiusMarkerWeight.setText(String.valueOf(transaction.getWeight()));
                            matchBidButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    leaveRadiusUI();
                                    if (supplyOnRadius.isChecked()) {
                                        demandOn.setChecked(true);
                                    } else {
                                        supplyOn.setChecked(true);
                                    }
                                    if (transaction.getOrganic()) {
                                        organicSwitch.setChecked(true);
                                    } else {
                                        organicSwitch.setChecked(false);
                                    }
                                    weightEditText.setText(String.valueOf(transaction.getWeight()));
                                    layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                                    presetBid = transaction.getBid();
                                }
                            });
                            markerInfo.setVisibility(View.VISIBLE);
                            markerInfo.post(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.setPadding(0, 0, 0, markerInfo.getHeight());
                                }
                            });
                        }
                        return false;
                    }
                });
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        hideMarkerInfo();
                    }
                });
                seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        //change UI here
                        radiusLengthText.setText(i/10.0 + "km");
                        radiusCircle.setRadius(i*100);
                        showMarkers(seekBar, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(radiusCircle.getCenter(), getZoomLevel(radiusCircle)));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                demandOnRadius.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMarkers(seekBarRadius, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        hideMarkerInfo();
                    }
                });
                supplyOnRadius.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMarkers(seekBarRadius, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        hideMarkerInfo();
                    }
                });
            }
        });

        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:852-12345678"));
                startActivity(intent);
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveSharedPreference.clearUserName(MapsActivity.this);
                new LogoutTokenTask().execute();
                Intent toLogout = new Intent(MapsActivity.this, LoginActivity.class);
                startActivity(toLogout);
                close();
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
        onShakeListener = new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if (buttonBar.getVisibility() == View.VISIBLE
                        && insidePane.getVisibility() == View.VISIBLE
                        && layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    vibrate();
                }
            }
        };
        enableShake();

        supplyOn.setChecked(true);
    }

    private void notificationDialog() {
        Bundle bundle = getIntent().getExtras();
        Intent toDialog = new Intent(this, MatchedDialog.class);
        toDialog.putExtras(bundle);
        startActivity(toDialog);
    }

    private void hideMarkerInfo() {
        mMap.setPadding(0, 0, 0, 0);
        markerInfo.setVisibility(View.GONE);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(radiusCircle.getCenter(), getZoomLevel(radiusCircle)));
    }

    private float getZoomLevel(Circle radiusCircle) {
        float zoomLevel = 11.0f;
        if (radiusCircle != null) {
            double radius = radiusCircle.getRadius() + radiusCircle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (float)(16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    private void close() {
        finish();
    }

    private void leaveRadiusUI() {
        radiusMenuBox.setVisibility(View.GONE);
        backArrow.setVisibility(View.GONE);
        enableShake();
        mMap.clear();
        layout.setEnabled(true);
        dragView.setVisibility(View.VISIBLE);
        insidePane.setVisibility(View.VISIBLE);
        buttonBar.setVisibility(View.VISIBLE);
        mMap.setPadding(0, 0, 0, 0);
        markerInfo.setVisibility(View.GONE);
    }

    private void enableShake() {
        mShakeDetector.setOnShakeListener(onShakeListener);
    }

    private void disableShake() {
        mShakeDetector.setOnShakeListener(null);
    }

    private void showMarkers(SeekBar seekBar, double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        if (markerList != null) {
            for (Marker marker : markerList) {
                Transaction transaction = (Transaction) marker.getTag();
                if (SphericalUtil.computeDistanceBetween(latLng, marker.getPosition()) <= seekBar.getProgress() * 100) {
                    if (transaction != null) {
                        if (supplyOnRadius.isChecked()) {
                            if (transaction.isSupply()) {
                                marker.setVisible(true);
                            } else {
                                marker.setVisible(false);
                            }
                        } else if (demandOnRadius.isChecked()) {
                            if (!transaction.isSupply()) {
                                marker.setVisible(true);
                            } else {
                                marker.setVisible(false);
                            }
                        }
                    }
                } else {
                    marker.setVisible(false);
                }
            }
        }
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
        dragView = (LinearLayout) findViewById(R.id.dragView);
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

        //Invisible latitude/longitude elements for sharing location
        shareLat = (TextView) findViewById(R.id.shareLat);
        shareLon = (TextView) findViewById(R.id.shareLon);

        //radius elements
        radiusMenuBox = (LinearLayout) findViewById(R.id.radiusMenuBox);
        seekBarRadius = (SeekBar) findViewById(R.id.seekBarRadius);
        radiusLengthText = (TextView) findViewById(R.id.radiusLength);
        supplyOnRadius = (RadioButton) findViewById(R.id.supplyOnRadius);
        demandOnRadius = (RadioButton) findViewById(R.id.demandOnRadius);
        backArrow = (ImageView) findViewById(R.id.back_arrow);
        markerInfo = (LinearLayout) findViewById(R.id.markerInfo);
        radiusMarkerAddress = (TextView) findViewById(R.id.radius_marker_address);
        radiusMarkerDateFrom = (TextView) findViewById(R.id.radius_marker_dateFrom);
        radiusMarkerDateTo = (TextView) findViewById(R.id.radius_marker_dateTo);
        radiusMarkerOrganic = (TextView) findViewById(R.id.radius_marker_organic);
        radiusMarkerWeight = (TextView) findViewById(R.id.radius_marker_weight);
        radiusMarkerPrice = (TextView) findViewById(R.id.radius_marker_price);
        radiusMarkerPriceVal = (TextView) findViewById(R.id.radius_marker_priceVal);
        matchBidButton = (Button) findViewById(R.id.matchBidButton);
        bidAmount = (EditText) findViewById(R.id.bidAmount);

        //Settings elements
        drawerOpen = (ImageView) findViewById(R.id.drawer_open);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        pendingOrders = (LinearLayout) findViewById(R.id.settings_pendingOrderLayout);
        orderHistory = (LinearLayout) findViewById(R.id.settings_orderHistoryLayout);
        radiusOfItems = (LinearLayout) findViewById(R.id.settings_radiusLayout);
        contactUs = (LinearLayout) findViewById(R.id.settings_contactLayout);
        logOut = (LinearLayout) findViewById(R.id.settings_logoutLayout);

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
        //TODO NOTIFICATIONS PRESS BACK
        if (layout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            //if in select from map case then reopen UI
            if (centreMap.getVisibility() == View.VISIBLE) {
                openAddressDialog();
                returnToAddressDialogUI();
            } else if (radiusMenuBox.getVisibility() == View.VISIBLE) {
                leaveRadiusUI();
            } else {
                finish();
            }
        } else if (layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    private void sendNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.recycle_logo)
                .setContentTitle("Shake to Share")
                .setContentText("You pressed back");
        Intent resultIntent = new Intent(this, MapsActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setVibrate(new long [] {0, 1000});
        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
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
                , addressPreview, buttonBar, insidePane, shareLat, shareLon);
    }

    private void openBidDialog(String address, String lat, String lon,
                               String organic, String isSupply, String dateFrom,
                               String dateTo, String weight, DatabaseConnection dbc, String presetBid) {
        BidDialog bidDialog = new BidDialog(MapsActivity.this);
        bidDialog.bidDialog(address, lat, lon, organic, isSupply, dateFrom, dateTo, weight, dbc, presetBid);
        //reset share menu
        supplyOn.setChecked(true);
        organicSwitch.setChecked(true);
    }

    private void shareRequest() {
        //gets the fields
        String address = pickUpAddress.getText().toString();
        String lat = shareLat.getText().toString();
        String lon = shareLon.getText().toString();
        String organic = organicSwitch.isChecked() ? "1" : "0";
        String isSupply = supplyOn.isChecked() ? "1" : "0";
        String dateFrom = dateFromEditText.getText().toString();
        String dateTo = dateToEditText.getText().toString();
        String weight = weightEditText.getText().toString();
        //with these fields
        openBidDialog(address, lat, lon, organic, isSupply, dateFrom, dateTo, weight, dbc, presetBid);
        //resets the UI
        clearForm(insidePane);
        dateFromEditText.setText(sdf.format(Calendar.getInstance().getTime()));
        dateToEditText.setText(sdf.format(Calendar.getInstance().getTime()));
        layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
        mMap.getUiSettings().setRotateGesturesEnabled(false);
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

    private class createDBC extends AsyncTask<String, Void, DatabaseConnection>{
        @Override
        protected DatabaseConnection doInBackground(String... number) {
            return new DatabaseConnection(USER, PASSWORD, IP, DBNAME, number[0]);
        }

        @Override
        protected void onPostExecute(DatabaseConnection databaseConnection) {
            dbc = databaseConnection;
            MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
            new GetNameOfUserTask().execute();
            new GetOrderTask().execute();
        }
    }

    public static class StaticGetOrderTask extends AsyncTask<Void, Void, List<Transaction>[]>{

        AsyncResponse delegate = null;

        @Override
        protected List<Transaction>[] doInBackground(Void... voids) {
            List<Transaction>[] transactions = new List[2];
            try {
                transactions[0] = dbc.getOrders(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                transactions[1] = dbc.getOrders(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return transactions;
        }

        @Override
        protected void onPostExecute(List<Transaction>[] transactions) {
            pendingTransactions = transactions[0];
            historyTransactions = transactions[1];
            if(delegate != null){
                delegate.processFinish(true);
            }
        }
    }

    public class GetOrderTask extends AsyncTask<Void, Void, List<Transaction>[]>{

        AsyncResponse delegate = null;

        @Override
        protected List<Transaction>[] doInBackground(Void... voids) {
            List<Transaction>[] transactions = new List[2];
            try {
                transactions[0] = dbc.getOrders(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                transactions[1] = dbc.getOrders(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return transactions;
        }

        @Override
        protected void onPostExecute(List<Transaction>[] transactions) {
            pendingTransactions = transactions[0];
            historyTransactions = transactions[1];
            if (getIntent().getExtras().getString("transactionID") != null) {
                notificationDialog();
            }
            if(delegate != null){
                delegate.processFinish(true);
            }
        }
    }

    private class getRadiusItems extends AsyncTask<LatLng, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(LatLng... latLngs) {
            try {
                return dbc.getRadiusItems(latLngs[0]);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            radiusItems = transactions;
            markerList = new ArrayList<>();
            for (Transaction t : transactions) {
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(t.getLat(), t.getLng()));
                if (t.getOrganic()) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.organic_marker));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.inorganic_marker));
                }
                Marker marker = mMap.addMarker(markerOptions);
                marker.setVisible(false);
                marker.setTag(t);
                markerList.add(marker);
            }
            showMarkers(seekBarRadius, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        }
    }

    private class LogoutTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                dbc.clearToken();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetNameOfUserTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void ... voids) {
            try {
                return dbc.getNameOfUser(phoneNumber);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String name) {
            nameOfUser = name;
            updateName(nameOfUser);
        }
    }

    private void updateName(String nameOfUser) {
        TextView name = (TextView) findViewById(R.id.settings_name);
        String space = " ";
        String nameDisplay = space + nameOfUser;
        name.setText(nameDisplay);
    }

}
