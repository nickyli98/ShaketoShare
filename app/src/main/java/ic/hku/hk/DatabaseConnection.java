package ic.hku.hk;

import com.google.android.gms.maps.model.LatLng;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import java.sql.*;

public class DatabaseConnection {

    private final String user;
    private final String password;
    private final String ip;
    private final String dbName;
    private String phoneNumber;

    public Connection con;

    public DatabaseConnection(String user, String password, String ip, String dbName) {
        this.user = user;
        this.password = password;
        this.ip = ip;
        this.dbName = dbName;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            System.out.println(ip);
            System.out.println(dbName);
            System.out.println(user);
            System.out.println(password);
            con = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbName, user, password);
            System.out.println("done");
        } catch (IllegalAccessException e) {
            System.out.println("illegal");
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.out.println("instantiation");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("sql");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("classnotfound");
            e.printStackTrace();
        }

    }

    public DatabaseConnection(String user, String password, String ip, String dbName, String phoneNumber) {
        this(user, password, ip, dbName);
        phoneNumber = phoneNumber;
    }



    public static void main(String[] args) throws SQLException {
        DatabaseConnection dbc = new DatabaseConnection("shake", "shake", "147.8.133.49", "s2s");
        dbc.share("Knowles", new LatLng(3, 1), true, true, "1111/33/11", "1111/33/12", 41.2);
    }


    public boolean confirmPassword(String user, String password) throws SQLException {
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select * from user where phone_number='"
                + user + "' and password='" + password + "'");
        //Makes sure that the user exist
        boolean r = rs.next();
        rs.close();
        statement.close();
        return r;
    }

    public boolean createUser(String name, String company, String email, String phone, String password) throws SQLException{
        Statement statement = con.createStatement();
        try {
            statement.executeUpdate("insert into user VALUES('" + phone + "', '" + password + "');");
            statement.executeUpdate("insert into user_info VALUES('" + name + "', '"
                    + company + "', '" + email + "', '" + phone + "');");
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


    public List<Transaction> getOrders(boolean doneBool) throws SQLException {
        Statement statement = con.createStatement();
        Statement statement2 = null;
        int done = 0;
        if(doneBool) {
            statement2 = con.createStatement();
            done = 1;
        }
        ResultSet orders = statement.executeQuery(GET_HISTORY + phoneNumber + "' and done=" + done + ";");
        if(orders != null){
            List<Transaction> transactions = new ArrayList<>();
            while(orders.next()){
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
                if(doneBool){
                    String idT = isSupply ? "idS" : "idD";
                    String other = isSupply ? "idD" : "idS";
                    ResultSet matched_Transaction = statement2.executeQuery("select * from matched_orders where " + idT + "=" + id);
                    matched_Transaction.next();
                    final int matchedID = matched_Transaction.getInt("id");
                    final int otherId = matched_Transaction.getInt(other);
                    final String dateMatched = matched_Transaction.getString("date");
                    Transaction t = new CompletedTransaction(id, phone, weight, address,
                            lat, lng, isSupply, dateFrom, dateTo, dateSubmitted, matchedID, dateMatched, otherId);
                    transactions.add(t);
                } else {
                    Transaction t = new Transaction(id, phone, weight,
                            address, lat, lng, isSupply, dateFrom, dateTo, dateSubmitted);
                    transactions.add(t);
                }
            }
            statement.close();
            statement2.close();
            return transactions;
        } else {
            statement.close();
            statement2.close();
            return null;
        }
    }

    public void closeConnection() throws SQLException {
        con.close();
    }
}
