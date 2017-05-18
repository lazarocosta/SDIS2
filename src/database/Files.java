package database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Files {
	
	public static ArrayList<String[]> getFileNames(Connection c,String email) throws SQLException{
		//Get user id 
		PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM p2p.users WHERE email = ?");
		preparedStatement.setString(1, email);
		
		ResultSet rs = preparedStatement.executeQuery();
		rs.next();
		Integer UserId = rs.getInt("user_id");
		System.out.println(UserId);
		
		
		//Get info from files
		preparedStatement = c.prepareStatement("SELECT * FROM p2p.files WHERE user_id = ?");
		preparedStatement.setInt(1, UserId);
		rs = preparedStatement.executeQuery();
		rs.next();
		
		ArrayList<String[]> files = new ArrayList();
		do{
			String file_id = rs.getString("file_id");
			String name = rs.getString("name");
			System.out.println(name);
			String added_time = rs.getTimestamp("added_time").toString();
			
			String[] file = new String[]{file_id,name,added_time};
			files.add(file);
		}while(rs.next());
		
		return files;
	}
	
	public static boolean moveFileToDeleted(Connection c, int file_id) throws SQLException {
		PreparedStatement preparedStatement = c.prepareStatement("DELETE FROM p2p.files WHERE file_id=?");
		preparedStatement.setInt(1, file_id);

        preparedStatement.executeUpdate();
		
		preparedStatement = c.prepareStatement("INSERT INTO p2p.deleted_files(file_id) VALUES (?)");
        preparedStatement.setInt(1, file_id);

        preparedStatement.executeUpdate();

        return true;
    }
}