package ic.hku.hk;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import static ic.hku.hk.DatabaseVariables.*;
import static ic.hku.hk.TransactionListUtil.*;

public class activity_history_orders extends AppCompatActivity implements AsyncResponse{

    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_orders);
        listAdd(this, historyTransactions, R.id.transactionHistoryScroll, true);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeUpContainerHistory);
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

    private MapsActivity.GetOrderTask setTask(){
        MapsActivity.GetOrderTask g = new MapsActivity.GetOrderTask();
        g.delegate = this;
        return g;
    }

    @Override
    public <T> void processFinish(T output) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.transactionHistoryScroll);
        layout.removeAllViews();
        listAdd(this, historyTransactions, R.id.transactionHistoryScroll, true);
        refreshLayout.setRefreshing(false);
    }
}
