package ic.hku.hk;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

import static ic.hku.hk.DatabaseVariables.dbc;
import static ic.hku.hk.DatabaseVariables.historyTransactions;

public class MatchedDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_matched_notification);

        Bundle bundle = getIntent().getExtras();
        final String isSup = bundle.getString("isSup");
        final String isOrganic = bundle.getString("isOrganic");
        String price = bundle.getString("price");
        String weight = bundle.getString("weight");
        String originalWeight = bundle.getString("originalWeight");
        final String originalLat = bundle.getString("originalLat");
        final String originalLon = bundle.getString("originalLon");
        final String originalAddress = bundle.getString("originalAddress");
        final String originalDateFrom = bundle.getString("originalDateFrom");
        final String originalDateTo = bundle.getString("originalDateTo");
        final String originalBid = bundle.getString("originalBid");
        String overlapDateFrom = bundle.getString("overlapDateFrom");
        String overlapDateTo = bundle.getString("overlapDateTo");
        String pairName = bundle.getString("pairName");
        String pairAddress = bundle.getString("pairAddress");
        String pairNumber = bundle.getString("pairNumber");
        final int transactionID = Integer.parseInt((bundle.getString("transactionID")));

        TextView introText = findViewById(R.id.matchedNotificationText);
        TextView priceText = findViewById(R.id.matched_priceVal);
        TextView weightText = findViewById(R.id.matched_weight);
        TextView dateFromText = findViewById(R.id.matched_dateFrom);
        TextView dateToText = findViewById(R.id.matched_dateTo);
        TextView pairNameText = findViewById(R.id.matched_name);
        TextView pairAddressText = findViewById(R.id.matched_address);
        TextView pairNumberText = findViewById(R.id.matched_number);
        Button relistButton = findViewById(R.id.matched_listRemainderButton);
        Button fullDetailsButton = findViewById(R.id.matched_full_details);

        boolean isSupBool = "1".equals(isSup);
        if (isSupBool) {
            introText.setText("Demander found!");
        } else {
            introText.setText("Supplier found!");
        }

        priceText.setText(price);
        weightText.setText(weight);
        dateFromText.setText(overlapDateFrom);
        dateToText.setText(overlapDateTo);
        pairNameText.setText(pairName);
        pairAddressText.setText(pairAddress);
        pairNumberText.setText(pairNumber);

        if (Double.parseDouble(originalWeight) - Double.parseDouble(weight) > 0) {
            final String remainingWeight = String.valueOf(Double.parseDouble(originalWeight) - Double.parseDouble(weight));
            String relistButtonText = "Continue " + (isSupBool ? "supply" : "demand") + " for remaining " + remainingWeight + " tonnes";
            relistButton.setText(relistButtonText);
            relistButton.setVisibility(View.VISIBLE);
            relistButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new reShareTask().execute(originalAddress, originalLat, originalLon, isOrganic, isSup, originalDateFrom, originalDateTo, remainingWeight, originalBid);
                    finish();
                }
            });
        }

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

    private class reShareTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(MatchedDialog.this, R.string.SharedToast, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MatchedDialog.this, "Failed to share", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String address = strings[0];
            double lat = Double.parseDouble(strings[1]);
            double lon = Double.parseDouble(strings[2]);
            int organic = Integer.parseInt(strings[3]);
            int isSupply = Integer.parseInt(strings[4]);
            String dateFrom = strings[5];
            String dateTo = strings[6];
            double weight = Double.parseDouble(strings[7]);
            double bid = Double.parseDouble(strings[8]);
            try {
                return dbc.share(address, lat, lon, organic, isSupply, dateFrom, dateTo, weight, bid);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
