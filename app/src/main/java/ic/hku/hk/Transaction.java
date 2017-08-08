package ic.hku.hk;

import com.google.android.gms.maps.model.LatLng;

public class Transaction {

    private final int id;
    private final String phone;
    private final double weight;
    private final String address;
    private final LatLng latLng;
    private final boolean isSupply;
    private final String dateFrom;
    private final String dateTo;
    private final String dateSubmitted;

    public Transaction(int id, String phone, double weight, String address, LatLng latLng,
                       boolean isSupply, String dateFrom, String dateTo, String dateSubmitted) {
        this.id = id;
        this.phone = phone;
        this.weight = weight;
        this.address = address;
        this.latLng = latLng;
        this.isSupply = isSupply;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.dateSubmitted = dateSubmitted;
    }

    public int getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public double getWeight() {
        return weight;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public boolean isSupply() {
        return isSupply;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public String getDateSubmitted() {
        return dateSubmitted;
    }
}
