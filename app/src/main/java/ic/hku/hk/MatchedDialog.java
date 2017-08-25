package ic.hku.hk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static ic.hku.hk.DatabaseVariables.historyTransactions;

public class MatchedDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_matched_notification);

        boolean isSup = getIntent().getExtras().getBoolean("isSup");
        String price = getIntent().getExtras().getString("price");
        String weight = getIntent().getExtras().getString("weight");
        String pairDeadline = getIntent().getExtras().getString("pairDeadline");
        String pairName = getIntent().getExtras().getString("pairName");
        String pairAddress = getIntent().getExtras().getString("pairAddress");
        String pairNumber = getIntent().getExtras().getString("pairNumber");
        final int transactionID = Integer.parseInt((getIntent().getExtras().getString("transactionID")));

        TextView introText = findViewById(R.id.matchedNotificationText);
        TextView priceText = findViewById(R.id.matched_priceVal);
        TextView weightText = findViewById(R.id.matched_weight);
        TextView deadlineText = findViewById(R.id.matched_dateExpire);
        TextView pairNameText = findViewById(R.id.matched_name);
        TextView pairAddressText = findViewById(R.id.matched_address);
        TextView pairNumberText = findViewById(R.id.matched_number);
        Button fullDetailsButton = findViewById(R.id.matched_full_details);

        if (isSup) {
            introText.setText("Demander found!");
        } else {
            introText.setText("Supplier found!");
        }

        priceText.setText(price);
        weightText.setText(weight);
        deadlineText.setText(pairDeadline);
        pairNameText.setText(pairName);
        pairAddressText.setText(pairAddress);
        pairNumberText.setText(pairNumber);

        fullDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toDetails = new Intent(MatchedDialog.this, activity_transaction_detail.class);
                //need to ensure notnull
                if (historyTransactions != null) {
                    for (Transaction t : historyTransactions) {
                        if (t.getId() == transactionID) {
                            toDetails.putExtra("Transaction", t);
                            startActivity(toDetails);
                            finish();
                            break;
                        }
                    }
                }
            }
        });
    }
}
