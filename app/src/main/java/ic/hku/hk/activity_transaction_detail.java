package ic.hku.hk;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

import static ic.hku.hk.DatabaseVariables.dbc;

public class activity_transaction_detail extends Activity {

    private Transaction t;
    private boolean completed;
    private Button delete;
    private UserInfo matchedInfo;
    private String otherAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        t = getIntent().getExtras().getParcelable("Transaction");
        completed = t instanceof CompletedTransaction;
        delete = findViewById(R.id.transactionCancel);
        if(completed){
            matchedInfo = ((CompletedTransaction) t).getUserInfo();
            otherAddress = ((CompletedTransaction) t).getOther_transaction_address();
            delete.setVisibility(View.GONE);
        } else {
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DeleteOrderTask().execute(t.getId());
                    close();
                }
            });
        }
        initialize();
    }

    private void close(){
        Toast.makeText(this, "Order deleted", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    private void initialize() {
        if(t != null){
            if(completed){
                ((TextView) findViewById(R.id.completedTransactionDetails_name)).setText(matchedInfo.getName());
                final TextView phone = (TextView) findViewById(R.id.completedTransactionDetails_phone);
                phone.setText(matchedInfo.getPhone());
                phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String phoneNumToParse = "tel:" + phone.getText().toString();
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                        phoneIntent.setData(Uri.parse(phoneNumToParse));
                        startActivity(phoneIntent);
                    }
                });
                final TextView email = (TextView) findViewById(R.id.completedTransactionDetails_email);
                email.setText(matchedInfo.getEmail());
                email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String emailToParse = email.getText().toString();
                        String[] addresses = new String[1];
                        addresses[0] = emailToParse;
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
                        if (emailIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(emailIntent);
                        }
                    }
                });
                ((TextView) findViewById(R.id.completedTransactionDetails_company)).setText(matchedInfo.getCompany());
                final TextView location = (TextView) findViewById(R.id.completedTransactionDetails_location);
                location.setText(otherAddress);
                location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        double lat = t.getLat();
                        double lng = t.getLng();
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query="+lat+","+lng));
                        startActivity(intent);
                    }
                });
                ((TextView) findViewById(R.id.completedTransactionDetails_date)).setText(((CompletedTransaction) t).getDateMatched());
                ((TextView) findViewById(R.id.transactionDetails_priceVal)).setText(((CompletedTransaction) t).getPrice());
                ((TextView) findViewById(R.id.transactionDetails_price)).setText("Agreed price: ");
            } else {
                findViewById(R.id.completedDetails).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.transactionDetails_priceVal)).setText(t.getAddress());
                ((TextView) findViewById(R.id.transactionDetails_priceVal)).setText(t.getBid());
                ((TextView) findViewById(R.id.transactionDetails_price)).setText("Your bid: ");
            }
            ((TextView) findViewById(R.id.transactionDetails_dateSubmitted)).setText(t.getDateSubmitted());
            final TextView address = (TextView) findViewById(R.id.transactionDetails_address);
            address.setText(t.getAddress());
            address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    double lat = t.getLat();
                    double lng = t.getLng();
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query="+lat+","+lng));
                    startActivity(intent);
                }
            });
            ((TextView) findViewById(R.id.transactionDetails_dateFrom)).setText(t.getDateFrom());
            ((TextView) findViewById(R.id.transactionDetails_dateTo)).setText(t.getDateTo());
            ((TextView) findViewById(R.id.transactionDetails_weight)).setText(t.getWeight() + "");
            ((TextView) findViewById(R.id.transactionDetails_organic)).setText
                    (t.getOrganic() ? getString(R.string.organicWord) : getString(R.string.inorganicWord));
            ((TextView) findViewById(R.id.transactionDetailsSupply)).setText
                    (t.isSupply() ? getString(R.string.supplyDetails) : getString(R.string.demandReqString));
        } else {
            Toast.makeText(this, "Failed to retrieve transaction history", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class DeleteOrderTask extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                dbc.deleteTask(integers[0]);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
