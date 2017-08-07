package ic.hku.hk;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import static ic.hku.hk.Constants.*;

import java.sql.SQLException;

public class activity_password_check extends AppCompatActivity {


    private EditText firstPin;
    private EditText secondPin;
    private EditText thirdPin;
    private EditText fourthPin;
    private String firstAttempt;
    private PasswordInput input;
    private EditText hiddenText;
    private Button passwordNext;
    private String PIN;
    private String phoneNumber;
    private String name;
    private String email;
    private String company;

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
                new createUserTask().execute();
                //send to db
                //verify
                //send to app
            }
        });
        //run();
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
    }

    private class createUserTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...voids) {
            DatabaseConnection dbc = new DatabaseConnection(USER, PASSWORD, IP, DBNAME);
            try {
                System.out.println("1");
                if (dbc.createUser(name, company, email, phoneNumber, PIN)) {
                    System.out.println("2");
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.made_user_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                    startActivity(intent);
                    activity_password_check.this.finish();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("3");
            return null;
        }
    }

}
