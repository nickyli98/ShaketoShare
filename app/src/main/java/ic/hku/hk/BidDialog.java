package ic.hku.hk;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

import static ic.hku.hk.DatabaseVariables.dbc;
import static ic.hku.hk.DatabaseVariables.presetBid;

public class BidDialog<U extends AppCompatActivity> implements AsyncResponse {

    private final U context;

    public BidDialog(U context) {
        this.context = context;
    }

    void bidDialog(final String address, final String lat
            , final String lon, final String organic, final String isSupply, final String dateFrom
            , final String dateTo, final String weight, DatabaseConnection dbc, final String presetBid) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = context.getLayoutInflater().inflate(R.layout.dialog_share_bid, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        final TextView bidPrompt = (TextView) dialog.findViewById(R.id.bidPromptText);
        final EditText bidAmount = (EditText) dialog.findViewById(R.id.bidAmount);
        final Button bidConfirm = (Button) dialog.findViewById(R.id.bidConfirm);

        if (bidPrompt != null && bidConfirm != null && bidAmount != null) {
            if (isSupply.equals("1")) {
                bidPrompt.setText(context.getString(R.string.supply_bid_prompt));
            } else {
                bidPrompt.setText(context.getString(R.string.demand_bid_prompt));
            }

            if (!presetBid.equals("0")) {
                bidAmount.setText(presetBid);
                bidAmount.setSelection(bidAmount.getText().length());
            }

            bidConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String bid = bidAmount.getText().toString();
                    shareRequestTask shareRequest = new shareRequestTask();
                    shareRequest.delegate = BidDialog.this;
                    shareRequest.execute(address, lat, lon, organic, isSupply, dateFrom, dateTo, weight, bid);
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    resetPresetBid();
                    dialog.cancel();
                }
            });
        }
    }

    @Override
    public <T> void processFinish(T output) {
        boolean aBoolean = (boolean) (Object) output;
        if (aBoolean) {
            Toast.makeText(context, R.string.SharedToast, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show();
        }
    }

    private static class shareRequestTask extends AsyncTask<String, Void, Boolean> {

        public AsyncResponse delegate = null;

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            delegate.processFinish(aBoolean);
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

    private void resetPresetBid() {
        presetBid = "0";

    }

}
