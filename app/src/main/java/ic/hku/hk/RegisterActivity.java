package ic.hku.hk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static ic.hku.hk.AreaCodeDialog.areaCodeDialog;

public class RegisterActivity extends AppCompatActivity {

    private EditText loginPhoneNumber;
    private EditText name;
    private EditText email;
    private EditText company;
    private TextView areaCodeText;
    private ImageView areaCodeImage;
    private LinearLayout areaCodeSelection;
    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginPhoneNumber = (EditText) findViewById(R.id.loginPhoneNumber_R);
        loginPhoneNumber.setText(getIntent().getStringExtra("phoneNumber"), TextView.BufferType.EDITABLE);

        name = (EditText) findViewById(R.id.nameInput_R);
        email = (EditText) findViewById(R.id.emailInput_R);
        company = (EditText) findViewById(R.id.company_R);
        areaCodeText = (TextView) findViewById(R.id.areaCodeSelection_text_R);
        areaCodeImage = (ImageView) findViewById(R.id.areaCodeSelection_image_R);
        areaCodeSelection = (LinearLayout) findViewById(R.id.areaCodeSelection_R);
        areaCodeSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                areaCodeDialog(RegisterActivity.this, areaCodeText, areaCodeImage);
            }
        });
        initialiseListeners();

        next = (Button) findViewById(R.id.next_button_R);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validInput()) {
                    Intent toPassword = new Intent(RegisterActivity.this, activity_password_check.class);
                    toPassword.putExtra("PHONE_NUMBER", areaCodeText.getText().toString().substring(1) + "-" + loginPhoneNumber.getText().toString());
                    toPassword.putExtra("NAME", name.getText().toString());
                    toPassword.putExtra("EMAIL", email.getText().toString());
                    toPassword.putExtra("COMPANY", company.getText().toString());
                    startActivity(toPassword);
                    RegisterActivity.this.finish();
                }
            }
        });

    }

    private boolean phoneEmpty() {
        if (TextUtils.isEmpty(loginPhoneNumber.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.no_phone_number), Toast.LENGTH_SHORT).show();
            loginPhoneNumber.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        } else if (loginPhoneNumber.getText().toString().length() < 8) {
            Toast.makeText(this, getResources().getString(R.string.phone_number_short), Toast.LENGTH_SHORT).show();
            loginPhoneNumber.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        }
        return false;
    }

    private boolean nameEmpty() {
        if (TextUtils.isEmpty(name.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.no_name), Toast.LENGTH_SHORT).show();
            name.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        }
        return false;
    }

    private boolean emailEmpty() {
        if (TextUtils.isEmpty(email.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.no_email), Toast.LENGTH_SHORT).show();
            email.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        }
        return false;
    }

    private boolean companyEmpty() {
        if (TextUtils.isEmpty(company.getText().toString())) {
            Toast.makeText(this, getResources().getString(R.string.no_company), Toast.LENGTH_SHORT).show();
            company.setBackgroundTintList(getResources().getColorStateList(R.color.inputError, this.getTheme()));
            return true;
        }
        return false;
    }

    private void initialiseListeners() {
        loginPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loginPhoneNumber.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent, RegisterActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent, RegisterActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                email.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent, RegisterActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        company.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                company.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent, RegisterActivity.this.getTheme()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean validInput() {
        return !phoneEmpty() && !nameEmpty() && !emailEmpty() && !companyEmpty();
    }
}
