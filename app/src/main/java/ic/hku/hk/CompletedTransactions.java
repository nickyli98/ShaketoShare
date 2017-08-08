package ic.hku.hk;

import com.google.android.gms.maps.model.LatLng;

public class CompletedTransactions extends Transaction{

    private final int matched_id;
    private final String dateMatched;

    public CompletedTransactions(int id, String phone, double weight, String address, LatLng latLng,
                                 boolean isSupply, String dateFrom, String dateTo,
                                 String dateSubmitted, int matched_id, String date_matched) {
        super(id, phone, weight, address, latLng, isSupply, dateFrom, dateTo, dateSubmitted);
        this.matched_id = matched_id;
        this.dateMatched = date_matched;
    }

    public int getMatched_id() {
        return matched_id;
    }

    public String getDateMatched() {
        return dateMatched;
    }
}
