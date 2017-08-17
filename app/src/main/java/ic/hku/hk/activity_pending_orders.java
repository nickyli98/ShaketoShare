package ic.hku.hk;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import static ic.hku.hk.DatabaseVariables.*;
import static ic.hku.hk.TransactionListUtil.listAdd;

public class activity_pending_orders extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);
        listAdd(this, pendingTransactions, R.id.transactionPendingScroll, false);
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeUpContainer);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new MapsActivity.getOrders().execute();
                refresh();
            }
        });
    }

    private void refresh() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.transactionPendingScroll);
        layout.removeAllViews();
        listAdd(this, pendingTransactions, R.id.transactionPendingScroll, false);
    }

}
