package ic.hku.hk;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MatchedDialog {

    static <T extends Activity> void matchedDialog(final T context
            , final boolean isSup, final String price, final String weight
            , final String pairDeadline, final String pairName, final String pairNumber) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = context.getLayoutInflater().inflate(R.layout.dialog_matched_notification, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        TextView introText = (TextView) dialog.findViewById(R.id.matchedNotificationText);
        TextView priceText = (TextView) dialog.findViewById(R.id.matched_priceVal);
        TextView weightText = (TextView) dialog.findViewById(R.id.matched_weight);
        TextView deadlineText = (TextView) dialog.findViewById(R.id.matched_dateExpireLabel);
        TextView pairNameText = (TextView) dialog.findViewById(R.id.matched_name);
        TextView pairNumberText = (TextView) dialog.findViewById(R.id.matched_phone);
        Button fullDetailsButton = (Button) dialog.findViewById(R.id.matched_full_details);

        if (isSup) {
            introText.setText("Demander found!");
        } else {
            introText.setText("Supplier found!");
        }

        priceText.setText(price);
        weightText.setText(weight);
        deadlineText.setText(pairDeadline);
        pairNameText.setText(pairName);
        pairNumberText.setText(pairNumber);

        fullDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

}
