package ic.hku.hk;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class AreaCodeDialog {

    static <T extends Activity> void areaCodeDialog(final T context, final LinearLayout areaCodeSelection){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = context.getLayoutInflater().inflate(R.layout.dialog_area_code_selection, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        LinearLayout HK = (LinearLayout) dialog.findViewById(R.id.areaCodeSelection_hongKong);
        LinearLayout Macau = (LinearLayout) dialog.findViewById(R.id.areaCodeSelection_macau);
        LinearLayout China = (LinearLayout) dialog.findViewById(R.id.areaCodeSelection_china);

        HK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TextView) context.findViewById(R.id.areaCodeSelection_text)).setText(R.string.hk_area_code);
                ((ImageView) context.findViewById(R.id.areaCodeSelection_image)).setImageResource(R.drawable.hong_kong_flag);
                dialog.cancel();
            }
        });

        Macau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TextView) context.findViewById(R.id.areaCodeSelection_text)).setText(R.string.macau_area_code);
                ((ImageView) context.findViewById(R.id.areaCodeSelection_image)).setImageResource(R.drawable.macau_flag);
                dialog.cancel();
            }
        });

        China.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TextView) context.findViewById(R.id.areaCodeSelection_text)).setText(R.string.china_area_code);
                ((ImageView) context.findViewById(R.id.areaCodeSelection_image)).setImageResource(R.drawable.china_flag);
                dialog.cancel();
            }
        });
    }
}
