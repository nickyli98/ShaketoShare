package ic.hku.hk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import static ic.hku.hk.AndroidUtils.dpToPx;

public class TransactionListUtil {

    static <T extends AppCompatActivity> void listAdd(final T context, List<Transaction> transactionList, int scrollViewID, boolean done){
        ScrollView sv = (ScrollView) context.findViewById(scrollViewID);
        for(final Transaction t : transactionList){
            TextView tv = new TextView(context);
            String text = "";
            if (done){
                text += ((CompletedTransaction) t).getDateMatched();
            } else {
                text += t.getDateSubmitted();
            }
            text += ", " + context.getString(R.string.weight_hint) + ": " + t.getWeight()
                    + context.getString(R.string.tonnes) + ", " + t.getAddress();
            tv.setText(text);
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
