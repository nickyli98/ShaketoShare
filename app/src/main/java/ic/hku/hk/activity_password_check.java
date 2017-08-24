package ic.hku.hk;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import static ic.hku.hk.Constants.*;

import java.sql.SQLException;

public class activity_password_check extends AppCompatActivity {


    private EditText firstPin;
    private EditText secondPin;
    private EditText thirdPin;
    private EditText fourthPin;
    private ProgressBar progressBarR;
    private String firstAttempt;
    private PasswordInput input;
    private EditText hiddenText;
    private Button passwordNext;
    private String PIN;
    private String phoneNumber;
    private String name;
    private String email;
    private String company;

    private DatabaseConnection dbc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_check);
        initiate();
        input = new PasswordInput(activity_password_check.this, firstPin, secondPin, thirdPin, fourthPin, hiddenText);
        passwordNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PIN = input.getPassword();
                phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");
                name = getIntent().getStringExtra("NAME");
                email = getIntent().getStringExtra("EMAIL");
                company = getIntent().getStringExtra("COMPANY");
                progressBarR.setVisibility(View.VISIBLE);
                new createUserTask().execute(name, email, company, phoneNumber, PIN);
            }});
    }

    private void run() throws InterruptedException {
        while(true){
            firstAttempt = input.getPassword();
            if(firstAttempt != null){
                break;
            }
            wait(100);
        }
        confirmPassword(firstAttempt);
    }

    private void confirmPassword(final String firstAttempt) throws InterruptedException {
        clearForm();
        input = new PasswordInput(activity_password_check.this, firstPin, secondPin, thirdPin, fourthPin, hiddenText);
        String s;
        while(true){
            s = input.getPassword();
            if(s != null){
                break;
            }
            wait(100);
        }
        if(s.equals(firstAttempt)){
            createAccount();
        } else {
            //TODO
        }
    }

    private void createAccount(){

    }

    private void clearForm() {
        firstPin.setText("");
        secondPin.setText("");
        thirdPin.setText("");
        fourthPin.setText("");
        hiddenText.setText("");
        input.showSoftKeyboard(firstPin);
    }

    private void initiate() {
        firstPin = (EditText) findViewById(R.id.first_pin_R);
        secondPin = (EditText) findViewById(R.id.second_pin_R);
        thirdPin = (EditText) findViewById(R.id.third_pin_R);
        fourthPin = (EditText) findViewById(R.id.fourth_pin_R);
        hiddenText = (EditText) findViewById(R.id.pin_hidden_edittext_R);
        passwordNext = (Button) findViewById(R.id.passwordNext);
        progressBarR = (ProgressBar) findViewById(R.id.progressBarR);
    }

    private class createUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Intent toMap = new Intent(activity_password_check.this, MapsActivity.class);
                toMap.putExtra("phoneNumber", phoneNumber);
                Toast.makeText(activity_password_check.this, getResources().getString(R.string.made_user_success), Toast.LENGTH_SHORT).show();
                startActivity(toMap);
                finish();
            } else {
                Intent toRegister = new Intent(activity_password_check.this, RegisterActivity.class);
                Toast.makeText(activity_password_check.this, getResources().getString(R.string.failedToMakeUser), Toast.LENGTH_SHORT).show();
                startActivity(toRegister);
                finish();
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                dbc = new DatabaseConnection(USER, PASSWORD, IP, DBNAME);
                return dbc.createUser(strings[0], strings[1], strings[2], strings[3], strings[4]);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
