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
            startActivity(toMap);
            finish();
        }
    }
}
