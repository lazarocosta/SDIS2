package database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Users {

    public static boolean isLoginCorrect(Connection c, String email, String password) throws SQLException {
        PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM p2p.users WHERE email = ? AND password = ?");
        preparedStatement.setString(1, email);

        preparedStatement.setString(2, hashPassword(password));
        ResultSet rs = preparedStatement.executeQuery();

        return rs.next();
    }

    public static boolean registerNewUser(Connection c, String email, String password) throws SQLException {
        PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO p2p.users(email, password) VALUES (?, ?)");
        preparedStatement.setString(1, email);

        preparedStatement.setString(2, hashPassword(password));
        preparedStatement.executeUpdate();

        return true;
    }

    private static String hashPassword(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");

            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

}
