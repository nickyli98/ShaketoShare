package ic.hku.hk;

public final class SQLQuery {

    static final String DRIVER_CLASS_LOCATION = "com.mysql.jdbc.Driver";
    static final String CONFIRM_PASSWORD_QUERY = "SELECT * FROM user WHERE phone_number = ? AND password = ?;";
    static final String USER_VALUES_INSERT = "insert into user VALUES(?, ?);";
    static final String USER_INFO_VALUES_INSERT = "insert into user_info VALUES(?, ?, ?, ?);";
    static final String SHARE_QUERY = "insert into share_history (weight, organic, address, " +
            "latitude, longitude, supply, phone_number, dateFrom, dateTo, dateSubmitted, bid) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    static final String GET_HISTORY = "SELECT * FROM share_history WHERE phone_number = ? AND done = ?;";
    static final String GET_RADIUS_ITEMS = "SELECT * FROM share_history where done = 0 AND " +
            "latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ?;";

}
