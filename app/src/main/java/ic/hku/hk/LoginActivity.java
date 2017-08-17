package ic.hku.hk;

import android.content.Intent;
import android.app.Activity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static ic.hku.hk.Constants.*;
import static ic.hku.hk.AreaCodeDialog.*;

public class LoginActivity extends Activity implements AsyncResponse {

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
    private ProgressBar p;

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

        p = findViewById(R.id.loginLoading);

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
                    final AttemptLoginTask task = new AttemptLoginTask();
                    task.delegate = LoginActivity.this;
                    task.execute(areaCodeSelectionText.getText().toString().substring(1) + "-" + phoneNumberEditText.getText().toString(), hiddenText.getText().toString());
                    p.setVisibility(View.VISIBLE);
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
        Intent toRegister = new Intent(this, RegisterActivity.class);
        toRegister.putExtra("phoneNumber", phoneNumberEditText.getText().toString());
        startActivity(toRegister);
        this.finish();
    }

    @Override
    public <T> void processFinish(T output) {
        if((boolean) (Object) output){
            Intent toMap = new Intent(this, MapsActivity.class);
            toMap.putExtra("phoneNumber", areaCodeSelectionText.getText().toString().substring(1) + "-" + phoneNumberEditText.getText().toString());
            startActivity(toMap);
            this.finish();
        } else {
            p.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Invalid number or password", Toast.LENGTH_SHORT).show();
        }

    }

    private class AttemptLoginTask extends AsyncTask<String, Void, Boolean>{

        public AsyncResponse delegate = null;

        public AttemptLoginTask() {
        }

        @Override
        protected Boolean doInBackground(String... texts) {
            DatabaseConnection dbc = new DatabaseConnection(USER, PASSWORD, IP, DBNAME);
            try{
                if(dbc.confirmPassword(texts[0], texts[1])){
                    dbc.closeConnection();
                    return true;
                } else {
                    dbc.closeConnection();
                    return false;
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                return false;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            delegate.processFinish(success);
        }
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
