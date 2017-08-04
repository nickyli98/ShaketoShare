package ic.hku.hk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterActivity extends AppCompatActivity {

    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        next = (Button) findViewById(R.id.next_button_R);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Checking
                Intent toPassword = new Intent(RegisterActivity.this, activity_password_check.class);
                startActivity(toPassword);
                //setters
                RegisterActivity.this.finish();
            }
        });

    }
}
