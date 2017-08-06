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

public class LanguageDialog {

   static <T extends Activity> void languageDialog(final T context, final LinearLayout areaCodeSelection){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = context.getLayoutInflater().inflate(R.layout.dialog_language_selection, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        LinearLayout en = (LinearLayout) dialog.findViewById(R.id.language_selection_en);
        LinearLayout cn = (LinearLayout) dialog.findViewById(R.id.language_selection_cn);
        LinearLayout zh = (LinearLayout) dialog.findViewById(R.id.language_selection_zh);

        en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ImageView) context.findViewById(R.id.settings_language_img)).setImageResource(R.drawable.english_flag);
                changeLanguage("en", context);
                dialog.cancel();
            }
        });

        cn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ImageView) context.findViewById(R.id.settings_language_img)).setImageResource(R.drawable.china_flag);
                changeLanguage("zh_CN", context);
                dialog.cancel();
            }
        });

        zh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ImageView) context.findViewById(R.id.settings_language_img)).setImageResource(R.drawable.hong_kong_flag);
                changeLanguage("zh_TW", context);
                dialog.cancel();
            }
        });
    }

    static <T extends Activity> void changeLanguage(String language, T context){
        Resources res2 = context.getApplicationContext().getResources();
        DisplayMetrics dm2 = res2.getDisplayMetrics();
        android.content.res.Configuration conf2 = res2.getConfiguration();
        conf2.locale = new Locale(language);
        res2.updateConfiguration(conf2, dm2);
        Intent intent = context.getIntent();
        context.finish();
        context.startActivity(intent);
    }
}
