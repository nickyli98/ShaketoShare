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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static ic.hku.hk.Constants.*;
import static ic.hku.hk.AreaCodeDialog.*;

public class LoginActivity extends Activity {

    // UI references.
    private LinearLayout areaCodeSelection;
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

        //Initializes UI
        areaCodeSelection = findViewById(R.id.areaCodeSelection);
        phoneNumberEditText = (EditText) findViewById(R.id.loginPhoneNumber);

        firstPin = findViewById(R.id.first_pin);
        secondPin = findViewById(R.id.second_pin);
        thirdPin = findViewById(R.id.third_pin);
        fourthPin = findViewById(R.id.fourth_pin);
        hiddenText = findViewById(R.id.pin_hidden_edittext);

        input = new PasswordInput(LoginActivity.this, firstPin, secondPin, thirdPin, fourthPin, hiddenText);

        loginButton = (Button) findViewById(R.id.loginButton);
        createAccount = (TextView) findViewById(R.id.createAccount);

        areaCodeSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaCodeDialog(LoginActivity.this, areaCodeSelection);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
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
        startActivity(toRegister);
        this.finish();
    }

    private void attemptLogin() {
        //TODO validation, get text from hiddentext
        Intent toMap = new Intent(this, MapsActivity.class);
        //loginButton.setVisibility(View.GONE);
        ProgressBar p = findViewById(R.id.loginLoading);
        startActivity(toMap);
        p.setVisibility(View.VISIBLE);
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
