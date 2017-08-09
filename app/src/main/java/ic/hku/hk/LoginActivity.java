package ic.hku.hk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static ic.hku.hk.Constants.*;
import static ic.hku.hk.AreaCodeDialog.*;

public class LoginActivity extends Activity {

    // UI references.
    private LinearLayout areaCodeSelection;
    private TextView areaCodeSelectionText;
    private ImageView areaCodeSelectionImage;
    private EditText phoneNumberEditText;
    private EditText passwordEditText;
    private TextView createAccount;
    private Button loginButton;
    private int areaCode;
    private PasswordInput input;
    private EditText firstPin;
    private EditText secondPin;
    private EditText thirdPin;
    private EditText fourthPin;
    private EditText hiddenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AndroidUtils.checkConnection(this);

        //Initializes UI
        areaCodeSelection = findViewById(R.id.areaCodeSelection);
        areaCodeSelectionText = findViewById(R.id.areaCodeSelection_text);
        areaCodeSelectionImage = findViewById(R.id.areaCodeSelection_image);
        phoneNumberEditText = findViewById(R.id.loginPhoneNumber);

        firstPin = findViewById(R.id.first_pin);
        secondPin = findViewById(R.id.second_pin);
        thirdPin = findViewById(R.id.third_pin);
        fourthPin = findViewById(R.id.fourth_pin);
        hiddenText = findViewById(R.id.pin_hidden_edittext);

        input = new PasswordInput(LoginActivity.this, firstPin, secondPin, thirdPin, fourthPin, hiddenText);

        loginButton = findViewById(R.id.loginButton);
        createAccount = findViewById(R.id.createAccount);

        areaCodeSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaCodeDialog(LoginActivity.this, areaCodeSelectionText, areaCodeSelectionImage);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AndroidUtils.isConnected(LoginActivity.this)) {
                    attemptLogin();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.check_internet_connection, Toast.LENGTH_LONG).show();
                }
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateAccount();
            }
        });
    }

    private void attemptCreateAccount() {
        //TODO validation
        Intent toRegister = new Intent(this, RegisterActivity.class);
        toRegister.putExtra("phoneNumber", phoneNumberEditText.getText().toString());
        startActivity(toRegister);
        this.finish();
    }

    private void attemptLogin() {
        //TODO validation, get text from hiddentext
        Intent toMap = new Intent(this, MapsActivity.class);
        ProgressBar p = findViewById(R.id.loginLoading);
        p.setVisibility(View.VISIBLE);
        startActivity(toMap);
        this.finish();
    }

    public int getAreaCode(){
        switch(((TextView) findViewById(R.id.areaCodeSelection_text)).getText().toString()) {
            case ("+852"):
                return HONGKONG_AREA_CODE;
            case ("+853"):
                return MACAU_AREA_CODE;
            case ("+86"):
                return CHINA_AREA_CODE;
            default:
                return 0;
        }
    }
}
