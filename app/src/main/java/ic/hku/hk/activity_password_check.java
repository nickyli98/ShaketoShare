package ic.hku.hk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class activity_password_check extends AppCompatActivity {


    private EditText firstPin;
    private EditText secondPin;
    private EditText thirdPin;
    private EditText fourthPin;
    private String firstAttempt;
    private PasswordInput input;
    private EditText hiddenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_check);
        initiate();
        input = new PasswordInput(activity_password_check.this, firstPin, secondPin, thirdPin, fourthPin, hiddenText);
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
    }

}
