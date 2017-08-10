package ic.hku.hk;

import android.os.Parcel;
import android.os.Parcelable;

public class PendingTransaction extends Transaction {

    public PendingTransaction(int id, String phone, double weight, String address, double lat, double lng, boolean isSupply, String dateFrom, String dateTo, String dateSubmitted, boolean organic, double bid) {
        super(id, phone, weight, address, lat, lng, isSupply, dateFrom, dateTo, dateSubmitted, organic, bid);
    }

    public PendingTransaction(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel parcel) {
            return new PendingTransaction(parcel);
        }

        @Override
        public Transaction[] newArray(int i) {
            return new Transaction[i];
        }
    };
}
