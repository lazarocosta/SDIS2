package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UsersKeys {

    public static boolean insertUserKey(Connection c, int iduser, byte[] asymmetrickeyprivate, byte[] asymmetrickeypublic, byte[] symmetricKey) throws SQLException {
        PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO p2p.userskeys(assymmetrickeyprivate,assymmetrickeypublic, symmetrickey,user_id) VALUES (?, ?, ?,?)");
        preparedStatement.setBytes(1, asymmetrickeyprivate);
        preparedStatement.setBytes(2, asymmetrickeypublic);
        preparedStatement.setBytes(3, symmetricKey);

        preparedStatement.setInt(4, iduser);
        preparedStatement.executeUpdate();

        System.out.println("inserted the keys in database successful");

        return true;
    }

    public static ResultSet loadUserKey(Connection c, int iduser) throws SQLException {
        PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM p2p.userskeys WHERE user_id= ?");
        preparedStatement.setInt(1, iduser);
        ResultSet rs = preparedStatement.executeQuery();


        if (!rs.next()) {
            System.out.println("Not loaded the database keys");
            return null;
        } else {
            System.out.println("loaded the database keys successfully ");
            return rs;
        }

    }

}
