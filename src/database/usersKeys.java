package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class usersKeys {

    public static boolean insertUserKey(Connection c, int iduser, String key) throws SQLException {
        PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO p2p.usersKeys(key, user_id) VALUES (?, ?)");
        preparedStatement.setString(1, key);

        preparedStatement.setInt(2, iduser);
        preparedStatement.executeUpdate();

        return true;
    }

    public static String loadUserKey(Connection c, int iduser) throws SQLException {
        PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM p2p.usersKeys");
        ResultSet rs = preparedStatement.executeQuery();

        if(!rs.next())
            return null;
        else
           return  rs.getString("key");

    }

}
