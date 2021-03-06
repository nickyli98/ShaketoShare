package ic.hku.hk;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static ic.hku.hk.AndroidUtils.dpToPx;

public class TransactionListUtil {

    static <T extends AppCompatActivity> void listAdd(final T context, List<Transaction> transactionList, int scrollViewID, final boolean done){
        LinearLayout sv = (LinearLayout) context.findViewById(scrollViewID);
        boolean first = true;
        if(transactionList != null){
            for(final Transaction t : transactionList){
                if(first){
                    first = false;
                } else {
                    View v = new View(context);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1, context));
                    int margin = (int) context.getResources().getDimension(R.dimen.lineMargin);
                    lp.setMargins(margin, 0, margin, 0);
                    v.setLayoutParams(lp);
                    v.setBackgroundColor(context.getResources().getColor(R.color.lineColour));
                    sv.addView(v);
                }
                TextView tv = new TextView(context);
                String text = "";
                if (done){
                    text += ((CompletedTransaction) t).getDateMatched();
                } else {
                    text += t.getDateSubmitted();
                }
                text += " - " + t.getWeight() + " "
                        + context.getString(R.string.tonnes) + "\n" + t.getAddress();
                tv.setText(text);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
                tv.isClickable();
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent toDetails = new Intent(context, activity_transaction_detail.class);
                        toDetails.putExtra("Transaction", t);
                        context.startActivity(toDetails);
                    }
                });
                int px12 = dpToPx(12, context);
                tv.setPadding(0, px12, 0, px12);
                sv.addView(tv);
            }
        }
    }
}
