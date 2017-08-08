package ic.hku.hk;

import com.google.android.gms.maps.model.LatLng;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
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

    private Connection con;

    public DatabaseConnection(String user, String password, String ip, String dbName) {
        this.user = user;
        this.password = password;
        this.ip = ip;
        this.dbName = dbName;
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



    public static void main(String[] args) throws SQLException {
        DatabaseConnection dbc = new DatabaseConnection("shake", "shake", "147.8.133.49", "s2s", "852-69793034");
    }


    public boolean confirmPassword(String user, String password) throws SQLException {
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(CONFIRM_PASSWORD_QUERY + user + "' and password='" + password + "'");
        //Makes sure that the user exist
        boolean r = rs.next();
        rs.close();
        statement.close();
        return r;
    }

    public boolean createUser(String name, String company, String email, String phone, String password) throws SQLException{
        Statement statement = con.createStatement();
        try {
            statement.executeUpdate(USER_VALUES_QUERY + phone + "', '" + password + "');");
            statement.executeUpdate(USER_INFO_VALUES_QUERY + name + "', '" + company + "', '"
                    + email + "', '" + phone + "');");
            statement.close();
            return true;
        } catch (MySQLIntegrityConstraintViolationException e) {
            //Duplicate entry
            statement.close();
            return false;
        }
    }

    public void share(String address, LatLng addressLatLng, boolean organic,
                         boolean isSupply, String dateFrom, String dateTo, double weight) throws SQLException {
        Statement statement = con.createStatement();
        int org = organic ? 1 : 0;
        int supply = isSupply ? 1 : 0;
        Date date = Calendar.getInstance().getTime();
        String dateS = sdf.format(date);
        statement.executeUpdate(SHARE_QUERY + weight + ", " + org + ", '" + address
                + "', " + addressLatLng.latitude + ", " + addressLatLng.longitude
                + ", " + supply + ", '" + phoneNumber + "', '"
                + dateFrom + "', '" + dateTo + "', '" + dateS + "');");
        statement.close();
    }

    public List<Transaction> getOrders(boolean doneB) throws SQLException {
        Statement statement = con.createStatement();
        int done = doneB ? 1 : 0;
        ResultSet orders = statement.executeQuery(GET_HISTORY + phoneNumber + "' and done=" + done + ";");
        if(orders != null){
            List<? extends Transaction> transactions
                    = doneB ? new ArrayList<CompletedTransactions>() : new ArrayList<Transaction>();
            while(orders.next()){
                /*
                final int id;
    private final String phone;
    private final double weight;
    private final String address;
    private final LatLng latLng;
    private final boolean isSupply;
    private final String dateFrom;
    private final String dateTo;
    private final String dateSubmitted;
                 */
                final int id = orders.getInt("id");
                final String phone = orders.getString("phone_number");
                final double weight = orders.getDouble("weight");
                final String address = orders.getString("address");
                final LatLng latLng = new LatLng(orders.getDouble("latitude"), orders.getDouble("longitude"));
                final boolean isSupply = orders.getBoolean("supply");
                final String dateFrom;
                return null;
            }
        } else {
            return null;
        }
    }

    public void closeConnection() throws SQLException {
        con.close();
    }
}
