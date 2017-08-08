package ic.hku.hk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;
import static ic.hku.hk.Constants.*;
import static ic.hku.hk.TransactionListUtil.listAdd;

public class activity_history_orders extends AppCompatActivity {

    private List<Transaction> transactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_orders);
        try {
            transactions = dbc.getOrders(true);
            listAdd(this, transactions, R.id.transactionHistoryScroll, true);
        } catch (SQLException e) {
            transactions = null;
            Toast.makeText(this, "Unable to connect to server", Toast.LENGTH_SHORT);
            finish();
            e.printStackTrace();
        }
    }


}
