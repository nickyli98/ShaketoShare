package ic.hku.hk;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

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
        dbc.createUser("Dr Chow", "ICL", "jc@ic.ac.uk", "852-11111111", "1234");
        if(dbc.confirmPassword("852-11111111", "1234")){
            System.out.println("HERE");
        }
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
        try {
            Statement statement = con.createStatement();
            statement.executeUpdate("insert into user VALUES('" + phone + "', '" + password + "');");
            statement.executeUpdate("insert into user_info VALUES('" + name + "', '"
                    + company + "', '" + email + "', '" + phone + "');");
            return true;
        } catch (MySQLIntegrityConstraintViolationException e) {
            //Duplicate entry
            return false;
        }

    }

    public void closeConnection() throws SQLException {
        con.close();
    }
}
