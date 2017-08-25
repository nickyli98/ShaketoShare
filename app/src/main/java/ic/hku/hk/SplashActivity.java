package ic.hku.hk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar_FullScreen);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (SaveSharedPreference.getUserName(this).length() == 0) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            finish();
        } else {
            Intent toMap = new Intent(this, MapsActivity.class);
            toMap.putExtra("phoneNumber", SaveSharedPreference.getUserName(this));
            //if it has been called by opening notification then send extras to intents for dialog
            if (getIntent().getExtras() != null) {
                final boolean isSup = getIntent().getExtras().getBoolean("isSup");
                final String price = getIntent().getExtras().getString("price");
                final String weight = getIntent().getExtras().getString("weight");
                final String pairDeadline = getIntent().getExtras().getString("pairDeadline");
                final String pairName = getIntent().getExtras().getString("pairName");
                final String pairAddress = getIntent().getExtras().getString("pairAddress");
                final String pairNumber = getIntent().getExtras().getString("pairNumber");
                final String transactionID = getIntent().getExtras().getString("transactionID");
                toMap.putExtra("isSup", isSup);
                toMap.putExtra("price", price);
                toMap.putExtra("weight", weight);
                toMap.putExtra("pairDeadline", pairDeadline);
                toMap.putExtra("pairName", pairName);
                toMap.putExtra("pairAddress", pairAddress);
                toMap.putExtra("pairNumber", pairNumber);
                toMap.putExtra("transactionID", transactionID);
            }
            startActivity(toMap);
            finish();
        }
    }
}
