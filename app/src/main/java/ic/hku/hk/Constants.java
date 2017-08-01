package ic.hku.hk;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.SimpleDateFormat;
import java.util.Locale;

class Constants {

    static final String dateFormat = "yyyy/MM/dd"; //In which you need put here
    static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.TRADITIONAL_CHINESE);
    static final String ISO3166_ALPHA2_COUNTRYCODE = "HK";
    static final String TAG = MapsActivity.class.getSimpleName();
    static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static final LatLngBounds HONGKONG
            = new LatLngBounds(new LatLng(22.17, 113.82), new LatLng(22.54, 114.38));
    static final LatLng HONGKONGCENTER = new LatLng(22.3964, 114.1095);
}
