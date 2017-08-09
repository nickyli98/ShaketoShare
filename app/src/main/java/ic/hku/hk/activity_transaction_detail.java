package ic.hku.hk;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class activity_transaction_detail extends Activity {

    private Transaction t;
    private boolean completed;
    private Button cancel;
    private UserInfo matchedInfo;
    private String otherAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        t = getIntent().getExtras().getParcelable("Transaction");
        completed = t instanceof CompletedTransaction;
        if(completed){
            matchedInfo = ((CompletedTransaction) t).getUserInfo();
        }
        cancel = findViewById(R.id.transactionCancel);
        initialize();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
    }

    private void initialize() {
        if(t != null){
            if(completed){
                ((TextView) findViewById(R.id.completedTransactionDetails_name)).setText(matchedInfo.getName());
                ((TextView) findViewById(R.id.completedTransactionDetails_phone)).setText(matchedInfo.getPhone());
                ((TextView) findViewById(R.id.completedTransactionDetails_email)).setText(matchedInfo.getEmail());
                ((TextView) findViewById(R.id.completedTransactionDetails_company)).setText(matchedInfo.getCompany());
                ((TextView) findViewById(R.id.completedTransactionDetails_location)).setText(otherAddress);
                ((TextView) findViewById(R.id.completedTransactionDetails_name)).setText(((CompletedTransaction) t).getDateMatched());
                ((TextView) findViewById(R.id.transactionDetails_priceVal)).setText(((CompletedTransaction) t).getPrice());
            } else {
                findViewById(R.id.completedDetails).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.transactionDetails_priceVal)).setText(t.getAddress());
                ((TextView) findViewById(R.id.transactionDetails_priceVal)).setText(t.getBid());
            }
            ((TextView) findViewById(R.id.transactionDetails_address)).setText(t.getAddress());
            ((TextView) findViewById(R.id.transactionDetails_dateFrom)).setText(t.getDateFrom());
            ((TextView) findViewById(R.id.transactionDetails_dateTo)).setText(t.getDateTo());
            ((TextView) findViewById(R.id.transactionDetails_weight)).setText(t.getWeight() + "");
            ((TextView) findViewById(R.id.transactionDetails_organic)).setText
                    (t.getOrganic() ? getString(R.string.organicWord) : getString(R.string.inorganicWord));
            if(t.isSupply()){
                ((TextView) findViewById(R.id.transactionDetailsSupply)).setText
                        (t.isSupply() ? getString(R.string.supplyDetails) : getString(R.string.demandReqString));
            }
        } else {
            Toast.makeText(this, "Failed to retrieve transaction history", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
