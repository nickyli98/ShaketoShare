package ic.hku.hk;

import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

public class CompletedTransaction extends Transaction{

    private final int matched_id;
    private final int other_transaction_id;
    private final String dateMatched;

    public CompletedTransaction(int id, String phone, double weight, String address, double lat, double lng,
                                boolean isSupply, String dateFrom, String dateTo, String dateSubmitted,
                                int matched_id, String date_matched, int other_transaction_id) {
        super(id, phone, weight, address, lat, lng, isSupply, dateFrom, dateTo, dateSubmitted);
        this.matched_id = matched_id;
        this.dateMatched = date_matched;
        this.other_transaction_id = other_transaction_id;
    }

    public int getMatched_id() {
        return matched_id;
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
    }

}
