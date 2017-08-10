package ic.hku.hk;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import java.sql.SQLException;

import static ic.hku.hk.DatabaseVariables.*;

public class CompletedTransaction extends PendingTransaction implements AsyncResponse{

    private final int matched_id;
    private final int other_transaction_id;
    private final String dateMatched;
    private final double price;
    private UserInfo userInfo;

    public CompletedTransaction(int id, String phone, double weight, String address, double lat, double lng,
                                boolean isSupply, String dateFrom, String dateTo, String dateSubmitted, boolean organic, double bid,
                                int matched_id, String date_matched, int other_transaction_id, double price) {
        super(id, phone, weight, address, lat, lng, isSupply, dateFrom, dateTo, dateSubmitted, organic, bid);
        this.matched_id = matched_id;
        this.dateMatched = date_matched;
        this.other_transaction_id = other_transaction_id;
        this.price = price;
        getUserInfoTask task = new getUserInfoTask();
        task.delegate = this;
        task.execute(other_transaction_id);
    }

    public int getMatched_id() {
        return matched_id;
    }

    public String getPrice(){
        return price + "";
    }

    public String getDateMatched() {
        return dateMatched;
    }

    public int getOtherId(){
        return other_transaction_id;
    }

    //PARCEL STUFF
    public CompletedTransaction(Parcel in){
        super(in);
        matched_id = in.readInt();
        other_transaction_id = in.readInt();
        dateMatched = in.readString();
        price = in.readDouble();
        userInfo = new UserInfo(in.readString(), in.readString()
                , in.readString(), in.readString());
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(matched_id);
        parcel.writeInt(other_transaction_id);
        parcel.writeString(dateMatched);
        parcel.writeDouble(price);
        parcel.writeString(userInfo.getName());
        parcel.writeString(userInfo.getEmail());
        parcel.writeString(userInfo.getPhone());
        parcel.writeString(userInfo.getCompany());
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public <T> void processFinish(T output) {
        this.userInfo = (UserInfo) output;
    }

    private class getUserInfoTask extends AsyncTask<Integer, Void, UserInfo> {

        public AsyncResponse delegate = null;

        public getUserInfoTask() {
        }

        @Override
        protected void onPostExecute(UserInfo userInfo) {
            delegate.processFinish(userInfo);
        }

        @Override
        protected UserInfo doInBackground(Integer... integers) {
            int transactionId = integers[0];
            try {
                return dbc.getUserInfo(transactionId);
            } catch (SQLException e) {
                return null;
            }
        }
    }

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel parcel) {
            return new CompletedTransaction(parcel);
        }

        @Override
        public Transaction[] newArray(int i) {
            return new Transaction[i];
        }
    };
}
