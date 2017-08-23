package ic.hku.hk;

import android.app.Service;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.sql.SQLException;

import static ic.hku.hk.DatabaseVariables.dbc;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        System.out.println("REFRESHED TOKEN: " + refreshedToken);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    public static void sendRegistrationToServer(String refreshedToken) {
        new RefreshTokenTask().execute(refreshedToken);
    }

    private static class RefreshTokenTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String token = strings[0];
            try {
                if (dbc != null) {
                    dbc.updateToken(token);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
