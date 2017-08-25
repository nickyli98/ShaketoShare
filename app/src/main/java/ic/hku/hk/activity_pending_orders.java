package ic.hku.hk;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import static ic.hku.hk.DatabaseVariables.*;
import static ic.hku.hk.TransactionListUtil.listAdd;
import static ic.hku.hk.MapsActivity.StaticGetOrderTask;

public class activity_pending_orders extends AppCompatActivity implements AsyncResponse{

    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);
        listAdd(this, pendingTransactions, R.id.transactionPendingScroll, false);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeUpContainer);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setTask().execute();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        refreshLayout.setRefreshing(true);
        setTask().execute();
    }

    private StaticGetOrderTask setTask(){
        StaticGetOrderTask g = new StaticGetOrderTask();
        g.delegate = this;
        return g;
    }

    @Override
    public <T> void processFinish(T output) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.transactionPendingScroll);
        layout.removeAllViews();
        listAdd(this, pendingTransactions, R.id.transactionPendingScroll, false);
        refreshLayout.setRefreshing(false);
    }
}
