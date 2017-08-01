package ic.hku.hk;

import android.app.Activity;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Nicholas Li on 01/08/2017.
 */

public class AndroidUtils {

    static int dpToPx(float dp, Activity activity){
        Resources r = activity.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }
}
