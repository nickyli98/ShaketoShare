package ic.hku.hk;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import static ic.hku.hk.DatabaseVariables.*;
import static ic.hku.hk.TransactionListUtil.*;

public class activity_history_orders extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_orders);
        listAdd(this, historyTransactions, R.id.transactionHistoryScroll, true);
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeUpContainerHistory);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new MapsActivity.getOrders().execute();
                refresh();
            }
        });
    }

    private void refresh() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.transactionHistoryScroll);
        layout.removeAllViews();
        listAdd(this, pendingTransactions, R.id.transactionHistoryScroll, false);
    }
}
