package ic.hku.hk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static ic.hku.hk.DatabaseVariables.*;

public class activity_pending_orders extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);
        //System.out.println("CHECK HERE - " + historyTransactions.size());
    }



}
