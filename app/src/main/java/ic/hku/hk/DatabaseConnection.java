package ic.hku.hk;

import java.sql.*;

public class DatabaseConnection {

    private final String user;
    private final String password;
    private final String ip;
    private final String dbName;

    private Connection con;

    public DatabaseConnection(String user, String password, String ip, String dbName) {
        this.user = user;
        this.password = password;
        this.ip = ip;
        this.dbName = dbName;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            con = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + dbName, user, password);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void main(String[] args) throws SQLException {
        DatabaseConnection dbc = new DatabaseConnection("shake", "shake", "147.8.133.49", "s2s");
        if(dbc.confirmPassword("852-69793034", "0000")){
            System.out.println("checked");
        }
    }


    public boolean confirmPassword(String user, String password) throws SQLException {
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select * from user where phone_number='"
                + user + "' and password='" + password + "'");
        return rs.next();
    }

    public void closeConnection() throws SQLException {
        con.close();
    }
}
