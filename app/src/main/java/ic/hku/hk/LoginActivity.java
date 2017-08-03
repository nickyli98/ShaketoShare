package ic.hku.hk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends Activity {

        private static final String[] DUMMY_CREDENTIALS = new String[]{
            "69793034", "999"
    };

    // UI references.
    private LinearLayout areaCodeSelection;
    private EditText phoneNumberEditText;
    private EditText passwordEditText;
    private TextView createAccount;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        areaCodeSelection = findViewById(R.id.areaCodeSelection);
        phoneNumberEditText = (EditText) findViewById(R.id.loginPhoneNumber);
        passwordEditText = (EditText) findViewById(R.id.loginPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
        createAccount = (TextView) findViewById(R.id.createAccount);
    }
}

