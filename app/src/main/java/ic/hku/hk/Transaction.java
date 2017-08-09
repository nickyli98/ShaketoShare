package ic.hku.hk;

import android.os.Parcel;
import android.os.Parcelable;


public class Transaction implements Parcelable{

    private final int id;
    private final String phone;
    private final double weight;
    private final String address;
    private final double lat;
    private final double lng;
    private final boolean isSupply;
    private final String dateFrom;
    private final String dateTo;
    private final String dateSubmitted;
    private final boolean organic;
    private final double bid;

    public Transaction(int id, String phone, double weight, String address, double lat, double lng,
                       boolean isSupply, String dateFrom, String dateTo, String dateSubmitted, boolean organic, double bid) {
        this.id = id;
        this.phone = phone;
        this.weight = weight;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.isSupply = isSupply;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.dateSubmitted = dateSubmitted;
        this.organic = organic;
        this.bid = bid;
    }

    public String getBid(){
        return bid + "";
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

    public double getLat(){
        return lat;
    }

    public double getLng(){
        return lng;
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


    //PARCEL STUFF
    public Transaction(Parcel in){
        String[] data = new String[9];
        id = in.readInt();
        phone = in.readString();
        weight = in.readDouble();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        isSupply = in.readByte() != 0;
        dateFrom = in.readString();
        dateTo = in.readString();
        dateSubmitted = in.readString();
        organic = in.readByte() != 0;
        bid = in.readDouble();
    }

    public boolean getOrganic() {
        return organic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(phone);
        parcel.writeDouble(weight);
        parcel.writeString(address);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeByte((byte) (isSupply ? 1 : 0));
        parcel.writeString(dateFrom);
        parcel.writeString(dateTo);
        parcel.writeString(dateSubmitted);
        parcel.writeByte((byte) (organic ? 1 : 0));
        parcel.writeDouble(bid);
    }

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel parcel) {
            return new Transaction(parcel);
        }

        @Override
        public Transaction[] newArray(int i) {
            return new Transaction[i];
        }
    };
}
