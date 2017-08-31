package ic.hku.hk;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.Serializable;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();

            final String isSup = data.get("isSup");
            final String isOrganic = data.get("isOrganic");
            final String price = data.get("price");
            final String weight = data.get("weight");
            final String originalWeight = data.get("originalWeight");
            final String originalLat = data.get("originalLat");
            final String originalLon = data.get("originalLon");
            final String originalAddress = data.get("originalAddress");
            final String originalDateFrom = data.get("originalDateFrom");
            final String originalDateTo = data.get("originalDateTo");
            final String originalBid = data.get("originalBid");
            final String overlapDateFrom = data.get("overlapDateFrom");
            final String overlapDateTo = data.get("overlapDateTo");
            final String pairName = data.get("pairName");
            final String pairAddress = data.get("pairAddress");
            final String pairNumber = data.get("pairNumber");
            final String transactionID = data.get("transactionID");
            Intent toDialog = new Intent(this, MatchedDialog.class);
            toDialog.putExtra("isSup", isSup);
            toDialog.putExtra("isOrganic", isOrganic);
            toDialog.putExtra("price", price);
            toDialog.putExtra("weight", weight);
            toDialog.putExtra("originalWeight", originalWeight);
            toDialog.putExtra("originalLat", originalLat);
            toDialog.putExtra("originalLon", originalLon);
            toDialog.putExtra("originalAddress", originalAddress);
            toDialog.putExtra("originalDateFrom", originalDateFrom);
            toDialog.putExtra("originalDateTo", originalDateTo);
            toDialog.putExtra("originalBid", originalBid);
            toDialog.putExtra("overlapDateFrom", overlapDateFrom);
            toDialog.putExtra("overlapDateTo", overlapDateTo);
            toDialog.putExtra("pairName", pairName);
            toDialog.putExtra("pairAddress", pairAddress);
            toDialog.putExtra("pairNumber", pairNumber);
            toDialog.putExtra("transactionID", transactionID);
            startActivity(toDialog);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
