package ic.hku.hk;

import com.google.android.gms.maps.model.LatLng;
import com.mysql.jdbc.*;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static ic.hku.hk.Constants.*;
import static ic.hku.hk.SQLQuery.*;

public class DatabaseConnection {

    private final String user;
    private final String password;
    private final String ip;
    private final String dbName;
    private String phoneNumber;
    private final Thread autoUpdate;

    private Connection con;

    public DatabaseConnection(String user, String password, String ip, String dbName) {
        this.user = user;
        this.password = password;
        this.ip = ip;
        this.dbName = dbName;
        autoUpdate = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    //TODO new match
                    try{
                        Thread.sleep(60000);
                    } catch (InterruptedException e){
                        break;
                    }
                }
            }
        });
        try {
            Class.forName(DRIVER_CLASS_LOCATION).newInstance();
            con = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbName, user, password);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public DatabaseConnection(String user, String password, String ip, String dbName, String phoneNumber) {
        this(user, password, ip, dbName);
        this.phoneNumber = phoneNumber;
    }

    public boolean confirmPassword(String user, String password) throws SQLException {
        PreparedStatement statement = con.prepareStatement(CONFIRM_PASSWORD_QUERY);
        statement.setString(1, user);
        statement.setString(2, password);
        ResultSet rs = statement.executeQuery();
        //Makes sure that the user exist
        boolean r = rs.next();
        rs.close();
        statement.close();
        return r;
    }

    public boolean createUser(String name, String company, String email, String phone, String password) throws SQLException{
        if(con == null){
            con = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbName, user, this.password);
        }
        PreparedStatement statement = con.prepareStatement(USER_VALUES_INSERT);
        statement.setString(1, phone);
        statement.setString(2, password);
        statement.setString(3, sdf.format(Calendar.getInstance().getTime()));
        try {
            statement.executeUpdate();
            statement = con.prepareStatement(USER_INFO_VALUES_INSERT);
            statement.setString(1, name);
            statement.setString(2, company);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (MySQLIntegrityConstraintViolationException e) {
            //Duplicate entry
            statement.close();
            return false;
        }
    }

    public boolean share(String address, double lat, double lon, int organic,
                      int isSupply, String dateFrom, String dateTo, double weight, double bid) throws SQLException {
        PreparedStatement statement = con.prepareStatement(INSERT_SHARE);
        Date date = Calendar.getInstance().getTime();
        String dateS = sdf.format(date);
        statement.setDouble(1, weight);
        statement.setInt(2, organic);
        statement.setString(3, address);
        statement.setDouble(4, lat);
        statement.setDouble(5, lon);
        statement.setInt(6, isSupply);
        statement.setString(7, phoneNumber);
        statement.setString(8, dateFrom);
        statement.setString(9, dateTo);
        statement.setString(10, dateS);
        statement.setDouble(11, bid);
        try{
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (MySQLIntegrityConstraintViolationException e) {
            statement.close();
            System.out.println(e);
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getOrders(boolean doneBool) throws SQLException {
        PreparedStatement statement = con.prepareStatement(GET_HISTORY);
        Statement statement2 = null;
        int done = 0;
        if(doneBool) {
            statement2 = con.createStatement();
            done = 1;
        }
        statement.setString(1, phoneNumber);
        statement.setInt(2, done);
        ResultSet orders = statement.executeQuery();
        if(orders != null){
            List<Transaction> transactions = new ArrayList<>();
            while(orders.next()){
                transactions.add(getTransaction(orders, doneBool, statement2));
            }
            statement.close();
            if(doneBool){
                statement2.close();
            }
            Collections.reverse(transactions);
            return transactions;
        } else {
            statement.close();
            if(doneBool){
                statement2.close();
            }
            return null;
        }
    }

    private Transaction getTransaction(ResultSet orders, boolean doneBool, Statement statement) throws SQLException {
        final int id = orders.getInt("id");
        final String phone = orders.getString("phone_number");
        final double weight = orders.getDouble("weight");
        final String address = orders.getString("address");
        final double lat = orders.getDouble("latitude");
        final double lng = orders.getDouble("longitude");
        final boolean isSupply = orders.getBoolean("supply");
        final String dateFrom = orders.getString("dateFrom");
        final String dateTo = orders.getString("dateTo");
        final String dateSubmitted = orders.getString("dateSubmitted");
        final boolean organic = orders.getBoolean("organic");
        final double bid = orders.getDouble("bid");
        Transaction t;
        if(doneBool){
            String idT = isSupply ? "idS" : "idD";
            String other = isSupply ? "idD" : "idS";
            ResultSet matched_Transaction = statement.executeQuery("select * from matched_orders where " + idT + "=" + id);
            matched_Transaction.next();
            final int matchedID = matched_Transaction.getInt("id");
            final int otherId = matched_Transaction.getInt(other);
            final String dateMatched = matched_Transaction.getString("date");
            final double price = matched_Transaction.getDouble("price");
            matched_Transaction = statement.executeQuery("select address from share_history where id = " + otherId);
            matched_Transaction.next();
            final String otherAddress = matched_Transaction.getString("address");
            System.out.println("database other address" + otherAddress);
            t = new CompletedTransaction(id, phone, weight, address, lat, lng,
                    isSupply, dateFrom, dateTo, dateSubmitted, organic, bid, matchedID,
                    dateMatched, otherId, price, otherAddress);
        } else {
            t = new PendingTransaction(id, phone, weight,
                    address, lat, lng, isSupply, dateFrom, dateTo, dateSubmitted, organic, bid);
        }
        return t;
    }

    public List<Transaction> getRadiusItems(LatLng currentLocation) throws SQLException {
        //Gets everything within 15km of currentLocation
        double lat = currentLocation.latitude;
        double lng = currentLocation.longitude;
        double latDeg15KM = KM_TO_LATITUDE * 15;
        double lngDeg15KM = KM_TO_LONGITUDE(lat) * 15;
        double latMax = lat + latDeg15KM;
        double latMin = lat - latDeg15KM;
        double lngMax = lng + lngDeg15KM;
        double lngMin = lng - lngDeg15KM;
        System.out.println("latMax: " + latMax + "\nlatMin: " + latMin + "\nlngMax: " + lngMax + "\nlngMin: " + lngMin);
        PreparedStatement statement = con.prepareStatement(GET_RADIUS_ITEMS);
        statement.setDouble(1, latMin);
        statement.setDouble(2, latMax);
        statement.setDouble(3, lngMax);
        statement.setDouble(4, lngMin);
        ResultSet rs = statement.executeQuery();
        List<Transaction> transactions = new ArrayList<>();
        while(rs.next()){
            transactions.add(getTransaction(rs, false, null));
        }
        return transactions;
    }

    public void closeConnection() throws SQLException {
        con.close();
    }

    public UserInfo getUserInfo(int transactionId) throws SQLException {
        PreparedStatement statement = con.prepareStatement(USER_INFO_QUERY);
        statement.setInt(1, transactionId);
        ResultSet r = statement.executeQuery();
        r.next();
        String number = r.getString("phone_number");
        statement = con.prepareStatement(USER_INFO_QUERY_TWO);
        statement.setString(1, number);
        r = statement.executeQuery();
        r.next();
        UserInfo u = new UserInfo(r.getString("name"), r.getString("email"), number, r.getString("company"));
        statement.close();
        return u;
    }

    public void deleteTask(Integer orderID) throws SQLException {
        PreparedStatement statement = con.prepareStatement(DELETE_ORDER);
        statement.setInt(1, orderID);
        statement.executeUpdate();
        statement.close();
    }

    public void updateToken(String token) throws SQLException {
        PreparedStatement statement = con.prepareStatement(UPDATE_TOKEN);
        statement.setString(1, token);
        statement.setString(2, phoneNumber);
        statement.executeUpdate();
        statement.close();
    }

    public void clearToken() throws SQLException {
        PreparedStatement statement = con.prepareStatement(UPDATE_TOKEN);
        statement.setString(1, null);
        statement.setString(2, phoneNumber);
        statement.executeUpdate();
        statement.close();
    }

    public String getNameOfUser(String phoneNumber) throws SQLException {
        PreparedStatement statement = con.prepareStatement(GET_NAME_OF_USER);
        statement.setString(1, phoneNumber);
        ResultSet r = statement.executeQuery();
        r.next();
        String name = r.getString("name");
        statement.close();
        return name;
    }

}