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
        if(con == null){
            con = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbName, user, this.password);
        }
        System.out.println("creating statement....");
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

    public boolean share(String address, LatLng addressLatLng, boolean organic,
                         boolean isSupply, String dateFrom, String dateTo, double weight) throws SQLException {
        Statement statement = con.createStatement();
        int org = organic ? 1 : 0;
        int supply = isSupply ? 1 : 0;
        statement.executeUpdate("insert into share_history VALUES(" + weight + ", " + org + ", '"
                + address + "', " + addressLatLng.latitude + ", " + addressLatLng.longitude + ", "
                + supply + ", '" + phoneNumber + "', '" + dateFrom + "', '" + dateTo + "');");
        statement.close();
        return true;
    }

    public void closeConnection() throws SQLException {
        con.close();
    }
}
