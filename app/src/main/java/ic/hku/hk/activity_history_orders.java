package ic.hku.hk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static ic.hku.hk.DatabaseVariables.*;
import static ic.hku.hk.TransactionListUtil.*;

public class activity_history_orders extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_orders);
        listAdd(this, historyTransactions, R.id.transactionHistoryScroll, true);
    }



}
